/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import eu.tng.policymanager.repository.PolicyRule;
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
    private List<PolicyRule> policyRules;

    public PolicyYamlFile readYaml(final File file) {
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

}
