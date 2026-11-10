package com.example.demo.eval.model;

import java.util.ArrayList;
import java.util.List;

public class EvalResult {

    private String scenarioId;
    private String scenarioName;
    private String endpoint;
    private int httpStatusCode;
    private long latencyMs;
    private boolean passed;
    private List<RuleEvaluation> evaluations = new ArrayList<>();
    private String diagnostics;

    public EvalResult() {}

    public EvalResult(
            String scenarioId,
            String scenarioName,
            String endpoint,
            int httpStatusCode,
            long latencyMs,
            boolean passed,
            List<RuleEvaluation> evaluations,
            String diagnostics) {
        this.scenarioId = scenarioId;
        this.scenarioName = scenarioName;
        this.endpoint = endpoint;
        this.httpStatusCode = httpStatusCode;
        this.latencyMs = latencyMs;
        this.passed = passed;
        this.evaluations = evaluations != null ? evaluations : new ArrayList<>();
        this.diagnostics = diagnostics;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String scenarioId;
        private String scenarioName;
        private String endpoint;
        private int httpStatusCode;
        private long latencyMs;
        private boolean passed;
        private List<RuleEvaluation> evaluations = new ArrayList<>();
        private String diagnostics;

        public Builder scenarioId(String scenarioId) {
            this.scenarioId = scenarioId;
            return this;
        }

        public Builder scenarioName(String scenarioName) {
            this.scenarioName = scenarioName;
            return this;
        }

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder httpStatusCode(int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }

        public Builder latencyMs(long latencyMs) {
            this.latencyMs = latencyMs;
            return this;
        }

        public Builder passed(boolean passed) {
            this.passed = passed;
            return this;
        }

        public Builder evaluations(List<RuleEvaluation> evaluations) {
            this.evaluations = evaluations;
            return this;
        }

        public Builder diagnostics(String diagnostics) {
            this.diagnostics = diagnostics;
            return this;
        }

        public EvalResult build() {
            return new EvalResult(
                    scenarioId,
                    scenarioName,
                    endpoint,
                    httpStatusCode,
                    latencyMs,
                    passed,
                    evaluations,
                    diagnostics);
        }
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public List<RuleEvaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(List<RuleEvaluation> evaluations) {
        this.evaluations = evaluations;
    }

    public String getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(String diagnostics) {
        this.diagnostics = diagnostics;
    }
}
