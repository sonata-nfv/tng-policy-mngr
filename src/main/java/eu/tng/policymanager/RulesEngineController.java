/*
 * Copyright (c) 2015 SONATA-NFV, 2017 5GTANGO [, ANY ADDITIONAL AFFILIATION]
 * ALL RIGHTS RESERVED.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Neither the name of the SONATA-NFV, 5GTANGO [, ANY ADDITIONAL AFFILIATION]
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * This work has been performed in the framework of the SONATA project,
 * funded by the European Commission under Grant number 671517 through
 * the Horizon 2020 and 5G-PPP programmes. The authors would like to
 * acknowledge the contributions of their colleagues of the SONATA
 * partner consortium (www.sonata-nfv.eu).
 *
 * This work has been performed in the framework of the 5GTANGO project,
 * funded by the European Commission under Grant number 761493 through
 * the Horizon 2020 and 5G-PPP programmes. The authors would like to
 * acknowledge the contributions of their colleagues of the 5GTANGO
 * partner consortium (www.5gtango.eu).
 */
package eu.tng.policymanager;

import com.google.gson.Gson;
import eu.tng.policymanager.facts.LogMetric;
import eu.tng.policymanager.facts.action.ElasticityAction;
import eu.tng.policymanager.facts.enums.ScalingType;
import eu.tng.policymanager.facts.enums.Status;
import eu.tng.policymanager.repository.dao.PlacementPolicyRepository;
import eu.tng.policymanager.repository.dao.RecommendedActionRepository;
import eu.tng.policymanager.repository.dao.RuntimePolicyRecordRepository;
import eu.tng.policymanager.repository.dao.RuntimePolicyRepository;
import eu.tng.policymanager.repository.domain.PlacementPolicy;
import eu.tng.policymanager.repository.domain.RecommendedAction;
import eu.tng.policymanager.repository.domain.RuntimePolicy;
import eu.tng.policymanager.repository.domain.RuntimePolicyRecord;
import eu.tng.policymanager.response.BasicResponseCode;
import eu.tng.policymanager.response.PolicyRestResponse;
import eu.tng.policymanager.rules.generation.Util;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
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

    @Autowired
    CataloguesConnector cataloguesConnector;

    @Value("${tng.cat.policies}")
    private String policies_url;

    @Value("${tng.cat.slas}")
    private String slas_url;

    @Value("${tng.cat.network.services}")
    private String services_url;

    @Autowired
    RuntimePolicyRepository runtimePolicyRepository;

    @Autowired
    RuntimePolicyRecordRepository runtimePolicyRecordRepository;

    @Autowired
    PlacementPolicyRepository placementPolicyRepository;

    @Autowired
    RecommendedActionRepository recommendedActionRepository;

    @Autowired
    private RabbitTemplate template;

    @Qualifier("runtimeActionsQueue")
    @Autowired
    private Queue queue;

    @Autowired
    private TopicExchange exchange;

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

    //GET healthcheck for runtime policies
    @RequestMapping(value = "/pings", method = RequestMethod.GET)
    public String pings() {
        log.info("ping policy manager");
        return "{ \"alive_now\": \"" + new Date() + "\"}";
    }

    //GET a list of all runtime policies. Accept as query parameter the ns_uuid
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity listPolicies(@RequestParam Map<String, String> queryParameters
    ) {

        if (queryParameters.containsKey("ns_uuid")) {
            log.info("Fetch policies with query filter " + queryParameters);
        } else {
            log.info("Fetch all policies");
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(policies_url, HttpMethod.GET, entity, String.class);

        JSONArray policieslist = new JSONArray(response.getBody());
        JSONArray policieslist_toreturn = new JSONArray();

        for (int i = 0; i < policieslist.length(); i++) {

            JSONObject policy = policieslist.getJSONObject(i);
            String enriched_policy = this.getPolicy(policy.getString("uuid"));
            JSONObject enriched_policyJSON = new JSONObject(enriched_policy);
            if (queryParameters.containsKey("ns_uuid")) {

                if (enriched_policyJSON.has("ns_uuid")) {
                    if (queryParameters.get("ns_uuid").equalsIgnoreCase(enriched_policyJSON.getString("ns_uuid"))) {
                        policieslist_toreturn.put(enriched_policyJSON);
                    }
                }

            } else {
                policieslist_toreturn.put(enriched_policyJSON);
            }

        }
        return new ResponseEntity(policieslist_toreturn.toString(), headers, HttpStatus.OK);
        //return policieslist_toreturn.toString();
    }

    //TODO: clean java headers!!!!!!!!!!!!!!!
    //Create a Policy
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity createPolicyDescriptor(@RequestBody String tobject
    ) {
        //save to catalogues
        log.info("Create a Policy");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> httpEntity = new HttpEntity<>(tobject, httpHeaders);

        String responseone = null;
        try {
            responseone = restTemplate.postForObject(policies_url, httpEntity, String.class);

            JSONObject policyDescriptor = new JSONObject(responseone);
            String policy_uuid = policyDescriptor.getString("uuid");

            //save locally
            rulesEngineService.savePolicyDescriptor(tobject, policy_uuid);

        } catch (Exception e) {
            log.info(e.getMessage());
            PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_CREATED_FAILURE, "Failed : HTTP error code : " + responseone
                    + ". Check if policy vendor or version are null");
            return buildResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_CREATED, responseone);
        return buildResponseEntity(response, HttpStatus.OK);

    }

    //GET a policy
    @RequestMapping(value = "/{policy_uuid}", method = RequestMethod.GET)
    public String getPolicy(@PathVariable("policy_uuid") String policy_uuid
    ) {
        log.info("Fetch a policy with uuid" + policy_uuid);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(policies_url + "/" + policy_uuid, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {

                JSONObject policy_descriptor = new JSONObject(response.getBody());
                Optional<RuntimePolicy> runtimepolicy = runtimePolicyRepository.findByPolicyid(policy_uuid);
                if (runtimepolicy.isPresent()) {
                    RuntimePolicy rp = runtimepolicy.get();
                    policy_descriptor.put("default_policy", rp.isDefaultPolicy());
                    policy_descriptor.put("sla_id", rp.getSlaid());

                    //get sla_name
                    if (rp.getSlaid() != null) {
                        ResponseEntity<String> sla_response = restTemplate.exchange(slas_url + "/" + rp.getSlaid(), HttpMethod.GET, entity, String.class);
                        JSONObject sla_descriptor = new JSONObject(sla_response.getBody());
                        JSONObject slad = sla_descriptor.getJSONObject("slad");
                        policy_descriptor.put("sla_name", slad.getString("name"));
                    }

                } else {
                    policy_descriptor.put("default_policy", false);

                }

                List<RuntimePolicyRecord> runtimepolicyrecord = runtimePolicyRecordRepository.findByPolicyid(policy_uuid);

                if (runtimepolicyrecord.size() > 0) {
                    policy_descriptor.put("enforced", true);
                } else {
                    policy_descriptor.put("enforced", false);
                }

                //fetch ns_uuid
                log.info("Fetch ns_uuid for current policy");

                JSONObject pld = policy_descriptor.getJSONObject("pld");

                //log.info("pld " + pld);
                JSONObject network_service = pld.getJSONObject("network_service");

                //log.info("network_service " + network_service);
                String services_url_complete = services_url
                        + "?name=" + network_service.getString("name")
                        + "&version=" + network_service.getString("version")
                        + "&vendor=" + network_service.getString("vendor");

                ResponseEntity<String> response1 = restTemplate.exchange(services_url_complete, HttpMethod.GET, entity, String.class);

                log.info("invoke the " + services_url_complete);

                JSONArray network_services = new JSONArray(response1.getBody());

                if (network_services.length() > 0) {
                    String ns_uuid = network_services.getJSONObject(0).getString("uuid");
                    policy_descriptor.put("ns_uuid", ns_uuid);
                }

                return policy_descriptor.toString();

            }
        } catch (HttpClientErrorException e) {
            return "{\"error\": \"The PLD ID " + policy_uuid + " does not exist at catalogues. Message : "
                    + e.getMessage() + "\"}";
        }

        return "{\"warning\": \"The PLD ID " + policy_uuid + " does not exist at catalogues.\"}";

    }

    //Update a Policy -TO BE CHECKED
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ResponseEntity updatePolicyDescriptor(@RequestBody String tobject
    ) {
        log.info("Update a policy descriptor");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(tobject, httpHeaders);

        //String responseone = null;
        //JSONObject policyDescriptor = new JSONObject(tobject);
        //String policy_uuid = policyDescriptor.getString("uuid");
        try {
            restTemplate.put(policies_url, httpEntity, String.class);

            //TODO update locally
            //rulesEngineService.savePolicyDescriptor(tobject, policy_uuid);
        } catch (Exception e) {
            log.info(e.getMessage());
            PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_UPDATED_FAILURE, "Check connection with tng-cat");
            return buildResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_UPDATED, tobject);
        return buildResponseEntity(response, HttpStatus.OK);

    }

    //Delete a Policy
    @RequestMapping(value = "/{policy_uuid}", method = RequestMethod.DELETE)
    public ResponseEntity deletePolicyDescriptor(@PathVariable("policy_uuid") String policy_uuid
    ) {

        log.info("Delete the policy descriptor");
        RestTemplate restTemplate = new RestTemplate();

        Gson gson = new Gson();

        HttpHeaders responseHeaders = new HttpHeaders();

        List<RuntimePolicyRecord> runtimePolicyRecord = runtimePolicyRecordRepository.findByPolicyid(policy_uuid);

        if (runtimePolicyRecord.size() > 0) {
            log.info(Message.POLICY_DELETED_FORBIDEN);
            PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_DELETED_FORBIDEN, policy_uuid);
            return buildResponseEntity(response, HttpStatus.OK);
        }

        try {
            restTemplate.delete(policies_url + "/" + policy_uuid);
            rulesEngineService.deletePolicyDescriptor(policy_uuid);
            Optional<RuntimePolicy> runtimePolicy = runtimePolicyRepository.findByPolicyid(policy_uuid);
            if (runtimePolicy.isPresent()) {
                runtimePolicyRepository.delete(runtimePolicy.get());
            }

        } catch (Exception e) {
            PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_DELETED_FAILURE, e.getMessage());
            String responseAsString = gson.toJson(response);

            responseHeaders.set("Content-Length", String.valueOf(responseAsString.length()));
            ResponseEntity responseEntity = new ResponseEntity(responseAsString, responseHeaders, HttpStatus.OK);
            return responseEntity;
        }

        PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_DELETED, true);
        return buildResponseEntity(response, HttpStatus.OK);
    }

    // Bind a Policy to an SLA
    // Define a Policy as default
    @RequestMapping(value = "/default/{policy_uuid}", method = RequestMethod.PATCH)
    public ResponseEntity updateRuntimePolicyasDefault(@RequestBody RuntimePolicy tobject, @PathVariable("policy_uuid") String policy_uuid
    ) {
        PolicyRestResponse response;

        if (!cataloguesConnector.checkifPolicyDescriptorExists(policy_uuid)) {
            response = new PolicyRestResponse(BasicResponseCode.INVALID, Message.POLICY_NOT_EXISTS, null);
            return buildResponseEntity(response, HttpStatus.NOT_FOUND);
        }

        if (!cataloguesConnector.checkifNSDescriptorExists(tobject.getNsid())) {
            response = new PolicyRestResponse(BasicResponseCode.INVALID, Message.NS_NOT_EXISTS, null);
            return buildResponseEntity(response, HttpStatus.NOT_FOUND);
        }

        Optional<RuntimePolicy> runtimepolicy = runtimePolicyRepository.findByPolicyid(policy_uuid);
        RuntimePolicy rp;

        if (!runtimepolicy.isPresent()) {
            log.info("create new runtime policy object");
            rp = new RuntimePolicy();
            rp.setPolicyid(policy_uuid);
        } else {
            log.info("update runtime policy object");
            rp = runtimepolicy.get();
        }

        if (tobject.getNsid() != null) {
            rp.setNsid(tobject.getNsid());
        } else {

            response = new PolicyRestResponse(BasicResponseCode.INVALID, Message.MISSING_PARAMETER, null);
            return buildResponseEntity(response, HttpStatus.PARTIAL_CONTENT);
        }

        if (tobject.isDefaultPolicy() == true || tobject.isDefaultPolicy() == false) {
            rp.setDefaultPolicy(tobject.isDefaultPolicy());

            if (tobject.isDefaultPolicy() == true) {

                //check if exists other default policy for same service
                Optional<RuntimePolicy> existing_default_policy = runtimePolicyRepository.findByNsidAndDefaultPolicyTrue(tobject.getNsid());
                if (existing_default_policy.isPresent()) {
                    RuntimePolicy exPolicy = existing_default_policy.get();
                    exPolicy.setDefaultPolicy(false);
                    runtimePolicyRepository.save(exPolicy);
                }

            }
            rp = runtimePolicyRepository.save(rp);
        }

        response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_METADATA_UPDATED, runtimepolicy);
        return buildResponseEntity(response, HttpStatus.OK);

    }

    @RequestMapping(value = "/bind/{policy_uuid}", method = RequestMethod.PATCH)
    public ResponseEntity bindRuntimePolicyWithSla(@RequestBody RuntimePolicy tobject, @PathVariable("policy_uuid") String policy_uuid
    ) {
        PolicyRestResponse response;
        if (!cataloguesConnector.checkifPolicyDescriptorExists(policy_uuid)) {
            response = new PolicyRestResponse(BasicResponseCode.INVALID, Message.POLICY_NOT_EXISTS, null);
            return buildResponseEntity(response, HttpStatus.NOT_FOUND);
        }

        if (tobject.getSlaid() != null && !cataloguesConnector.checkifSlaDescriptorExists(tobject.getSlaid())) {
            response = new PolicyRestResponse(BasicResponseCode.INVALID, Message.SLA_NOT_EXISTS, null);
            return buildResponseEntity(response, HttpStatus.NOT_FOUND);
        }

        Optional<RuntimePolicy> runtimepolicy = runtimePolicyRepository.findByPolicyid(policy_uuid);
        RuntimePolicy rp;

        if (!runtimepolicy.isPresent()) {
            log.info("create new runtime policy object");
            rp = new RuntimePolicy();
            rp.setPolicyid(policy_uuid);
        } else {
            log.info("update runtime policy object");
            rp = runtimepolicy.get();
        }

        if (tobject.getNsid() == null) {
            response = new PolicyRestResponse(BasicResponseCode.INVALID, Message.MISSING_PARAMETER, null);
            return buildResponseEntity(response, HttpStatus.PARTIAL_CONTENT);
        } else if (!cataloguesConnector.checkifNSDescriptorExists(tobject.getNsid())) {
            response = new PolicyRestResponse(BasicResponseCode.INVALID, Message.NS_NOT_EXISTS, null);
            return buildResponseEntity(response, HttpStatus.NOT_FOUND);
        }

        rp.setNsid(tobject.getNsid());
        rp.setSlaid(tobject.getSlaid());

        List<RuntimePolicy> existing_runtimepolicy_list = runtimePolicyRepository.findBySlaidAndNsid(tobject.getSlaid(), tobject.getNsid());

        if (existing_runtimepolicy_list.size()>0 && existing_runtimepolicy_list.get(0).getSlaid() != null) {
            response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_ALREADY_BINDED, "");
            log.info(Message.POLICY_ALREADY_BINDED);
        } else {
            runtimePolicyRepository.save(rp);
            response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_METADATA_UPDATED, runtimepolicy);
            log.info(Message.POLICY_METADATA_UPDATED);
        }

        return buildResponseEntity(response, HttpStatus.OK);
    }


    /*Create placement policy
     {
     "policy": "prioritise",
     "datacenters": ["vim_city 1", "vim_city 2"]
     }*/
    @RequestMapping(value = "/placement", method = RequestMethod.POST)
    public ResponseEntity createPlacementPolicy(@RequestBody String tobject
    ) {
        log.info("Create placement policy");
        HttpHeaders responseHeaders = new HttpHeaders();
        Gson gson = new Gson();
        placementPolicyRepository.deleteAll();

        JSONObject placementpolicy_object = new JSONObject(tobject);
        PlacementPolicy placementpolicy_tosave = new PlacementPolicy();

        String policy = placementpolicy_object.getString("policy");
        placementpolicy_tosave.setPolicy(policy);
        placementpolicy_tosave.setUuid(UUID.randomUUID());

        if (placementpolicy_object.has("datacenters")) {
            JSONArray datacenters = placementpolicy_object.getJSONArray("datacenters");

            String[] datacenters_tosave = new String[datacenters.length()];

            for (int i = 0; i < datacenters.length(); i++) {
                datacenters_tosave[i] = (String) datacenters.get(i);
            }

            placementpolicy_tosave.setDatacenters(datacenters_tosave);
        }

        PlacementPolicy placementpolicy = placementPolicyRepository.save(placementpolicy_tosave);
        String responseAsString = gson.toJson(placementpolicy);
        responseHeaders.set("Content-Length", String.valueOf(responseAsString.length()));
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONObject placement_policy_to_send = new JSONObject(gson.toJson(placementpolicy));
        placement_policy_to_send.remove("id");

        ResponseEntity responseEntity = new ResponseEntity(placement_policy_to_send.toString(), responseHeaders, HttpStatus.OK);
        return responseEntity;

    }

    //GET a list of all placement policies
    @RequestMapping(value = "/placement", method = RequestMethod.GET)
    public ResponseEntity listPlacementPolicies() {
        log.info("Fetch placement policy");
        List<PlacementPolicy> placementPolicies = placementPolicyRepository.findAll();

        ResponseEntity responseEntity;
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);

        if (placementPolicies.size() > 0) {
            Gson gson = new Gson();

            JSONObject placement_policy = new JSONObject(gson.toJson(placementPolicies.get(0)));
            placement_policy.remove("id");
            return new ResponseEntity(placement_policy.toString(), responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity(new JSONObject().toString(), responseHeaders, HttpStatus.OK);
        }
    }

    //GET a list of all recommended actions
    @RequestMapping(value = "/actions", method = RequestMethod.GET)
    public String listActions() {
        log.info("Fetch list of Actions");
        List<RecommendedAction> recommendedActions = recommendedActionRepository.findAll();
        Gson gson = new Gson();

        return gson.toJson(recommendedActions);
    }

    //deactivate an enforced policy
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

    //deactivate an enforced policy
    @RequestMapping(value = "/deactivate/{nsr_id}", method = RequestMethod.GET)
    public ResponseEntity deactivate(@PathVariable("nsr_id") String nsr_id
    ) {
        log.info("remove knowledgebase");
        rulesEngineService.removeKnowledgebase("s" + nsr_id.replaceAll("-", ""));

        Optional<RuntimePolicyRecord> runtimePolicyRecord = runtimePolicyRecordRepository.findByNsrid(nsr_id);

        runtimePolicyRecordRepository.delete(runtimePolicyRecord.get());

        PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_DEACTIVATED, true);
        return buildResponseEntity(response, HttpStatus.OK);
    }

    ResponseEntity buildResponseEntity(PolicyRestResponse response, HttpStatus httpstatus) {

        HttpHeaders responseHeaders = new HttpHeaders();
        Gson gson = new Gson();

        String responseAsString = gson.toJson(response);
        responseHeaders.set("Content-Length", String.valueOf(responseAsString.length()));
        ResponseEntity responseEntity = new ResponseEntity(responseAsString, responseHeaders, httpstatus);
        return responseEntity;

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
        final static String POLICY_DELETED_FORBIDEN = "Policy can not be deleted because is enforced";
        final static String POLICY_DELETION = "Policy failed to be deleted at catalogues";
        final static String POLICY_METADATA_UPDATED = "Policy metadata are sucesfully updated";
        final static String POLICY_ALREADY_BINDED = "Already exists a policy binded with the requested sla and nsid";
        final static String MISSING_PARAMETER = "Bad Request. Missing parameters.";
        final static String POLICY_DEFAULT = "Policy is set as default";
        final static String POLICY_NOT_EXISTS = "Policy does not exist";
        final static String SLA_NOT_EXISTS = "Sla does not exist";
        final static String NS_NOT_EXISTS = "Network service does not exist";
        final static String POLICY_UPDATED = "Policy is succesfully updated";
        final static String POLICY_UPDATED_FAILURE = "Policy failed to be updated at catalogues";
        final static String CATALOGUES_CONNECTION_ERROR = "Check conneciton with tng-catalogues";

    }

}
