/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.facts.action;

import eu.tng.policymanager.facts.enums.NetworkMechanismType;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class NetworkManagementAction extends Action {

    String virtualLinkid;

    NetworkMechanismType networkLinkFunction;

    public NetworkManagementAction(String service_instance_id, String virtualLinkid, NetworkMechanismType networkLinkFunction, String value) {
        this.service_instance_id = service_instance_id;
        this.virtualLinkid = virtualLinkid;
        this.networkLinkFunction = networkLinkFunction;
        this.value = value;
    }

    public String getVirtualLinkid() {
        return virtualLinkid;
    }

    public void setVirtualLinkid(String virtualLinkid) {
        this.virtualLinkid = virtualLinkid;
    }

    public NetworkMechanismType getNetworkLinkFunction() {
        return networkLinkFunction;
    }

    public void setNetworkLinkFunction(NetworkMechanismType networkLinkFunction) {
        this.networkLinkFunction = networkLinkFunction;
    }

    @Override
    public String toString() {
        return "NetworkManagementAction: { virtualLinkid=\"" + virtualLinkid + "\""
                + ", service_instance_id=" + service_instance_id
                + ", value=" + value
                + ",networkLinkFunction=\"" + networkLinkFunction + "\"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NetworkManagementAction that = (NetworkManagementAction) o;
        return this.value == that.value && this.service_instance_id.equals(that.service_instance_id) && this.virtualLinkid.equals(that.virtualLinkid) && this.networkLinkFunction.equals(that.networkLinkFunction);
    }

    @Override
    public int hashCode() {
        int result = networkLinkFunction.hashCode();
        result = (int) (31 * result + virtualLinkid.hashCode() +service_instance_id.hashCode());
        return result;
    }

}
