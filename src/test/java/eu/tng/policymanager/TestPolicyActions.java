/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager;

import eu.tng.policymanager.repository.dao.RecommendedActionRepository;
import eu.tng.policymanager.rules.generation.RepositoryUtil;
import java.util.Arrays;
import java.util.List;
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

    
    //@Test
    public void testListOfPlacementPolicies() {
        String policy = "Fill First";
        List<String> myList = Arrays.asList("Prioritise", "Load Balanced", "Fill First");
        boolean containselement = myList.stream().anyMatch(str -> str.equals(policy));
        System.out.println("containselement "+containselement);
    }

   //@Test
    public void testStringSubSring() {
        String nsr_id = "6a72ddc9-36e5-4055-8c58-d691b39d4cc6";

        String rule_prefix = nsr_id.substring(0, Math.min(nsr_id.length(), 8));
        String rule_name = "haproxy-vnf:vdu01:haproxy_frontend_scur:less30".replace(":", "_").replace("-", "_") + "_" + rule_prefix;
        System.out.println("rule_name " + rule_name);

        String alertname = rule_name.substring(0, rule_name.length() - 9);

        System.out.println("alertname " + alertname);

    }

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
