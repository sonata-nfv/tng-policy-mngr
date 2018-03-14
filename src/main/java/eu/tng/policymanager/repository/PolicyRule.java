/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyRule {

    @JsonProperty
    private String name;
    @JsonProperty
    private String salience;
    @JsonProperty
    private Inertia inertia;

    @JsonIgnore
    private Duration duration;

    @JsonIgnore
    private JsonProperty aggregation;

    @JsonProperty
    private RuleCondition conditions;

    @JsonProperty
    private List<Action> actions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSalience() {
        return salience;
    }

    public void setSalience(String salience) {
        this.salience = salience;
    }

    public Inertia getInertia() {
        return inertia;
    }

    public void setInertia(Inertia inertia) {
        this.inertia = inertia;
    }

    public RuleCondition getConditions() {
        return conditions;
    }

    public void setConditions(RuleCondition conditions) {
        this.conditions = conditions;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

}
