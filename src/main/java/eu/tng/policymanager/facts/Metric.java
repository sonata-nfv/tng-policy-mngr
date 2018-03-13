/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.facts;

import eu.tng.policymanager.facts.enums.AggregationFunctionType;
import eu.tng.policymanager.facts.enums.ValueType;
import eu.tng.policymanager.facts.enums.UnitType;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class Metric {

    String value;
    ValueType valueType;
    UnitType valueUnit;
    AggregationFunctionType aggregationFunctionType;

    public Metric() {
    }

    public Metric(String value, ValueType valueType, UnitType valueUnit, AggregationFunctionType aggregationFunctionType) {

        this.value = value;
        this.valueType = valueType;
        this.valueUnit = valueUnit;
        this.aggregationFunctionType = aggregationFunctionType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public UnitType getValueUnit() {
        return valueUnit;
    }

    public void setValueUnit(UnitType valueUnit) {
        this.valueUnit = valueUnit;
    }

}
