/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.facts.action;

import eu.tng.policymanager.facts.enums.LogMessage;
import eu.tng.policymanager.facts.enums.Status;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class AlertAction extends Action {

    String vnf_name;
    String vendor;
    String version;
    String criterion;
    int inertia;
    String vnfd_id;
    String vim_id;
    LogMessage logMessage;

    public AlertAction(String service_instance_id, String vnf_name, String vendor, String version, LogMessage logMessage, String value, String criterion, int inertia, Status status) {
        this.service_instance_id = service_instance_id;
        this.vnf_name = vnf_name;
        this.vendor = vendor;
        this.version = version;
        this.logMessage = logMessage;
        this.value = value;
        this.criterion = criterion;
        this.inertia = inertia;
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

    public int getInertia() {
        return inertia;
    }

    public String getCriterion() {
        return criterion;
    }

    public void setCriterion(String criterion) {
        this.criterion = criterion;
    }

    public void setInertia(int inertia) {
        this.inertia = inertia;
    }

    public LogMessage getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(LogMessage logMessage) {
        this.logMessage = logMessage;
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
                + ", logMessage=" + logMessage
                + ", criterion=" + criterion
                + ", status=" + status
                + ", inertia=" + inertia + "\"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AlertAction that = (AlertAction) o;
        return this.value == that.value
                && this.service_instance_id.equals(that.service_instance_id)
                && this.vnf_name.equals(that.vnf_name)
                && this.vendor.equals(that.vendor)
                && this.version.equals(that.version)
                && this.logMessage.equals(that.logMessage)
                && this.status.equals(that.status);
    }

    @Override
    public int hashCode() {
        int result = logMessage.hashCode();
        result = (int) (31 * result + vnf_name.hashCode() + vendor.hashCode() + version.hashCode() + service_instance_id.hashCode());
        return result;
    }

}