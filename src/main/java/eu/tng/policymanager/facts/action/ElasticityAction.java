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

    String componentid;
    OrchestrationType orchestrationType;

    public ElasticityAction(String gnsid, String componentid, OrchestrationType orchestrationType, String value, Status status) {
        this.gnsid = gnsid;
        this.componentid = componentid;
        this.orchestrationType = orchestrationType;
        this.value = value;
        this.status = status;
    }

    public String getComponentid() {
        return componentid;
    }

    public void setComponentid(String componentid) {
        this.componentid = componentid;
    }

    public OrchestrationType getOrchestrationType() {
        return orchestrationType;
    }

    public void setOrchestrationType(OrchestrationType orchestrationType) {
        this.orchestrationType = orchestrationType;
    }


    @Override
    public String toString() {
        return "ComponentResourceAllocationAction: { componentid=\"" + componentid + "\""
                + ", value=" + value
                + ", gnsid=" + gnsid
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
        return this.value == that.value && this.gnsid.equals(that.gnsid)
                && this.componentid.equals(that.componentid)
                && this.orchestrationType.equals(that.orchestrationType)
                && this.status.equals(that.status);
    }

    @Override
    public int hashCode() {
        int result = orchestrationType.hashCode();
        result = (int) (31 * result + componentid.hashCode() + gnsid.hashCode());
        return result;
    }

}
