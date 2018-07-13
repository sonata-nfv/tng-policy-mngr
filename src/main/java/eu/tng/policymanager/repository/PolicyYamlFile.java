/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class PolicyYamlFile {

    @JsonProperty
    private String name;

    @JsonProperty
    private String descriptor_schema;

    @JsonProperty
    private String vendor;

    @JsonProperty
    private String version;

    @JsonProperty
    private NetworkService network_service;

    @JsonProperty
    private List<MonitoringRule> monitoring_rules;

    @JsonProperty
    private List<PolicyRule> policyRules;

    public static PolicyYamlFile readYaml(final File file) {
        PolicyYamlFile policyYamlFile = null;
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); // jackson databind
        try {

            policyYamlFile = mapper.readValue(file, PolicyYamlFile.class);
        } catch (IOException ex) {
            Logger.getLogger(PolicyYamlFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        return policyYamlFile;
    }

    public List<PolicyRule> getPolicyRules() {
        return policyRules;
    }

    public void setPolicyRules(List<PolicyRule> policyRules) {
        this.policyRules = policyRules;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescriptor_schema() {
        return descriptor_schema;
    }

    public void setDescriptor_schema(String descriptor_schema) {
        this.descriptor_schema = descriptor_schema;
    }

    public NetworkService getNetwork_service() {
        return network_service;
    }

    public void setNetwork_service(NetworkService network_service) {
        this.network_service = network_service;
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

    public List<MonitoringRule> getMonitoring_rules() {
        return monitoring_rules;
    }

    public void setMonitoring_rules(List<MonitoringRule> monitoring_rules) {
        this.monitoring_rules = monitoring_rules;
    }

}
