/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager;

import eu.tng.policymanager.repository.dao.RecommendedActionRepository;
import eu.tng.policymanager.rules.generation.RepositoryUtil;
import java.util.logging.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
//@RunWith(SpringJUnit4ClassRunner.class)

//@SpringBootTest(classes = Application.class)
//@WebAppConfiguration
public class TestPolicyActions {

    private static final Logger logger = Logger.getLogger(RepositoryUtil.class.getName());

    @Autowired
    RecommendedActionRepository recommendedActionRepository;

    @Test
    public void createAMockUpAction() {

        logger.info("createAMockUpAction");
//        RecommendedAction recommendedAction = new RecommendedAction();
//        
//        ElasticityAction doactionsubclass = new ElasticityAction("nsr_abcd", "vnf_name_squid-vnf", ScalingType.addvnf, "value_1", Status.send);
//
//        recommendedAction.setAction(doactionsubclass);
//        recommendedAction.setCorrelation_id("123456");
        //recommendedActionRepository.save(recommendedAction);
        //logger.info("num of recommended actions" + recommendedActionRepository.count());
    }

}
