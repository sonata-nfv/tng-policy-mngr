/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.transferobjects;

import eu.tng.policymanager.facts.enums.MetricValueTypes;
import eu.tng.policymanager.facts.enums.MonitoringTypes;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class MonitoringMessageTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String metricName;

    private String unitOfMeasurement;

    private String metricValue;

    //MetricValueTypes {Integer,Float,String}
    private String metricValueType;

    //This refers to Unix Timestamp from the grounded component that the metricvalue was observed
    private Date unixtimestamp;

    //Grounded Service Graph ID
    private String gsgid;

    private String nsrid;

    //Grounded Component ID (id of neo4j entity)
    private Long gcid;

    private String cnid;

    private String privateIP;

    private String publicIP;

    private Long IaaSProviderId;

    //MonitoringTypes{    USER_DEFINED_METRICS, VM_METRICS}
    private String monitorigType;

    private String nodeid;

    public void setMetricValueType(MetricValueTypes metricValueType) {
        this.metricValueType = metricValueType.name();
    }

    public void setMonitorigType(MonitoringTypes monitorigType) {
        this.monitorigType = monitorigType.name();
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public void setUnitOfMeasurement(String unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
    }

    public String getGsgid() {
        return gsgid;
    }

    public void setGsgid(String gsgid) {
        this.gsgid = gsgid;
    }

    public Long getGcid() {
        return gcid;
    }

    public void setGcid(Long gcid) {
        this.gcid = gcid;
    }

    public String getCnid() {
        return cnid;
    }

    public void setCnid(String cnid) {
        this.cnid = cnid;
    }

    public String getPrivateIP() {
        return privateIP;
    }

    public void setPrivateIP(String privateIP) {
        this.privateIP = privateIP;
    }

    public String getPublicIP() {
        return publicIP;
    }

    public void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }

    public Long getIaaSProviderId() {
        return IaaSProviderId;
    }

    public void setIaaSProviderId(Long IaaSProviderId) {
        this.IaaSProviderId = IaaSProviderId;
    }

    public String getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(String metricValue) {
        this.metricValue = metricValue;
    }

    public Date getUnixtimestamp() {
        return unixtimestamp;
    }

    public void setUnixtimestamp(Date unixtimestamp) {
        this.unixtimestamp = unixtimestamp;
    }

    public String getNodeid() {
        return nodeid;
    }

    public void setNodeid(String nodeid) {
        this.nodeid = nodeid;
    }

    public String getNsrid() {
        return nsrid;
    }

    public void setNsrid(String nsrid) {
        this.nsrid = nsrid;
    }
    

    @Override
    public String toString() {
        return "MonitoringMessage{"
                + "metricName='" + metricName + '\''
                + ", unitOfMeasurement='" + unitOfMeasurement + '\''
                + ", metricValue='" + metricValue + '\''
                + ", metricValueType='" + metricValueType + '\''
                + ", unixtimestamp=" + unixtimestamp
                + ", gsgid='" + gsgid + '\''
                + ", nsrid='" + nsrid + '\''
                + ", gcid=" + gcid
                + ", cnid='" + cnid + '\''
                + ", privateIP='" + privateIP + '\''
                + ", publicIP='" + publicIP + '\''
                + ", IaaSProviderId=" + IaaSProviderId
                + ", monitorigType='" + monitorigType + '\''
                + ", nodeid='" + nodeid + '\''
                + '}';
    }
}
