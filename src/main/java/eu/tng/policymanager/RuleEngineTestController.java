package eu.tng.policymanager;

import eu.tng.policymanager.Exceptions.NSDoesNotExistException;
import eu.tng.policymanager.Exceptions.VNFRDoesNotExistException;
import eu.tng.policymanager.facts.LogMetric;
import eu.tng.policymanager.facts.action.ElasticityAction;
import eu.tng.policymanager.facts.enums.ScalingType;
import eu.tng.policymanager.facts.enums.Status;
import eu.tng.policymanager.repository.dao.RecommendedActionRepository;
import eu.tng.policymanager.repository.domain.RecommendedAction;
import eu.tng.policymanager.rules.generation.Util;
import java.util.Date;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
@RestController
@RequestMapping("/test/api/v1")
public class RuleEngineTestController {

    private static final Logger log = LoggerFactory.getLogger(RuleEngineTestController.class);

    @Autowired
    RulesEngineService rulesEngineService;

    @Autowired
    RecommendedActionRepository recommendedActionRepository;

    @Autowired
    RepositoryConnector repositoryConnector;

    @Autowired
    private RabbitTemplate template;

    @Qualifier("runtimeActionsQueue")
    @Autowired
    private Queue queue;

    @Autowired
    private TopicExchange exchange;

    @Value("${tng.cat.policies}")
    private String policies_url;

    //usefull for testing drools
    @RequestMapping(value = "/newLogMetric", method = RequestMethod.POST)
    public boolean newMonitoringMessage(@RequestBody String tobject) {
        //test log metric expiration
        JSONObject request = new JSONObject(tobject);
        //LogMetric logMetric1 = new LogMetric("pilotTranscodingService", "vnf1", "mon_rule_vm_cpu_perc", "123", "456");
        LogMetric logMetric1 = new LogMetric(request.getString("nsrid"),
                request.getString("vnf_name"),
                request.getString("value"),
                request.getString("vnfd_id"),
                request.getString("vim_id"));

        log.info("create log fact " + logMetric1.toString());
        rulesEngineService.createLogFact(logMetric1);
        //end of test
        return true;
    }

    //usefull for rabbitmq message format
    @RequestMapping(value = "/newElasticityAction", method = RequestMethod.GET)
    public boolean generateElasticityAction() {

        log.info("createAMockUpAction");
        RecommendedAction recommendedAction = new RecommendedAction();

        ElasticityAction doactionsubclass = new ElasticityAction("fe01cf76-c333-4eea-93a5-9bf182649c8a", "squid-vnf", "eu.tango", "0.1", ScalingType.addvnf, "value_1", null, Status.send);

        recommendedAction.setAction(doactionsubclass);
        recommendedAction.setInDateTime(new Date());
        recommendedAction.setCorrelation_id("7895");
        recommendedActionRepository.save(recommendedAction);

        RecommendedAction recommendedAction1 = new RecommendedAction();
        ElasticityAction doactionsubclass1 = new ElasticityAction("fe01cf76-c910-4lkl-93a5-6bf182649c8a", "squid-vnf", "eu.tango", "0.1", ScalingType.removevnf, "value_1", "random", Status.send);

        recommendedAction1.setAction(doactionsubclass1);
        recommendedAction1.setInDateTime(new Date());
        recommendedAction1.setCorrelation_id("12345");
        recommendedActionRepository.save(recommendedAction1);

        return true;
    }

    //usefull for testing scale out action
    @RequestMapping(value = "/scale_out", method = RequestMethod.POST)
    public boolean generateScaleoutAction(@RequestBody String tobject
    ) {
        JSONObject request = new JSONObject(tobject);

        JSONObject elasticity_action_msg = new JSONObject();
        elasticity_action_msg.put("vnf_name", request.getString("vnf_name"));
        elasticity_action_msg.put("vnfd_id", request.getString("vnfd_id"));
        elasticity_action_msg.put("scaling_type", request.getString("scaling_type"));
        elasticity_action_msg.put("service_instance_id", request.getString("service_instance_id"));
        //elasticity_action_msg.put("correlation_id", correlation_id);
        elasticity_action_msg.put("value", request.getString("value"));
        JSONArray constraints = new JSONArray();
        JSONObject constraint = new JSONObject();
        constraint.put("vim_id", request.getString("vim_id"));
        constraints.put(constraint);

        elasticity_action_msg.put("constraints", constraints);
        CorrelationData cd = new CorrelationData();

        String correlation_id = request.getString("correlation_id");
        cd.setId(correlation_id);

        // template.convertAndSend(queue.getName(), elasticity_action_msg, cd);
        String elasticity_action_msg_as_yml = Util.jsonToYaml(elasticity_action_msg);

        //template.convertAndSend(exchange.getName(), queue.getName(), elasticity_action_msg_as_yml, cd);
        template.convertAndSend(exchange.getName(), queue.getName(), elasticity_action_msg_as_yml, m -> {
            //m.getMessageProperties().getHeaders().put("foo", "bar");
            m.getMessageProperties().setAppId("tng-policy-mngr");
            m.getMessageProperties().setReplyTo(queue.getName());
            m.getMessageProperties().setCorrelationId(correlation_id);
            return m;
        });

        System.out.println(" [x] Sent mock action to topic '" + elasticity_action_msg_as_yml + "'");
        return true;
    }

    //activate an enforced policy
    @RequestMapping(value = "/activate/{nsr_id}/{runtimepolicy_id}", method = RequestMethod.GET)
    public boolean activate(@PathVariable("nsr_id") String nsr_id, @PathVariable("runtimepolicy_id") String runtimepolicy_id) {

        //1. Fech yml file from catalogues
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(policies_url + "/" + runtimepolicy_id, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException e) {
            log.warn("{\"error\": \"The PLD ID " + runtimepolicy_id + " does not exist at catalogues. Message : "
                    + e.getMessage() + "\"}");
            return false;
        }

        JSONObject policydescriptorRaw = new JSONObject(response.getBody());
        log.info("response" + policydescriptorRaw.toString());

        JSONObject pld = policydescriptorRaw.getJSONObject("pld");

        String policyAsYaml = Util.jsonToYaml(pld);
        rulesEngineService.addNewKnowledgebase(nsr_id, runtimepolicy_id, policyAsYaml);
        return true;
    }

    @RequestMapping(value = "/service/{nsr_id}/vnfd/{vnfd_id}", method = RequestMethod.GET)
    public String fetchRandomlyVNF(@PathVariable("nsr_id") String nsr_id, @PathVariable("vnfd_id") String vnfd_id) {

        try {
            return repositoryConnector.get_vnfr_id_to_remove_random(nsr_id, vnfd_id);
        } catch (NSDoesNotExistException | VNFRDoesNotExistException ex) {
            java.util.logging.Logger.getLogger(RuleEngineTestController.class.getName()).log(Level.SEVERE, null, ex);
            return null;

        }

    }
    
}
