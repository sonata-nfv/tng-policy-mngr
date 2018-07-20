/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.facts.action;

import eu.tng.policymanager.facts.enums.Status;
import eu.tng.policymanager.facts.enums.UnitType;
import eu.tng.policymanager.facts.enums.ValueType;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class Action {

    String ns_id;
    String nsr_id;
    String value;
    ValueType valueType;
    UnitType valueUnit;
    Status status;
    
    String correlation_id;

    public String getNs_id() {
        return ns_id;
    }

    public void setNs_id(String ns_id) {
        this.ns_id = ns_id;
    }

    public String getNsr_id() {
        return nsr_id;
    }

    public void setNsr_id(String nsr_id) {
        this.nsr_id = nsr_id;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCorrelation_id() {
        return correlation_id;
    }

    public void setCorrelation_id(String correlation_id) {
        this.correlation_id = correlation_id;
    }

    @Override
    public String toString() {
        return "Action: { valueType=\"" + valueType + "\""
                + ", ns_id=" + ns_id
                + ", nsr_id=" + nsr_id
                + ", value=" + value
                + ",valueUnit=\"" + valueUnit
                + ", status=" + status + "\"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Action that = (Action) o;
        return this.value == that.value
                && this.valueType.equals(that.valueType)
                && this.valueUnit.equals(that.valueUnit)
                && this.ns_id.equals(that.ns_id)
                && this.nsr_id.equals(that.nsr_id);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = (int) (31 * result + valueType.hashCode() + nsr_id.hashCode() + ns_id.hashCode());
        return result;
    }

}
