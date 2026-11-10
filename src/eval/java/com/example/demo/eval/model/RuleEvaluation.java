package com.example.demo.eval.model;

public class RuleEvaluation {

    private String ruleType;
    private String description;
    private boolean passed;
    private Object expected;
    private Object actual;
    private String failureReason;

    public RuleEvaluation() {}

    public RuleEvaluation(
            String ruleType,
            String description,
            boolean passed,
            Object expected,
            Object actual,
            String failureReason) {
        this.ruleType = ruleType;
        this.description = description;
        this.passed = passed;
        this.expected = expected;
        this.actual = actual;
        this.failureReason = failureReason;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String ruleType;
        private String description;
        private boolean passed;
        private Object expected;
        private Object actual;
        private String failureReason;

        public Builder ruleType(String ruleType) {
            this.ruleType = ruleType;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder passed(boolean passed) {
            this.passed = passed;
            return this;
        }

        public Builder expected(Object expected) {
            this.expected = expected;
            return this;
        }

        public Builder actual(Object actual) {
            this.actual = actual;
            return this;
        }

        public Builder failureReason(String failureReason) {
            this.failureReason = failureReason;
            return this;
        }

        public RuleEvaluation build() {
            return new RuleEvaluation(
                    ruleType, description, passed, expected, actual, failureReason);
        }
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public Object getExpected() {
        return expected;
    }

    public void setExpected(Object expected) {
        this.expected = expected;
    }

    public Object getActual() {
        return actual;
    }

    public void setActual(Object actual) {
        this.actual = actual;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
