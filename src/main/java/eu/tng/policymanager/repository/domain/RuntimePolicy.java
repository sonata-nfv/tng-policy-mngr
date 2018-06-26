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
@Document(collection = "RuntimePolicy")
public class RuntimePolicy {

    @Id
    private String id;

    private String policyid;
    
    private String nsrid;

    private String slaid;
    
    private boolean is_default;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isIs_default() {
        return is_default;
    }

    public void setIs_default(boolean is_default) {
        this.is_default = is_default;
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

    public String getSlaid() {
        return slaid;
    }

    public void setSlaid(String slaid) {
        this.slaid = slaid;
    }

}
