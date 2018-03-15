/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.facts.action;

import eu.tng.policymanager.facts.enums.InfrastructureType;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class ComponentResourceAllocationAction extends Action {

    String componentid;
    InfrastructureType resourceAllocationType;

    public ComponentResourceAllocationAction(String componentid, InfrastructureType resourceAllocationType, String value) {
        this.componentid = componentid;
        this.resourceAllocationType = resourceAllocationType;
        this.value = value;
    }

    public String getComponentid() {
        return componentid;
    }

    public void setComponentid(String componentid) {
        this.componentid = componentid;
    }

    public InfrastructureType getResourceAllocationType() {
        return resourceAllocationType;
    }

    public void setResourceAllocationType(InfrastructureType resourceAllocationType) {
        this.resourceAllocationType = resourceAllocationType;
    }

    @Override
    public String toString() {
        return "ComponentResourceAllocationAction: { componentid=\"" + componentid + "\""
                + ", value=" + value
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
        return this.value == that.value && this.componentid.equals(that.componentid) && this.resourceAllocationType.equals(that.resourceAllocationType);
    }

    @Override
    public int hashCode() {
        int result = resourceAllocationType.hashCode();
        result = (int) (31 * result + componentid.hashCode());
        return result;
    }

}
