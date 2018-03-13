/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.facts;

import eu.tng.policymanager.facts.action.Action;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class DoActionToComponent {

    private MonitoredComponent monitoredComponent;
    private Action action;

    public MonitoredComponent getMonitoredComponent() {
        return monitoredComponent;
    }

    public void setMonitoredComponent(MonitoredComponent monitoredComponent) {
        this.monitoredComponent = monitoredComponent;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public DoActionToComponent(MonitoredComponent monitoredComponent, Action action) {
        this.monitoredComponent = monitoredComponent;
        this.action = action;
    }
    
    @Override
    public String toString() {
        //return "DoAction: { \"monitoredComponent\":"+ monitoredComponent.getName()+",\"actiontype: \"" + actiontype + "\" }";
        return "DoAction: {" + monitoredComponent+ ","+ action+" }";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DoActionToComponent that = (DoActionToComponent) o;
        return this.action.equals(that.action) && this.monitoredComponent.equals(that.monitoredComponent);
    }

    @Override
    public int hashCode() {
        return monitoredComponent.hashCode();
    }

}
