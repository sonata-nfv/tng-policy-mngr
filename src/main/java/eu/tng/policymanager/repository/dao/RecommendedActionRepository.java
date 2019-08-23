/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.repository.dao;

import eu.tng.policymanager.repository.domain.RecommendedAction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public interface RecommendedActionRepository extends MongoRepository<RecommendedAction, String> {

    Optional<RecommendedAction> findTopByNsridOrderByInDateTimeDesc(String nsr_id);
    
    List<RecommendedAction> findAllByOrderByInDateTimeDesc();

}
