package com.example.demo.eval.engine;

import com.example.demo.eval.model.EvalReport;
import com.example.demo.eval.model.EvalResult;
import com.example.demo.eval.model.EvalRule;
import com.example.demo.eval.model.EvalScenario;
import com.example.demo.eval.model.RuleEvaluation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class EvalRunner {

    private final String targetBaseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;

    public EvalRunner(String targetBaseUrl) {
        this.targetBaseUrl =
                targetBaseUrl.endsWith("/")
                        ? targetBaseUrl.substring(0, targetBaseUrl.length() - 1)
                        : targetBaseUrl;
        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build();
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.jsonMapper = new ObjectMapper();
    }

    public List<EvalScenario> loadScenariosFromDirectory(Path dir) throws IOException {
        List<EvalScenario> scenarios = new ArrayList<>();
        if (!Files.exists(dir)) {
            return scenarios;
        }

        try (Stream<Path> paths = Files.walk(dir)) {
            List<Path> files =
                    paths.filter(Files::isRegularFile)
                            .filter(
                                    p ->
                                            p.toString().endsWith(".eval.yml")
                                                    || p.toString().endsWith(".eval.yaml")
                                                    || p.toString().endsWith(".yml")
                                                    || p.toString().endsWith(".yaml"))
                            .toList();

            for (Path file : files) {
                try {
                    EvalScenario scenario = yamlMapper.readValue(file.toFile(), EvalScenario.class);
                    if (scenario.getId() == null || scenario.getId().isBlank()) {
                        scenario.setId(file.getFileName().toString().replaceAll("\\.[^.]+$", ""));
                    }
                    scenarios.add(scenario);
                } catch (Exception e) {
                    System.err.println(
                            "Warning: Failed to parse eval spec "
                                    + file.getFileName()
                                    + ": "
                                    + e.getMessage());
                }
            }
        }
        return scenarios;
    }

    public EvalResult evaluateScenario(EvalScenario scenario) {
        StringBuilder urlBuilder = new StringBuilder(targetBaseUrl);
        if (!scenario.getEndpoint().startsWith("/")) {
            urlBuilder.append("/");
        }
        urlBuilder.append(scenario.getEndpoint());

        if (scenario.getQueryParams() != null && !scenario.getQueryParams().isEmpty()) {
            urlBuilder.append("?");
            scenario.getQueryParams()
                    .forEach((k, v) -> urlBuilder.append(k).append("=").append(v).append("&"));
            urlBuilder.setLength(urlBuilder.length() - 1);
        }

        String fullUrl = urlBuilder.toString();
        HttpRequest.Builder reqBuilder =
                HttpRequest.newBuilder().uri(URI.create(fullUrl)).timeout(Duration.ofSeconds(10));

        if (scenario.getHeaders() != null) {
            scenario.getHeaders().forEach(reqBuilder::header);
        }

        String method = scenario.getMethod() != null ? scenario.getMethod().toUpperCase() : "GET";
        if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
            String bodyContent = "";
            if (scenario.getBody() != null) {
                try {
                    bodyContent = jsonMapper.writeValueAsString(scenario.getBody());
                } catch (Exception e) {
                    bodyContent = scenario.getBody().toString();
                }
            }
            reqBuilder.method(method, HttpRequest.BodyPublishers.ofString(bodyContent));
            if (scenario.getHeaders() == null
                    || !scenario.getHeaders().containsKey("Content-Type")) {
                reqBuilder.header("Content-Type", "application/json");
            }
        } else {
            reqBuilder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        long start = System.currentTimeMillis();
        HttpResponse<String> response;
        long latencyMs;
        try {
            response = httpClient.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());
            latencyMs = System.currentTimeMillis() - start;
        } catch (Exception e) {
            latencyMs = System.currentTimeMillis() - start;
            RuleEvaluation connectionEval =
                    RuleEvaluation.builder()
                            .ruleType("connection")
                            .description("HTTP Connection to " + fullUrl)
                            .passed(false)
                            .expected("HTTP 2xx/3xx/4xx")
                            .actual("Connection Error: " + e.getMessage())
                            .failureReason(
                                    "Target application is unreachable at "
                                            + fullUrl
                                            + ". Ensure the Spring Boot app is running.")
                            .build();

            return EvalResult.builder()
                    .scenarioId(scenario.getId())
                    .scenarioName(scenario.getName())
                    .endpoint(scenario.getEndpoint())
                    .httpStatusCode(-1)
                    .latencyMs(latencyMs)
                    .passed(false)
                    .evaluations(List.of(connectionEval))
                    .diagnostics("Connection failed: " + e.getMessage())
                    .build();
        }

        List<RuleEvaluation> evaluations = new ArrayList<>();
        boolean allPassed = true;
        StringBuilder diagBuilder = new StringBuilder();

        for (EvalRule rule : scenario.getRules()) {
            RuleEvaluation eval = evaluateRule(rule, response, latencyMs);
            evaluations.add(eval);
            if (!eval.isPassed()) {
                allPassed = false;
                diagBuilder.append("- ").append(eval.getFailureReason()).append("\n");
            }
        }

        return EvalResult.builder()
                .scenarioId(scenario.getId())
                .scenarioName(scenario.getName())
                .endpoint(scenario.getEndpoint())
                .httpStatusCode(response.statusCode())
                .latencyMs(latencyMs)
                .passed(allPassed)
                .evaluations(evaluations)
                .diagnostics(diagBuilder.toString().trim())
                .build();
    }

    private RuleEvaluation evaluateRule(
            EvalRule rule, HttpResponse<String> response, long latencyMs) {
        String type = rule.getType() != null ? rule.getType().toLowerCase() : "";

        switch (type) {
            case "status_code":
            case "status":
                int expectedStatus =
                        rule.getExpected() instanceof Number
                                ? ((Number) rule.getExpected()).intValue()
                                : Integer.parseInt(rule.getExpected().toString());
                boolean statusOk = response.statusCode() == expectedStatus;
                return RuleEvaluation.builder()
                        .ruleType("status_code")
                        .description("Status Code is " + expectedStatus)
                        .passed(statusOk)
                        .expected(expectedStatus)
                        .actual(response.statusCode())
                        .failureReason(
                                statusOk
                                        ? null
                                        : "Expected HTTP status "
                                                + expectedStatus
                                                + " but got "
                                                + response.statusCode())
                        .build();

            case "latency":
            case "max_latency":
                long maxMs = rule.getMaxMs() != null ? rule.getMaxMs() : 500L;
                boolean latencyOk = latencyMs <= maxMs;
                return RuleEvaluation.builder()
                        .ruleType("latency")
                        .description("Response latency <= " + maxMs + "ms")
                        .passed(latencyOk)
                        .expected("<= " + maxMs + "ms")
                        .actual(latencyMs + "ms")
                        .failureReason(
                                latencyOk
                                        ? null
                                        : "Response latency "
                                                + latencyMs
                                                + "ms exceeded maximum SLA threshold of "
                                                + maxMs
                                                + "ms")
                        .build();

            case "header":
                String headerKey = rule.getField();
                String headerValue = response.headers().firstValue(headerKey).orElse(null);
                String expectedHeader =
                        rule.getExpected() != null ? rule.getExpected().toString() : "";
                boolean headerOk =
                        headerValue != null
                                && (headerValue.contains(expectedHeader)
                                        || Pattern.matches(expectedHeader, headerValue));
                return RuleEvaluation.builder()
                        .ruleType("header")
                        .description("Header '" + headerKey + "' matches '" + expectedHeader + "'")
                        .passed(headerOk)
                        .expected(expectedHeader)
                        .actual(headerValue)
                        .failureReason(
                                headerOk
                                        ? null
                                        : "Header '"
                                                + headerKey
                                                + "' was '"
                                                + headerValue
                                                + "', expected match: "
                                                + expectedHeader)
                        .build();

            case "required_fields":
                if (rule.getExpected() instanceof List<?> list) {
                    List<String> missing = new ArrayList<>();
                    for (Object fieldName : list) {
                        try {
                            Object val =
                                    JsonPath.read(response.body(), "$." + fieldName.toString());
                            if (val == null) {
                                missing.add(fieldName.toString());
                            }
                        } catch (Exception e) {
                            missing.add(fieldName.toString());
                        }
                    }
                    boolean requiredOk = missing.isEmpty();
                    return RuleEvaluation.builder()
                            .ruleType("required_fields")
                            .description("Response payload contains required fields: " + list)
                            .passed(requiredOk)
                            .expected("All fields present: " + list)
                            .actual(requiredOk ? "All present" : "Missing: " + missing)
                            .failureReason(
                                    requiredOk
                                            ? null
                                            : "Missing contract fields in response: " + missing)
                            .build();
                }
                break;

            case "json_path":
                String path = rule.getField();
                Object actualValue = null;
                try {
                    actualValue = JsonPath.read(response.body(), path);
                } catch (Exception e) {
                    return RuleEvaluation.builder()
                            .ruleType("json_path")
                            .description("JsonPath " + path)
                            .passed(false)
                            .expected(rule.getExpected())
                            .actual("JSON Path not found: " + e.getMessage())
                            .failureReason(
                                    "JsonPath "
                                            + path
                                            + " not found in body: "
                                            + truncate(response.body(), 120))
                            .build();
                }

                String op =
                        rule.getOperator() != null ? rule.getOperator().toLowerCase() : "equals";
                boolean pathOk = false;
                switch (op) {
                    case "equals":
                        pathOk =
                                actualValue != null
                                        && actualValue
                                                .toString()
                                                .equals(
                                                        rule.getExpected() != null
                                                                ? rule.getExpected().toString()
                                                                : null);
                        break;
                    case "contains":
                        pathOk =
                                actualValue != null
                                        && actualValue
                                                .toString()
                                                .contains(
                                                        rule.getExpected() != null
                                                                ? rule.getExpected().toString()
                                                                : "");
                        break;
                    case "matches":
                        pathOk =
                                actualValue != null
                                        && Pattern.matches(
                                                rule.getExpected().toString(),
                                                actualValue.toString());
                        break;
                    case "is_not_null":
                        pathOk = (actualValue != null);
                        break;
                    case "is_null":
                        pathOk = (actualValue == null);
                        break;
                    default:
                        pathOk = false;
                }

                return RuleEvaluation.builder()
                        .ruleType("json_path")
                        .description("JsonPath " + path + " " + op + " " + rule.getExpected())
                        .passed(pathOk)
                        .expected(rule.getExpected())
                        .actual(actualValue)
                        .failureReason(
                                pathOk
                                        ? null
                                        : "Rule violation at "
                                                + path
                                                + ": expected "
                                                + op
                                                + " '"
                                                + rule.getExpected()
                                                + "', but was '"
                                                + actualValue
                                                + "'")
                        .build();

            default:
                break;
        }

        return RuleEvaluation.builder()
                .ruleType(type)
                .description("Unknown or unhandled rule: " + type)
                .passed(true)
                .build();
    }

    public EvalReport runSuite(List<EvalScenario> scenarios) {
        List<EvalResult> results = new ArrayList<>();
        int passed = 0;
        long totalLatency = 0;

        for (EvalScenario s : scenarios) {
            EvalResult result = evaluateScenario(s);
            results.add(result);
            if (result.isPassed()) {
                passed++;
            }
            totalLatency += result.getLatencyMs();
        }

        int total = scenarios.size();
        double passRate = total > 0 ? ((double) passed / total) * 100.0 : 0.0;
        double avgLatency = total > 0 ? (double) totalLatency / total : 0.0;

        return EvalReport.builder()
                .targetBaseUrl(targetBaseUrl)
                .totalScenarios(total)
                .passedScenarios(passed)
                .failedScenarios(total - passed)
                .passRatePercentage(Math.round(passRate * 10.0) / 10.0)
                .avgLatencyMs(Math.round(avgLatency * 10.0) / 10.0)
                .results(results)
                .build();
    }

    public void renderConsoleReport(EvalReport report) {
        System.out.println(
                "\n"
                    + "================================================================================");
        System.out.println("                        SPRING BOOT EVAL BENCHMARK REPORT");
        System.out.println(
                "================================================================================");
        System.out.printf("Target Endpoint:  %s\n", report.getTargetBaseUrl());
        System.out.printf(
                "Scenarios Run:    %d total  |  %d passed  |  %d failed\n",
                report.getTotalScenarios(),
                report.getPassedScenarios(),
                report.getFailedScenarios());
        System.out.printf("Pass Rate:        %.1f%%\n", report.getPassRatePercentage());
        System.out.printf("Average Latency:  %.1f ms\n", report.getAvgLatencyMs());
        System.out.println(
                "--------------------------------------------------------------------------------");

        for (EvalResult r : report.getResults()) {
            String statusBadge = r.isPassed() ? "[ PASS ]" : "[ FAIL ]";
            System.out.printf(
                    "%s %-32s (%s %s) %dms\n",
                    statusBadge,
                    r.getScenarioName(),
                    r.getHttpStatusCode(),
                    r.getEndpoint(),
                    r.getLatencyMs());

            for (RuleEvaluation re : r.getEvaluations()) {
                String ruleIcon = re.isPassed() ? "  + " : "  x ";
                System.out.printf("%s%s\n", ruleIcon, re.getDescription());
                if (!re.isPassed() && re.getFailureReason() != null) {
                    System.out.printf("      DIAGNOSTIC: %s\n", re.getFailureReason());
                }
            }
            System.out.println();
        }

        System.out.println(
                "================================================================================");
        if (report.getFailedScenarios() > 0) {
            System.out.println(
                    "COURSE CORRECTION NEEDED: Review the failed rules above to update"
                            + " implementation.");
        } else {
            System.out.println(
                    "ALL EVAL RULES PASSED: Application conforms to contracts and rules.");
        }
        System.out.println(
                "================================================================================\n");
    }

    public void saveReportJson(EvalReport report, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        jsonMapper.writerWithDefaultPrettyPrinter().writeValue(outputPath.toFile(), report);
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return "null";
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
