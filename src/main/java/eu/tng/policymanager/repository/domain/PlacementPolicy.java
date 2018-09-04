/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.repository.domain;

import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
@Document(collection = "PlacementPolicy")
public class PlacementPolicy {

    @Id
    private String id;

    private UUID uuid;

    private String policy;

    private String[] datacenters;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPolicy() {
        return policy;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String[] getDatacenters() {
        return datacenters;
    }

    public void setDatacenters(String[] datacenters) {
        this.datacenters = datacenters;
    }

}
