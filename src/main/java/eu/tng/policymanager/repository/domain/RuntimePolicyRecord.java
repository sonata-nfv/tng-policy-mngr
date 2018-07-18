/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.repository.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
@Document(collection = "RuntimePolicyRecord")
public class RuntimePolicyRecord {

    @Id
    private String id;

    private String policyid;
    
    private String nsrid;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

  
    public String getPolicyid() {
        return policyid;
    }

    public void setPolicyid(String policyid) {
        this.policyid = policyid;
    }

    public String getNsrid() {
        return nsrid;
    }

    public void setNsrid(String nsrid) {
        this.nsrid = nsrid;
    }

}
