/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.facts.action;

import eu.tng.policymanager.facts.enums.ScalingType;
import eu.tng.policymanager.facts.enums.Status;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class ElasticityAction extends Action {

    String vnf_name;
    String vendor;
    String version;
    String criterion;

    String vnfd_id;
    String vim_id;
    ScalingType scaling_type;

    public ElasticityAction(String service_instance_id, String vnf_name, String vendor, String version, ScalingType scaling_type, String value, String criterion, Status status) {
        this.service_instance_id = service_instance_id;
        this.vnf_name = vnf_name;
        this.vendor = vendor;
        this.version = version;
        this.scaling_type = scaling_type;
        this.value = value;
        this.criterion = criterion;
        this.status = status;
    }

    public String getVnf_name() {
        return vnf_name;
    }

    public void setVnf_name(String vnf_name) {
        this.vnf_name = vnf_name;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCriterion() {
        return criterion;
    }

    public void setCriterion(String criterion) {
        this.criterion = criterion;
    }

    public ScalingType getScaling_type() {
        return scaling_type;
    }

    public void setScaling_type(ScalingType scaling_type) {
        this.scaling_type = scaling_type;
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
        return "ElasticityAction: { vnf_name=\"" + vnf_name + "\""
                + ", vendor=" + vendor
                + ", version=" + version
                + ", value=" + value
                + ", service_instance_id=" + service_instance_id
                + ", vnfd_id=" + vnfd_id
                + ", vim_id=" + vim_id
                + ", criterion=" + criterion
                + ", scaling_type=\"" + scaling_type + "\"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ElasticityAction that = (ElasticityAction) o;
        return this.value == that.value
                && this.service_instance_id.equals(that.service_instance_id)
                && this.vnf_name.equals(that.vnf_name)
                && this.vendor.equals(that.vendor)
                && this.version.equals(that.version);
                //&& this.vnfd_id.equals(that.vnfd_id)
                //&& this.vim_id.equals(that.vim_id)
                //&& this.scaling_type.equals(that.scaling_type)
                //&& this.criterion.equals(that.criterion)
                //&& this.status.equals(that.status);
    }

    @Override
    public int hashCode() {
        int result = scaling_type.hashCode();
        result = (int) (31 * result + vnf_name.hashCode() + vendor.hashCode() + version.hashCode() + service_instance_id.hashCode());
        return result;
    }

}
