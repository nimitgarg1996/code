package com.example.demo.eval.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EvalRule {

    private String type;
    private String field;
    private String operator;
    private Object expected;
    private Long maxMs;

    public EvalRule() {}

    public EvalRule(String type, String field, String operator, Object expected, Long maxMs) {
        this.type = type;
        this.field = field;
        this.operator = operator;
        this.expected = expected;
        this.maxMs = maxMs;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Object getExpected() {
        return expected;
    }

    public void setExpected(Object expected) {
        this.expected = expected;
    }

    public Long getMaxMs() {
        return maxMs;
    }

    public void setMaxMs(Long maxMs) {
        this.maxMs = maxMs;
    }
}
