package eu.tng.policymanager;

import eu.tng.policymanager.repository.dao.RuntimePolicyRepository;
import eu.tng.policymanager.repository.domain.RuntimePolicy;
import eu.tng.policymanager.response.BasicResponseCode;
import eu.tng.policymanager.response.PolicyRestResponse;
import eu.tng.policymanager.transferobjects.MonitoringMessageTO;
import java.util.Optional;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
@RestController
@RequestMapping("/api/v1")
public class RulesEngineController {
    
    private static final Logger log = LoggerFactory.getLogger(RulesEngineController.class);
    
    @Autowired
    RulesEngineService rulesEngineService;
    
    @Value("${tng.cat.policies}")
    private String policies_url;
    
    @Value("${github.repo}")
    private String github_repo;
    
    @Autowired
    RuntimePolicyRepository runtimePolicyRepository;
    
    @RequestMapping(value = "/newMonitoringMessage", method = RequestMethod.POST)
    public boolean newMonitoringMessage(@RequestBody MonitoringMessageTO tobject) {
        rulesEngineService.createFact(tobject);
        return true;
    }
    
    @RequestMapping(value = "", method = RequestMethod.GET)
    public PolicyRestResponse getInfoPolicyManager() {
        return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICIES_INFO, github_repo);
    }

    //TODO: clean java headers!!!!!!!!!!!!!!!
    @RequestMapping(value = "", method = RequestMethod.POST)
    public PolicyRestResponse savePolicyDescriptor(@RequestBody String tobject) {
        //save to catalogues
        log.info("i call catalogues with spring rest template so as to create the policy descriptor");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        //httpHeaders.set("Content-Type", "application/json");
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        //httpHeaders.add("Accept", MediaType.APPLICATION_JSON.toString());
        
        HttpEntity<String> httpEntity = new HttpEntity<>(tobject, httpHeaders);
        
        String responseone = null;
        try {
            responseone = restTemplate.postForObject(policies_url, httpEntity, String.class);
            
            JSONObject policyDescriptor = new JSONObject(responseone);
            String policy_uuid = policyDescriptor.getString("uuid");
            //save locally
            rulesEngineService.savePolicyDescriptor(tobject, policy_uuid);
            
        } catch (Exception e) {
            //log.info(e.getMessage());
            return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_CREATED_FAILURE, "Failed : HTTP error code : " + responseone
                    + ". Check if policy vendor or version are null");
        }
        
        return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_CREATED, responseone);
        
    }
    
    @RequestMapping(value = "/{policy_descriptor_uuid}", method = RequestMethod.DELETE)
    public PolicyRestResponse deletePolicyDescriptor(@PathVariable("policy_descriptor_uuid") String policy_descriptor_uuid
    ) {
        //JSONObject policyname = new JSONObject(policynamejson);
        //rulesEngineService.deletePolicyDescriptor(policyname.getString("policyname"));

        log.info("i call catalogues with spring rest template so as to delete the policy descriptor");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", "application/json");
        
        try {
            restTemplate.delete(policies_url);
            rulesEngineService.deletePolicyDescriptor(policy_descriptor_uuid);
            
        } catch (Exception e) {
            
            return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_DELETED_FAILURE, "Failed : HTTP error code : "
                    + ". Check if policy vendor or version are null");
        }
        
        return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_DELETED, "");
    }

    //Not used
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listPolicies() {
        log.info("Fecth all policies");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(policies_url, HttpMethod.GET, entity, String.class);
        
        return response.getBody();
    }

    //This REST API should be replaced by asyncronous interaction within son-broker
    @RequestMapping(value = "/activation/{policy_descriptor_uuid}", method = RequestMethod.POST)
    public PolicyRestResponse addKnowledgebase(@RequestBody String SLMObject, @PathVariable("policy_descriptor_uuid") String policy_descriptor_uuid
    ) {
        JSONObject SLMJsonObject = new JSONObject(SLMObject);
        log.info("Rest create addKnowledgebase" + SLMJsonObject.toString());
        rulesEngineService.addNewKnowledgebase(SLMJsonObject.getString("gnsid").replaceAll("-", ""), SLMJsonObject.getString("policyname"));
        
        return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_ACTIVATED, Optional.empty());
        
    }

    //This REST API should be replaced by asyncronous interaction within son-broker
    @RequestMapping(value = "/deactivation/{nsr_id}", method = RequestMethod.POST)
    public PolicyRestResponse removeKnowledgebase(@RequestBody String SLMObject, @PathVariable("nsr_id") String nsr_id
    ) {
        rulesEngineService.removeKnowledgebase(nsr_id);
        return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_DEACTIVATED, Optional.empty());
        
    }

    //Bind a policy with and sla
    @RequestMapping(value = "/{policy_uuid}/sla/{sla_uuid}", method = RequestMethod.GET)
    public PolicyRestResponse bindPolicyToSla(@PathVariable("policy_uuid") String policy_uuid, @PathVariable("sla_uuid") String sla_uuid) {
        
        Optional<RuntimePolicy> runtimepolicy = runtimePolicyRepository.findByPolicyid(policy_uuid);
        
        if (!runtimepolicy.isPresent()) {
            log.info("create new runtime policy object");
            RuntimePolicy rp = new RuntimePolicy();
            rp.setPolicyid(policy_uuid);
            rp.setSlaid(sla_uuid);
            runtimePolicyRepository.save(rp);
        } else {
            log.info("update runtime policy object");
            runtimepolicy.get().setSlaid(sla_uuid);
            runtimePolicyRepository.save(runtimepolicy.get());
        }
        
        return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_BIND_SLA, runtimepolicy);
    }

    //Define a policy as a default policy
    @RequestMapping(value = "/{policy_uuid}/default", method = RequestMethod.GET)
    public PolicyRestResponse setPolicyAsDefault(@PathVariable("policy_uuid") String policy_uuid) {
        Optional<RuntimePolicy> runtimepolicy = runtimePolicyRepository.findByPolicyid(policy_uuid);
        
        if (!runtimepolicy.isPresent()) {
            return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_NOT_EXISTS, null);
        } else {
            log.info("update runtime policy object");
            runtimepolicy.get().setIs_default(true);
            runtimePolicyRepository.save(runtimepolicy.get());
        }
        
        return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_DEFAULT, runtimepolicy);
    }

    /**
     * Inner class containing all the static messages which will be used in an
     * EntropyRestResponse.
     *
     */
    private final static class Message {
        
        final static String POLICIES_LIST = "Return policies list";
        final static String POLICIES_INFO = "Welcome to tng-policy-mngr. For more info check: ";
        final static String POLICY_ACTIVATED = "Policy is succesfully activated";
        final static String POLICY_DEACTIVATED = "Policy is succesfully deactivated";
        final static String POLICY_CREATED = "Policy is succesfully created";
        final static String POLICY_DELETED = "Policy is succesfully deleted";
        final static String POLICY_CREATED_FAILURE = "Policy failed to be created at catalogues";
        final static String POLICY_DELETED_FAILURE = "Policy failed to be deleted at catalogues";
        final static String POLICY_BIND_SLA = "Policy is succesfully binded witn an SLA";
        final static String POLICY_DEFAULT = "Policy is set as default";
        final static String POLICY_NOT_EXISTS = "Policy does not exist";
        
    }
    
}
