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
public class LogMetric extends Metric {

    String gnsid;
    String componentid;

    public LogMetric(String gnsid, String componentid, String value) {
        this.gnsid = gnsid;
        this.componentid = componentid;
        this.value = value;
        this.valueType = ValueType.String;
        this.valueUnit = UnitType.none;
        this.aggregationFunctionType = AggregationFunctionType.none;
    }

    public String getGnsid() {
        return gnsid;
    }

    public void setGnsid(String gnsid) {
        this.gnsid = gnsid;
    }

    public String getComponentid() {
        return componentid;
    }

    public void setComponentid(String componentid) {
        this.componentid = componentid;
    }

    public AggregationFunctionType getAggregationFunctionType() {
        return aggregationFunctionType;
    }

    public void setAggregationFunctionType(AggregationFunctionType aggregationFunctionType) {
        this.aggregationFunctionType = aggregationFunctionType;
    }

    @Override
    public String toString() {
        return "LogMetric: { componentid=\"" + componentid + "\""
                + ", value=\"" + value+ "\""
                + ",gnsid=\"" + gnsid + "\"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogMetric that = (LogMetric) o;
        return this.value.equals(that.value) && this.gnsid.equals(that.gnsid) && this.componentid.equals(that.componentid);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = (int) (31 * result + componentid.hashCode() + gnsid.hashCode());
        return result;
    }

}
