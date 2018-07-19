/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.facts.action;

import eu.tng.policymanager.facts.enums.OrchestrationType;
import eu.tng.policymanager.facts.enums.Status;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class ElasticityAction extends Action {

    String vnf_name;
    OrchestrationType orchestrationType;

    public ElasticityAction(String nsrid, String vnf_name, OrchestrationType orchestrationType, String value, Status status) {
        this.nsrid = nsrid;
        this.vnf_name = vnf_name;
        this.orchestrationType = orchestrationType;
        this.value = value;
        this.status = status;
    }

    public String getVnf_name() {
        return vnf_name;
    }

    public void setVnf_name(String vnf_name) {
        this.vnf_name = vnf_name;
    }

    public OrchestrationType getOrchestrationType() {
        return orchestrationType;
    }

    public void setOrchestrationType(OrchestrationType orchestrationType) {
        this.orchestrationType = orchestrationType;
    }


    @Override
    public String toString() {
        return "ElasticityAction: { vnf_name=\"" + vnf_name + "\""
                + ", value=" + value
                + ", nsrid=" + nsrid
                + ",orchestrationType=\"" + orchestrationType + "\"}";
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
        return this.value == that.value && this.nsrid.equals(that.nsrid)
                && this.vnf_name.equals(that.vnf_name)
                && this.orchestrationType.equals(that.orchestrationType)
                && this.status.equals(that.status);
    }

    @Override
    public int hashCode() {
        int result = orchestrationType.hashCode();
        result = (int) (31 * result + vnf_name.hashCode() + nsrid.hashCode());
        return result;
    }

}
