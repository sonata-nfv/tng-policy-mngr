/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.repository.dao;

import eu.tng.policymanager.repository.domain.PlacementPolicy;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public interface PlacementPolicyRepository extends MongoRepository<PlacementPolicy, String> {

}
