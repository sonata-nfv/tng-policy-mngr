/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class FullExpression {

    @JsonProperty
    private Optional<LogicalOperator> condition;

    @JsonProperty
    private String id;
    @JsonProperty
    private String field;
    @JsonProperty
    private String type;
    @JsonProperty
    private String input;
    @JsonProperty
    private String operator;
    @JsonProperty
    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Optional<LogicalOperator> getCondition() {
        return condition;
    }

    public void setCondition(Optional<LogicalOperator> condition) {
        this.condition = condition;
    }
    

}
