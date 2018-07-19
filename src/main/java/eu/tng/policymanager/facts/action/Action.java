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

    String nsid;
    String nsrid;
    String value;
    ValueType valueType;
    UnitType valueUnit;
    Status status;

    public String getNsid() {
        return nsid;
    }

    public void setNsid(String nsid) {
        this.nsid = nsid;
    }

    public String getNsrid() {
        return nsrid;
    }

    public void setNsrid(String nsrid) {
        this.nsrid = nsrid;
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

    @Override
    public String toString() {
        return "Action: { valueType=\"" + valueType + "\""
                + ", nsid=" + nsid
                + ", nsrid=" + nsrid
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
                && this.nsid.equals(that.nsid)
                && this.nsrid.equals(that.nsrid);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = (int) (31 * result + valueType.hashCode() + nsrid.hashCode() + nsid.hashCode());
        return result;
    }

}
