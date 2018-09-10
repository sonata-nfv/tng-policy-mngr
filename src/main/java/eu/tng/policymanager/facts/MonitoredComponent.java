/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.facts;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class MonitoredComponent {

    private String name;
    private String metric;
    private double value;
    private String groundedGraphid;
    private String nsrid;
    private String vnf_id;
    private String vnfd_id;
    private String vim_id;

    public MonitoredComponent(String name, String metric, double value, String groundedGraphid, String nsrid, String vnf_id, String vnfd_id, String vim_id) {
        this.name = name;
        this.metric = metric;
        this.value = value;
        this.nsrid = nsrid;
        this.groundedGraphid = groundedGraphid;
        this.vnf_id = vnf_id;
        this.vnfd_id = vnfd_id;
        this.vim_id = vim_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getGroundedGraphid() {
        return groundedGraphid;
    }

    public void setGroundedGraphid(String groundedGraphid) {
        this.groundedGraphid = groundedGraphid;
    }

    public String getNsrid() {
        return nsrid;
    }

    public void setNsrid(String nsrid) {
        this.nsrid = nsrid;
    }

    public String getVnf_id() {
        return vnf_id;
    }

    public void setVnf_id(String vnf_id) {
        this.vnf_id = vnf_id;
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
        return "MonitoredComponent: { name=\"" + name + "\""
                + ",metric=\"" + metric + "\""
                + ", value=" + value
                + ", nsrid==\"" + nsrid + "\""
                + ", vnf_id==\"" + vnf_id + "\""
                + ", vnfd_id==\"" + vnfd_id + "\""
                + ", vim_id==\"" + vim_id + "\""
                + ",groundedGraphid=\"" + groundedGraphid + "\"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MonitoredComponent that = (MonitoredComponent) o;
        return this.value == that.value
                && this.name.equals(that.name)
                && this.nsrid.equals(that.nsrid)
                && this.groundedGraphid.equals(that.groundedGraphid);
    }

    @Override
    public int hashCode() {
        int result = groundedGraphid.hashCode();
        result = (int) (31 * result + value);
        return result;
    }

}
