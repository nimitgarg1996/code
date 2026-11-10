package com.example.demo.eval.model;

import java.util.ArrayList;
import java.util.List;

public class EvalReport {

    private String targetBaseUrl;
    private int totalScenarios;
    private int passedScenarios;
    private int failedScenarios;
    private double passRatePercentage;
    private double avgLatencyMs;
    private List<EvalResult> results = new ArrayList<>();

    public EvalReport() {}

    public EvalReport(
            String targetBaseUrl,
            int totalScenarios,
            int passedScenarios,
            int failedScenarios,
            double passRatePercentage,
            double avgLatencyMs,
            List<EvalResult> results) {
        this.targetBaseUrl = targetBaseUrl;
        this.totalScenarios = totalScenarios;
        this.passedScenarios = passedScenarios;
        this.failedScenarios = failedScenarios;
        this.passRatePercentage = passRatePercentage;
        this.avgLatencyMs = avgLatencyMs;
        this.results = results != null ? results : new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String targetBaseUrl;
        private int totalScenarios;
        private int passedScenarios;
        private int failedScenarios;
        private double passRatePercentage;
        private double avgLatencyMs;
        private List<EvalResult> results = new ArrayList<>();

        public Builder targetBaseUrl(String targetBaseUrl) {
            this.targetBaseUrl = targetBaseUrl;
            return this;
        }

        public Builder totalScenarios(int totalScenarios) {
            this.totalScenarios = totalScenarios;
            return this;
        }

        public Builder passedScenarios(int passedScenarios) {
            this.passedScenarios = passedScenarios;
            return this;
        }

        public Builder failedScenarios(int failedScenarios) {
            this.failedScenarios = failedScenarios;
            return this;
        }

        public Builder passRatePercentage(double passRatePercentage) {
            this.passRatePercentage = passRatePercentage;
            return this;
        }

        public Builder avgLatencyMs(double avgLatencyMs) {
            this.avgLatencyMs = avgLatencyMs;
            return this;
        }

        public Builder results(List<EvalResult> results) {
            this.results = results;
            return this;
        }

        public EvalReport build() {
            return new EvalReport(
                    targetBaseUrl,
                    totalScenarios,
                    passedScenarios,
                    failedScenarios,
                    passRatePercentage,
                    avgLatencyMs,
                    results);
        }
    }

    public String getTargetBaseUrl() {
        return targetBaseUrl;
    }

    public void setTargetBaseUrl(String targetBaseUrl) {
        this.targetBaseUrl = targetBaseUrl;
    }

    public int getTotalScenarios() {
        return totalScenarios;
    }

    public void setTotalScenarios(int totalScenarios) {
        this.totalScenarios = totalScenarios;
    }

    public int getPassedScenarios() {
        return passedScenarios;
    }

    public void setPassedScenarios(int passedScenarios) {
        this.passedScenarios = passedScenarios;
    }

    public int getFailedScenarios() {
        return failedScenarios;
    }

    public void setFailedScenarios(int failedScenarios) {
        this.failedScenarios = failedScenarios;
    }

    public double getPassRatePercentage() {
        return passRatePercentage;
    }

    public void setPassRatePercentage(double passRatePercentage) {
        this.passRatePercentage = passRatePercentage;
    }

    public double getAvgLatencyMs() {
        return avgLatencyMs;
    }

    public void setAvgLatencyMs(double avgLatencyMs) {
        this.avgLatencyMs = avgLatencyMs;
    }

    public List<EvalResult> getResults() {
        return results;
    }

    public void setResults(List<EvalResult> results) {
        this.results = results;
    }
}
