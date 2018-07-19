/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.facts.action;

import eu.tng.policymanager.facts.enums.InfrastructureType;
import eu.tng.policymanager.facts.enums.Status;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class ComponentResourceAllocationAction extends Action {

    String vnf_name;
    InfrastructureType resourceAllocationType;

    public ComponentResourceAllocationAction(String nsrid, String vnf_name, InfrastructureType resourceAllocationType, String value, Status status) {
        this.nsrid = nsrid;
        this.vnf_name = vnf_name;
        this.resourceAllocationType = resourceAllocationType;
        this.value = value;
        this.status = status;
    }

    public String getVnf_name() {
        return vnf_name;
    }

    public void setVnf_name(String vnf_name) {
        this.vnf_name = vnf_name;
    }

    public InfrastructureType getResourceAllocationType() {
        return resourceAllocationType;
    }

    public void setResourceAllocationType(InfrastructureType resourceAllocationType) {
        this.resourceAllocationType = resourceAllocationType;
    }

    @Override
    public String toString() {
        return "ComponentResourceAllocationAction: { vnf_name=\"" + vnf_name + "\""
                + ", value=" + value
                + ", nsrid=" + nsrid
                + ",resourceAllocationType=\"" + resourceAllocationType + "\"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ComponentResourceAllocationAction that = (ComponentResourceAllocationAction) o;
        return this.value == that.value && this.nsrid.equals(that.nsrid) 
                && this.vnf_name.equals(that.vnf_name) 
                && this.resourceAllocationType.equals(that.resourceAllocationType)
                && this.status.equals(that.status);
    }

    @Override
    public int hashCode() {
        int result = resourceAllocationType.hashCode();
        result = (int) (31 * result + vnf_name.hashCode() + nsrid.hashCode());
        return result;
    }

}


