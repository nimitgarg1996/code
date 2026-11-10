package com.example.demo.eval.contract;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import com.example.demo.Application;
import com.example.demo.eval.engine.EvalRunner;
import com.example.demo.eval.model.EvalReport;
import com.example.demo.eval.model.EvalResult;
import com.example.demo.eval.model.EvalScenario;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiContractTest {

    @LocalServerPort private int port;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        String customUrl = System.getProperty("eval.target.url");
        if (customUrl != null && !customUrl.isBlank()) {
            this.baseUrl = customUrl;
        } else {
            this.baseUrl = "http://localhost:" + port;
        }
        RestAssured.baseURI = this.baseUrl;
    }

    @Test
    @DisplayName("OpenAPI 3.1 Spec Endpoint is exposed and valid JSON")
    void openApiSpecIsExposed() {
        given().when().get("/api-docs").then().statusCode(200).contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("Contract Test: Greetings endpoint matches OpenAPI 3.1 specification")
    void greetingsEndpointConformsToOpenApiContract() {
        OpenApiValidationFilter validationFilter =
                new OpenApiValidationFilter(baseUrl + "/api-docs");

        given().filter(validationFilter)
                .queryParam("name", "Alice")
                .when()
                .get("/api/v1/greetings")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Eval Suite: Execute all declarative YAML eval benchmarks")
    void executeDeclarativeEvalSuites() throws Exception {
        EvalRunner runner = new EvalRunner(baseUrl);
        List<EvalScenario> scenarios = runner.loadScenariosFromDirectory(Paths.get("evals"));
        assertThat(scenarios).isNotEmpty();

        EvalReport report = runner.runSuite(scenarios);
        runner.renderConsoleReport(report);

        for (EvalResult result : report.getResults()) {
            assertThat(result.isPassed())
                    .withFailMessage(
                            "Scenario '%s' failed eval rules:\n%s",
                            result.getScenarioName(), result.getDiagnostics())
                    .isTrue();
        }
    }
}
