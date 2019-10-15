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
import eu.tng.policymanager.Exceptions.NSDoesNotExistException;
import eu.tng.policymanager.Exceptions.VNFDoesNotExistException;
import eu.tng.policymanager.Messaging.LogsFormat;
import eu.tng.policymanager.facts.action.Action;
import eu.tng.policymanager.facts.action.AlertAction;
import eu.tng.policymanager.repository.MonitoringRule;
import eu.tng.policymanager.repository.PolicyYamlFile;
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
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    LogsFormat logsFormat;

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

    @Value("${tng.gatekeeper}")
    private String gatekeeper_url;

    @Value("${monitoring.manager}")
    private String monitoring_manager;

    @Autowired
    RuntimePolicyRepository runtimePolicyRepository;

    @Autowired
    RuntimePolicyRecordRepository runtimePolicyRecordRepository;

    @Autowired
    PlacementPolicyRepository placementPolicyRepository;

    @Autowired
    RecommendedActionRepository recommendedActionRepository;

    //GET healthcheck for runtime policies
    @RequestMapping(value = "/pings", method = RequestMethod.GET)
    public String pings() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        logsFormat.createLogInfo("I", timestamp.toString(), "healthcheck", "ping policy manager", "200");

        return "{ \"alive_now\": \"" + new Date() + "\"}";
    }

    //GET a list of all runtime policies. Accept as query parameter the ns_uuid
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity listPolicies(@RequestParam Map<String, String> queryParameters
    ) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logsFormat.createLogInfo("I", timestamp.toString(), "Fetch all policies", "", "200");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(policies_url, HttpMethod.GET, entity, String.class);

        JSONArray policieslist = new JSONArray(response.getBody());
        JSONArray policieslist_toreturn = new JSONArray();

        for (int i = 0; i < policieslist.length(); i++) {

            JSONObject policy = policieslist.getJSONObject(i);
            ResponseEntity enriched_policy_response = this.getPolicy(policy.getString("uuid"));
            String enriched_policy = enriched_policy_response.getBody().toString();

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

    //get number of existing policies
    @RequestMapping(value = "/counter", method = RequestMethod.GET)
    public String num_of_policies() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logsFormat.createLogInfo("I", timestamp.toString(), "Fetch number of policies", "", "200");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(policies_url, HttpMethod.GET, entity, String.class);

        JSONArray policieslist = new JSONArray(response.getBody());

        return String.valueOf(policieslist.length());
    }

    //Create a Policy
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity createPolicyDescriptor(@RequestBody String tobject
    ) {
        //save to catalogues
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logsFormat.createLogInfo("I", timestamp.toString(), "Create a Policy", "Request creation of Policy", "200");
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
            logsFormat.createLogError("E", timestamp.toString(), "Error in policy creation", e.getMessage(), "200");
            PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.REJECTED, Message.POLICY_CREATED_FAILURE, "Failed : HTTP error code : " + responseone
                    + ". Check if policy vendor or version are null");
            return buildResponseEntity(response, HttpStatus.PRECONDITION_FAILED);
        }

        //PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_CREATED, responseone);
        return this.buildPlainResponse(responseone, HttpStatus.OK);

    }

    //create a policy via the ui
    @RequestMapping(value = "/ui", method = RequestMethod.POST)
    public ResponseEntity createPolicyDescriptorFromUI(@RequestBody String tobject
    ) {
        //save to catalogues
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logsFormat.createLogInfo("I", timestamp.toString(), "Create a Policy", "Request creation of Policy", "200");
        logsFormat.createLogInfo("I", timestamp.toString(), "Submitted policy format from UI", tobject, "200");

        JSONObject policyjson = new JSONObject(tobject);
        policyjson.put("descriptor_schema", "https://raw.githubusercontent.com/sonata-nfv/tng-schema/master/policy-descriptor/policy-schema.yml");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        JSONArray monitoring_rules = policyjson.getJSONArray("monitoring_rules");
        for (int i = 0; i < monitoring_rules.length(); i++) {

            JSONObject monitoring_rule = monitoring_rules.getJSONObject(i);

            //check if > or < creates problem
            String monitoring_rule_name = monitoring_rule.getString("name");

            monitoring_rule_name = defineRuleOperator(monitoring_rule_name);
            monitoring_rule.put("name", monitoring_rule_name);

        }

        JSONArray policyRules = policyjson.getJSONArray("policyRules");

        for (int i = 0; i < policyRules.length(); i++) {

            JSONObject policy_rule = policyRules.getJSONObject(i);

            //check if > or < creates problem
            JSONObject policy_rules_conditions = policy_rule.getJSONObject("conditions");
            JSONArray policy_condition_rules = policy_rules_conditions.getJSONArray("rules");

            for (int j = 0; j < policy_condition_rules.length(); j++) {
                JSONObject policy_condition_rule = policy_condition_rules.getJSONObject(j);

                String rule_id = policy_condition_rule.getString("id");
                policy_condition_rule.put("id", rule_id + ".LogMetric");

                String rule_field = policy_condition_rule.getString("field");
                policy_condition_rule.put("field", rule_field + ".LogMetric");

                policy_condition_rule.put("type", "string");
                policy_condition_rule.put("input", "text");
                policy_condition_rule.put("operator", "equal");

                String rule_value = policy_condition_rule.getString("value");
                rule_value = defineRuleOperator(rule_value);
                rule_value = rule_value.replace(":", "_").replace("-", "_");
                policy_condition_rule.put("value", rule_value);

            }

            JSONArray policy_rules_actions = policy_rule.getJSONArray("actions");
            for (int k = 0; k < policy_rules_actions.length(); k++) {
                JSONObject policy_rules_action = policy_rules_actions.getJSONObject(k);
                if (policy_rules_action.getString("action_object").equalsIgnoreCase("ElasticityAction")) {
                    policy_rules_action.put("action_type", "ScalingType");
                }

            }

        }

        String responseone = null;

        boolean default_policy = false;
        if (policyjson.has("default_policy")) {
            if (policyjson.getBoolean("default_policy")) {
                default_policy = true;
            }
            policyjson.remove("default_policy");
        }

        HttpEntity<String> httpEntity = new HttpEntity<>(policyjson.toString(), httpHeaders);

        //System.out.println("final policy json " + policyjson.toString());
        JSONObject ns_json = policyjson.getJSONObject("network_service");
        String ns_uuid;
        try {
            ns_uuid = cataloguesConnector.getNSid(services_url, ns_json.getString("name"), ns_json.getString("vendor"), ns_json.getString("version"));
        } catch (NSDoesNotExistException ex) {
            logsFormat.createLogError("E", timestamp.toString(), "Error in policy creation", ex.getMessage(), "200");
            PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.REJECTED, Message.POLICY_CREATED_FAILURE, "Failed : HTTP error code : " + responseone
                    + ". Network service does not exist on catalogues");
            return buildResponseEntity(response, HttpStatus.PRECONDITION_FAILED);
        }

        try {

            responseone = restTemplate.postForObject(policies_url, httpEntity, String.class);
            JSONObject policyDescriptor = new JSONObject(responseone);
            String policy_uuid = policyDescriptor.getString("uuid");

            if (default_policy) {
                RuntimePolicy rp = new RuntimePolicy();
                rp.setDefaultPolicy(true);
                rp.setPolicyid(policy_uuid);
                rp.setNsid(ns_uuid);
                runtimePolicyRepository.save(rp);
            }

            //save locally
            rulesEngineService.savePolicyDescriptor(tobject, policy_uuid);
        } catch (Exception e) {
            logsFormat.createLogError("E", timestamp.toString(), "Error in policy creation", e.getMessage(), "200");
            PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.REJECTED, Message.POLICY_CREATED_FAILURE, "Failed : HTTP error code : " + responseone
                    + ". Check if policy vendor or version are null");
            return buildResponseEntity(response, HttpStatus.PRECONDITION_FAILED);
        }
        return this.buildPlainResponse(responseone, HttpStatus.OK);

    }

    @RequestMapping(value = "/clone/{policy_uuid}", method = RequestMethod.GET)
    public ResponseEntity clonePolicy(@PathVariable("policy_uuid") String policy_uuid
    ) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logsFormat.createLogInfo("I", timestamp.toString(), "Clone a policy", "duplicate a policy with uuid" + policy_uuid, "200");

        ResponseEntity parent_policy_response = this.getPolicy(policy_uuid);
        String parent_policy = parent_policy_response.getBody().toString();

        JSONObject parent_policy_descriptor = new JSONObject(parent_policy);
        JSONObject policy_descriptor = parent_policy_descriptor.getJSONObject("pld");

        String parent_name = policy_descriptor.getString("name");
        String child_name = "Copy-of-" + parent_name;
        policy_descriptor.put("name", child_name);

        ResponseEntity responseone = this.createPolicyDescriptor(policy_descriptor.toString());

        PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_CREATED, responseone);
        return buildResponseEntity(response, HttpStatus.OK);

    }

    //GET a policy
    @RequestMapping(value = "/{policy_uuid}", method = RequestMethod.GET)
    public ResponseEntity getPolicy(@PathVariable("policy_uuid") String policy_uuid
    ) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logsFormat.createLogInfo("I", timestamp.toString(), "Get a policy", "Fetch a policy with uuid: " + policy_uuid, "200");

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
                //logsFormat.createLogInfo("I", timestamp.toString(), "Fetch ns", "Fetch ns_uuid for current policy" + policy_uuid, "200");
                JSONObject pld = policy_descriptor.getJSONObject("pld");

                //log.info("pld " + pld);
                JSONObject network_service = pld.getJSONObject("network_service");

                String ns_uuid = cataloguesConnector.getNSid(services_url, network_service.getString("name"), network_service.getString("vendor"), network_service.getString("version"));
                policy_descriptor.put("ns_uuid", ns_uuid);
                return buildPlainResponse(policy_descriptor.toString(), HttpStatus.OK);

            }
        } catch (HttpClientErrorException e) {

            return buildPlainResponse("{\"error\": \"The PLD ID " + policy_uuid + " does not exist at catalogues. Message : "
                    + e.getMessage() + "\"}", HttpStatus.NOT_FOUND);

        } catch (NSDoesNotExistException ex) {
            return buildPlainResponse("{\"error\": \"The network service mentioned at policy " + policy_uuid + " does not exist at catalogues.\"}", HttpStatus.NOT_FOUND);
        }
        return buildPlainResponse("{\"warning\": \"The PLD ID " + policy_uuid + " does not exist at catalogues.\"}", HttpStatus.NOT_FOUND);

    }

    //Update a Policy -TO BE CHECKED
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ResponseEntity updatePolicyDescriptor(@RequestBody String tobject
    ) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logsFormat.createLogInfo("I", timestamp.toString(), "Update policy", "Update a policy descriptor", "200");

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
            logsFormat.createLogError("E", timestamp.toString(), "Error while updating policy", e.getMessage(), "500");
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

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logsFormat.createLogInfo("I", timestamp.toString(), "delete policy", "Request deletion of policy descriptor", "200");

        RestTemplate restTemplate = new RestTemplate();

        Gson gson = new Gson();

        HttpHeaders responseHeaders = new HttpHeaders();

        List<RuntimePolicyRecord> runtimePolicyRecord = runtimePolicyRecordRepository.findByPolicyid(policy_uuid);

        if (runtimePolicyRecord.size() > 0) {
            logsFormat.createLogError("E", timestamp.toString(), "Error while deleting a policy", Message.POLICY_DELETED_FORBIDEN, "");

            PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.REJECTED, Message.POLICY_DELETED_FORBIDEN, policy_uuid);
            return buildResponseEntity(response, HttpStatus.PRECONDITION_FAILED);
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

        //check if nsid is associated with sla and policy
        Optional<RuntimePolicy> runtimepolicy = runtimePolicyRepository.findByPolicyid(policy_uuid);
        RuntimePolicy rp;

        if (!runtimepolicy.isPresent()) {
            //log.info("create new runtime policy object");
            rp = new RuntimePolicy();
            rp.setPolicyid(policy_uuid);
        } else {
            //log.info("update runtime policy object");
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
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        String ns_uuid = tobject.getNsid();
        String sla_uuid = tobject.getSlaid();

        if (!cataloguesConnector.checkifPolicyDescriptorExistsForNS(policy_uuid, ns_uuid)) {
            response = new PolicyRestResponse(BasicResponseCode.INVALID, Message.POLICY_NOT_EXISTS_FOR_NS, null);
            return buildResponseEntity(response, HttpStatus.NOT_FOUND);
        }

        if (tobject.getSlaid() != null && !cataloguesConnector.checkifSlaDescriptorExists(sla_uuid)) {
            response = new PolicyRestResponse(BasicResponseCode.INVALID, Message.SLA_NOT_EXISTS_FOR_NS, null);
            return buildResponseEntity(response, HttpStatus.NOT_FOUND);
        }

        Optional<RuntimePolicy> runtimepolicy = runtimePolicyRepository.findByPolicyid(policy_uuid);
        RuntimePolicy rp;

        if (!runtimepolicy.isPresent()) {
            //log.info("create new runtime policy object");
            rp = new RuntimePolicy();
            rp.setPolicyid(policy_uuid);
        } else {
            //log.info("update runtime policy object");
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

        List<RuntimePolicy> existing_runtimepolicy_list = runtimePolicyRepository.findBySlaidAndNsid(sla_uuid, ns_uuid);

        if (existing_runtimepolicy_list.size() > 0 && existing_runtimepolicy_list.get(0).getSlaid() != null) {
            response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_ALREADY_BINDED, "");
            logsFormat.createLogInfo("I", timestamp.toString(), "Bind Runtime Policy With Sla", Message.POLICY_ALREADY_BINDED, "200");

        } else {
            runtimePolicyRepository.save(rp);
            response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_METADATA_UPDATED, runtimepolicy);
            logsFormat.createLogInfo("I", timestamp.toString(), "Bind Runtime Policy With Sla", Message.POLICY_METADATA_UPDATED, "200");

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
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        HttpHeaders responseHeaders = new HttpHeaders();
        Gson gson = new Gson();
        placementPolicyRepository.deleteAll();

        JSONObject placementpolicy_object = new JSONObject(tobject);
        PlacementPolicy placementpolicy_tosave = new PlacementPolicy();

        String policy = placementpolicy_object.getString("policy");

        //policy validation
        List<String> myList = Arrays.asList("Prioritise", "Load Balanced", "Fill First");
        boolean is_policy_valid = myList.stream().anyMatch(str -> str.equals(policy));

        if (!is_policy_valid) {

            logsFormat.createLogError("E", timestamp.toString(), "Error in placement policy creation", Message.PLACEMENT_POLICY_CREATED_FAILURE, "400");
            PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.INVALID, Message.PLACEMENT_POLICY_CREATED_FAILURE, "Policy name is invalid");
            return buildResponseEntity(response, HttpStatus.BAD_REQUEST);

        }

        placementpolicy_tosave.setPolicy(policy);
        placementpolicy_tosave.setUuid(UUID.randomUUID());

        if (placementpolicy_object.has("datacenters") && policy.equalsIgnoreCase("Prioritise")) {

            JSONArray datacenters = placementpolicy_object.getJSONArray("datacenters");

            String[] datacenters_tosave = new String[datacenters.length()];

            for (int i = 0; i < datacenters.length(); i++) {

                if (!cataloguesConnector.checkifVIMExists((String) datacenters.get(i))) {
                    PolicyRestResponse response1 = new PolicyRestResponse(BasicResponseCode.INVALID, Message.PLACEMENT_POLICY_CREATED_FAILURE, "Datacenter with uuid " + datacenters.get(i) + " does not exist");
                    return buildResponseEntity(response1, HttpStatus.BAD_REQUEST);
                }

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

        logsFormat.createLogInfo("I", timestamp.toString(), "Create placement policy", placement_policy_to_send.toString(), "200");
        //ResponseEntity responseEntity = new ResponseEntity(placement_policy_to_send.toString(), responseHeaders, HttpStatus.OK);
        //return responseEntity;
        return buildPlainResponse(placement_policy_to_send.toString(), HttpStatus.OK);

    }

    //GET a list of all placement policies
    @RequestMapping(value = "/placement", method = RequestMethod.GET)
    public ResponseEntity listPlacementPolicies() {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        List<PlacementPolicy> placementPolicies = placementPolicyRepository.findAll();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);

        if (placementPolicies.size() > 0) {
            Gson gson = new Gson();

            JSONObject placement_policy = new JSONObject(gson.toJson(placementPolicies.get(0)));
            placement_policy.remove("id");
            logsFormat.createLogInfo("I", timestamp.toString(), "Fetch placement policy", placement_policy.toString(), "200");
            return new ResponseEntity(placement_policy.toString(), responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity(new JSONObject().toString(), responseHeaders, HttpStatus.OK);
        }
    }

    //GET a list of all recommended actions
    @RequestMapping(value = "/actions", method = RequestMethod.GET)
    public String listActions() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logsFormat.createLogInfo("I", timestamp.toString(), "Fetch list of Actions", "", "200");
        List<RecommendedAction> recommendedActions = recommendedActionRepository.findAllByOrderByInDateTimeDesc();
        Gson gson = new Gson();

        return gson.toJson(recommendedActions);
    }

    @RequestMapping(value = "/actions/counter", method = RequestMethod.GET)
    public String num_of_actions() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logsFormat.createLogInfo("I", timestamp.toString(), "Fetch number of Actions", "", "200");
        long num_actions = recommendedActionRepository.count();
        return String.valueOf(num_actions);
    }

    //deactivate an enforced policy
    @RequestMapping(value = "/deactivate/{nsr_id}", method = RequestMethod.GET)
    public ResponseEntity deactivate(@PathVariable("nsr_id") String nsr_id
    ) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logsFormat.createLogInfo("I", timestamp.toString(), "Deactivate policy", "Deactivate runtme policy for nsr_id " + nsr_id, "200");

        rulesEngineService.removeKnowledgebase("s" + nsr_id.replaceAll("-", ""));

        Optional<RuntimePolicyRecord> runtimePolicyRecord = runtimePolicyRecordRepository.findByNsrid(nsr_id);

        //todo : check if exists before delete
        if (runtimePolicyRecord.isPresent()) {
            runtimePolicyRecordRepository.delete(runtimePolicyRecord.get());
        } else {
            logsFormat.createLogInfo("I", timestamp.toString(), "Runtime policy is already deactivated", "Runtime policy is already deactivated for nsr_id " + nsr_id, "200");
        }

        PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_DEACTIVATED, true);
        return buildResponseEntity(response, HttpStatus.OK);
    }

    //activate an enforced policy given the nsr_id and the runtime policy id
    @RequestMapping(value = "/activate/{nsr_id}/{runtimepolicy_id}", method = RequestMethod.GET)
    public ResponseEntity activate(@PathVariable("nsr_id") String nsr_id, @PathVariable("runtimepolicy_id") String runtimepolicy_id) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logsFormat.createLogInfo("I", timestamp.toString(), "Activate policy", "Activate runtme policy for nsr_id " + nsr_id, "200");
        //1. Fech yml file from catalogues
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(policies_url + "/" + runtimepolicy_id, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException e) {
            logsFormat.createLogError("E", timestamp.toString(), "Activate policy", "The runtime policy " + runtimepolicy_id + " does not exist at catalogues. Message : "
                    + e.getMessage(), "200");
            PolicyRestResponse return_response = new PolicyRestResponse(BasicResponseCode.REJECTED, Message.POLICY_NOT_EXISTS, false);
            return buildResponseEntity(return_response, HttpStatus.NOT_FOUND);
        }

        JSONObject policydescriptorRaw = new JSONObject(response.getBody());
        //log.info("response" + policydescriptorRaw.toString());

        JSONObject pld = policydescriptorRaw.getJSONObject("pld");

        String policyAsYaml = Util.jsonToYaml(pld);
        boolean is_enforcement_succesfull = rulesEngineService.addNewKnowledgebase("s" + nsr_id.replaceAll("-", ""), runtimepolicy_id, policyAsYaml);
        if (is_enforcement_succesfull) {

            //submit monitoring-rules to son-broker
            //fecth monitoring rules from policy
            // update dbpolicy mongo repo
            RuntimePolicyRecord policyrecord = new RuntimePolicyRecord();
            policyrecord.setNsrid(nsr_id);
            policyrecord.setPolicyid(runtimepolicy_id);
            runtimePolicyRecordRepository.save(policyrecord);

            PolicyYamlFile policyyml = PolicyYamlFile.readYaml(policyAsYaml);

            //2. create hashmap with monitoring rules
            List<MonitoringRule> monitoringRules = policyyml.getMonitoring_rules();

            //3. construct prometheus rules
            JSONObject prometheous_rules = new JSONObject();
            prometheous_rules.put("plc_cnt", nsr_id);
            prometheous_rules.put("sonata_service_id", nsr_id);

            ResponseEntity<String> services_response = restTemplate.exchange(gatekeeper_url + "/services/" + nsr_id, HttpMethod.GET, entity, String.class);

            JSONObject service_json = new JSONObject(services_response.getBody());

            //parse network service record
            JSONArray vnfrs = service_json.getJSONArray("network_functions");

            for (int i = 0; i < vnfrs.length(); i++) {

                JSONObject vnfr_info = vnfrs.getJSONObject(i);

                String vnfr_uuid = vnfr_info.getString("vnfr_id");

                ResponseEntity<String> vnfr_response = restTemplate.exchange(gatekeeper_url + "/functions/" + vnfr_uuid, HttpMethod.GET, entity, String.class);

                JSONObject vnfr_object = new JSONObject(vnfr_response.getBody());
                vnfr_object.put("id", vnfr_uuid);

                //System.out.println("vnfr_object--> " + vnfr_object);
                JSONArray prometheous_vnfs = new JSONArray();
                if (vnfr_object.has("virtual_deployment_units")) {
                    prometheous_vnfs = Util.compose_monitoring_rules_os(nsr_id, vnfr_object, monitoringRules);
                } else if (vnfr_object.has("cloudnative_deployment_units")) {
                    prometheous_vnfs = Util.compose_monitoring_rules_k8s(nsr_id, vnfr_object, monitoringRules);
                }

                prometheous_rules.put("vnfs", prometheous_vnfs);

                //System.out.println("prometheous_vnfs ---->" + prometheous_vnfs);
                // Create PLC rules to son-monitor
                String monitoring_url = "http://" + monitoring_manager + "/api/v2/policies/monitoring-rules";
                logsFormat.createLogInfo("I", timestamp.toString(), "Submit monitoring rules to monitoring manager",
                        "POST CALL: " + monitoring_url + " with payload: " + prometheous_rules, "200");

                try {
                    String monitoring_response = Util.sendPrometheusRulesToMonitoringManager(monitoring_url, prometheous_rules);
                    logsFormat.createLogInfo("I", timestamp.toString(), "Monitoring Manager response after submiting prometheus rules",
                            monitoring_response, "200");
                } catch (IOException ex) {
                    logsFormat.createLogInfo("E", timestamp.toString(), "Communication problem with Monitoring Manager",
                            "Policy Enforcement was not succesful", "500");
                }

            }

        } else {
            logsFormat.createLogInfo("E", timestamp.toString(), "Error in policy enforcement function",
                    "Policy Enforcement was not succesful", "500");
        }

        PolicyRestResponse return_response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_ACTIVATED, true);
        return buildResponseEntity(return_response, HttpStatus.OK);
    }

    //get available policies to be activated or deactivated during a NS record lifetime
    @RequestMapping(value = "/records/{nsr_id}", method = RequestMethod.GET)
    public String getPolicyStatusPerNSR(@PathVariable("nsr_id") String nsr_id
    ) {

        Optional<RuntimePolicyRecord> runtimepolicyrecordObject = runtimePolicyRecordRepository.findByNsrid(nsr_id);

        JSONObject runtimePolicyInfo = new JSONObject();
        JSONObject policy = new JSONObject();
        if (runtimepolicyrecordObject.isPresent()) { //check if policy is enforced

            RuntimePolicyRecord runtimePolicyRecord = runtimepolicyrecordObject.get();
            runtimePolicyInfo.put("enforced", true);
            runtimePolicyInfo.put("policy", this.getPolicyMetadata(runtimePolicyRecord.getPolicyid()));

        } else { //check if policy is exists as default or via an sla

            runtimePolicyInfo.put("enforced", false);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(gatekeeper_url + "/services/" + nsr_id, HttpMethod.GET, entity, String.class);
            JSONObject myresponse = new JSONObject(response.getBody());

            String ns_uuid = myresponse.getString("descriptor_reference");

            if (myresponse.has("sla_id")) {

                String sla_uuid = myresponse.getString("sla_id");
                List<RuntimePolicy> runtimePolicyList = runtimePolicyRepository.findBySlaidAndNsid(sla_uuid, ns_uuid);

                if (runtimePolicyList.size() > 0) {

                    RuntimePolicy runtimePolicy = runtimePolicyList.get(0);

                    runtimePolicyInfo.put("policy", this.getPolicyMetadata(runtimePolicy.getPolicyid()));
                } else { //there is no policy binded with this sla
                    runtimePolicyInfo.put("policy", policy);

                }
            } else { //see if it has some default or has no policy at all
                Optional<RuntimePolicy> runtimePolicyObject = runtimePolicyRepository.findByNsidAndDefaultPolicyTrue(ns_uuid);

                if (runtimePolicyObject.isPresent()) {

                    RuntimePolicy runtimePolicy = runtimePolicyObject.get();
                    runtimePolicyInfo.put("policy", this.getPolicyMetadata(runtimePolicy.getPolicyid()));

                } else {

                    runtimePolicyInfo.put("policy", policy);
                }

            }

        }
        return runtimePolicyInfo.toString();
    }

    //get available policies to be activated or deactivated during a NS record lifetime
    @RequestMapping(value = "/monitoring_metrics/{ns_id}", method = RequestMethod.GET)
    public JSONArray getNSMetrics(@PathVariable("ns_id") String ns_id
    ) {

        return cataloguesConnector.getNSMetrics(ns_id);

    }

    //Create a Policy
    @RequestMapping(value = "/new_action", method = RequestMethod.POST)
    public ResponseEntity newAction(@RequestBody String tobject
    ) {
        //save to catalogues
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logsFormat.createLogInfo("I", timestamp.toString(), "New Action is generated", "Request creation of new policy Action", "200");

        JSONObject actionjson = new JSONObject(tobject);
        rulesEngineService.newAction(actionjson);

        return this.buildPlainResponse("", HttpStatus.OK);

    }

    String defineRuleOperator(String monitoring_rule_name) {

        if (monitoring_rule_name.contains(">")) {
            monitoring_rule_name = monitoring_rule_name.replaceAll(">", "more");
        } else if (monitoring_rule_name.contains("<")) {
            monitoring_rule_name = monitoring_rule_name.replaceAll("<", "less");
        } else {
            monitoring_rule_name = monitoring_rule_name.replaceAll("=", "less");
        }

        return monitoring_rule_name;
    }

    ResponseEntity buildResponseEntity(PolicyRestResponse response, HttpStatus httpstatus) {

        HttpHeaders responseHeaders = new HttpHeaders();
        Gson gson = new Gson();

        String responseAsString = gson.toJson(response);
        responseHeaders.set("Content-Length", String.valueOf(responseAsString.length()));
        ResponseEntity responseEntity = new ResponseEntity(responseAsString, responseHeaders, httpstatus);
        return responseEntity;

    }

    ResponseEntity buildPlainResponse(String response, HttpStatus httpstatus) {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Length", String.valueOf(response.length()));
        ResponseEntity responseEntity = new ResponseEntity(response, responseHeaders, httpstatus);
        return responseEntity;

    }

    JSONObject getPolicyMetadata(String policy_uuid) {
        JSONObject policy = new JSONObject();
        ResponseEntity runtimepolicyInfo = this.getPolicy(policy_uuid);
        JSONObject policyAsJson = new JSONObject(runtimepolicyInfo.getBody().toString());
        policy.put("policy_uuid", policy_uuid);
        policy.put("policy_name", policyAsJson.getJSONObject("pld").getString("name"));
        policy.put("policy_vendor", policyAsJson.getJSONObject("pld").getString("vendor"));
        policy.put("policy_version", policyAsJson.getJSONObject("pld").getString("version"));
        if (policyAsJson.has("sla_name")) {
            policy.put("sla_name", policyAsJson.getString("sla_name"));
        }
        return policy;
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
        final static String PLACEMENT_POLICY_CREATED = "Placement Policy is succesfully created";
        final static String POLICY_CLONED = "Policy is succesfully cloned";
        final static String POLICY_DELETED = "Policy is succesfully deleted";
        final static String POLICY_CREATED_FAILURE = "Policy failed to be created at catalogues";
        final static String POLICY_DELETED_FAILURE = "Policy failed to be deleted at catalogues";
        final static String POLICY_DELETED_FORBIDEN = "Policy can not be deleted because is enforced";
        final static String POLICY_DELETION = "Policy failed to be deleted at catalogues";
        final static String POLICY_METADATA_UPDATED = "Policy metadata are sucesfully updated";
        final static String POLICY_ALREADY_BINDED = "Already exists a policy binded with the requested sla and nsid";
        final static String PLACEMENT_POLICY_CREATED_FAILURE = "Placement Policy failed to be created due to invalid parameters";
        final static String MISSING_PARAMETER = "Bad Request. Missing parameters.";
        final static String POLICY_DEFAULT = "Policy is set as default";
        final static String POLICY_NOT_EXISTS = "Policy does not exist";
        final static String POLICY_NOT_EXISTS_FOR_NS = "Policy does not exist for requested network service";
        final static String SLA_NOT_EXISTS = "Sla does not exist";
        final static String SLA_NOT_EXISTS_FOR_NS = "Sla does not exist for requested network service";
        final static String NS_NOT_EXISTS = "Network service does not exist";
        final static String POLICY_UPDATED = "Policy is succesfully updated";
        final static String POLICY_UPDATED_FAILURE = "Policy failed to be updated at catalogues";
        final static String CATALOGUES_CONNECTION_ERROR = "Check conneciton with tng-catalogues";

    }

}
