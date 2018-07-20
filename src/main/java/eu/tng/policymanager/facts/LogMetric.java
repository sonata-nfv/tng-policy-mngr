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

    String nsrid;
    String vnf_name;
    String vnfd_id;
    String vim_id;

    public LogMetric(String nsrid, String vnf_name, String value, String vnfd_id, String vim_id) {
        this.nsrid = nsrid;
        this.vnf_name = vnf_name;
        this.vnfd_id = vnfd_id;
        this.vim_id = vim_id;
        this.value = value;
        this.valueType = ValueType.String;
        this.valueUnit = UnitType.none;
        this.aggregationFunctionType = AggregationFunctionType.none;
    }

    public String getNsrid() {
        return nsrid;
    }

    public void setNsrid(String nsrid) {
        this.nsrid = nsrid;
    }

    public String getVnf_name() {
        return vnf_name;
    }

    public void setVnf_name(String vnf_name) {
        this.vnf_name = vnf_name;
    }

    public AggregationFunctionType getAggregationFunctionType() {
        return aggregationFunctionType;
    }

    public void setAggregationFunctionType(AggregationFunctionType aggregationFunctionType) {
        this.aggregationFunctionType = aggregationFunctionType;
    }

    public String getVnfd_id() {
        return vnfd_id;
    }

    public void setVnfd_id(String vnfd_id) {
        this.vnfd_id = vnfd_id;
    }

    public String getVim_id() {
        return vim_id;
    }

    public void setVim_id(String vim_id) {
        this.vim_id = vim_id;
    }
    

    @Override
    public String toString() {
        return "LogMetric: { vnf_name=\"" + vnf_name + "\""
                + ", value=\"" + value + "\""
                + ", vnfd_id=\"" + vnfd_id + "\""
                + ", vim_id=\"" + vim_id + "\""
                + ",nsrid=\"" + nsrid + "\"}";
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
        return this.value.equals(that.value) 
                && this.nsrid.equals(that.nsrid) 
                && this.vnf_name.equals(that.vnf_name)
                && this.vnfd_id.equals(that.vnfd_id)
                && this.vim_id.equals(that.vim_id);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = (int) (31 * result + vnf_name.hashCode() + nsrid.hashCode()+ vnfd_id.hashCode()+ vim_id.hashCode());
        return result;
    }

}
