/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class RuleCondition {

    @JsonProperty
    private String condition;

    @JsonProperty
    private List<Expression> rules;
    


    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public List<Expression> getRules() {
        return rules;
    }

    public void setRules(List<Expression> rules) {
        this.rules = rules;
    }

}
