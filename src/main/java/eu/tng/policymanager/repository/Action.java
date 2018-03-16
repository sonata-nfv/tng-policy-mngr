/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Action {

    @JsonProperty
    private String action_object;

    @JsonProperty
    private String action_type;

    @JsonProperty
    private String name;

    @JsonProperty
    private String value;

    @JsonProperty
    private String target;

    public String getAction_object() {
        return action_object;
    }

    public void setAction_object(String action_object) {
        this.action_object = action_object;
    }

    public String getAction_type() {
        return action_type;
    }

    public void setAction_type(String action_type) {
        this.action_type = action_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

}
