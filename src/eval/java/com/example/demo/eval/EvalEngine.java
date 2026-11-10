package com.example.demo.eval;

import com.example.demo.eval.engine.EvalRunner;
import com.example.demo.eval.model.EvalReport;
import com.example.demo.eval.model.EvalScenario;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class EvalEngine {

    public static void main(String[] args) throws Exception {
        String targetUrl = args.length > 0 ? args[0] : "http://localhost:8080";
        String evalsDir = args.length > 1 ? args[1] : "evals";

        System.out.println("Starting Eval Engine against: " + targetUrl);
        System.out.println("Loading eval suites from:     " + evalsDir);

        EvalRunner runner = new EvalRunner(targetUrl);
        List<EvalScenario> scenarios = runner.loadScenariosFromDirectory(Paths.get(evalsDir));

        if (scenarios.isEmpty()) {
            System.err.println("No eval scenarios found in directory: " + evalsDir);
            System.exit(1);
        }

        EvalReport report = runner.runSuite(scenarios);
        runner.renderConsoleReport(report);

        Path reportPath = Paths.get("build/reports/evals/eval-summary.json");
        runner.saveReportJson(report, reportPath);
        System.out.println("Saved machine-readable eval report to: " + reportPath.toAbsolutePath());

        if (report.getFailedScenarios() > 0) {
            System.exit(1);
        }
    }
}
