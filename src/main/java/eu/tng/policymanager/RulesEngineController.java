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
import eu.tng.policymanager.repository.dao.PlacementPolicyRepository;
import eu.tng.policymanager.repository.dao.RuntimePolicyRepository;
import eu.tng.policymanager.repository.domain.PlacementPolicy;
import eu.tng.policymanager.repository.domain.RuntimePolicy;
import eu.tng.policymanager.response.BasicResponseCode;
import eu.tng.policymanager.response.PolicyRestResponse;
import eu.tng.policymanager.transferobjects.MonitoringMessageTO;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Autowired
    RuntimePolicyRepository runtimePolicyRepository;

    @Autowired
    PlacementPolicyRepository placementPolicyRepository;

    @RequestMapping(value = "/newMonitoringMessage", method = RequestMethod.POST)
    public boolean newMonitoringMessage(@RequestBody MonitoringMessageTO tobject) {
        rulesEngineService.createFact(tobject);
        return true;
    }

    //GET healthcheck for runtime policies
    @RequestMapping(value = "/pings", method = RequestMethod.GET)
    public String pings() {
        log.info("ping policy manager");
        return "{ \"alive_now\": \"" + new Date() + "\"}";
    }

    //GET a list of all runtime policies
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String listPolicies() {
        log.info("Fetch all policies");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(policies_url, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    //TODO: clean java headers!!!!!!!!!!!!!!!
    //Create a Policy
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity createPolicyDescriptor(@RequestBody String tobject) {
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
            return buildResponseEntity(response);
        }

        PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_CREATED, responseone);
        return buildResponseEntity(response);

    }

    //GET a policy
    @RequestMapping(value = "/{policy_uuid}", method = RequestMethod.GET)
    public String getPolicy(@PathVariable("policy_uuid") String policy_uuid) {
        log.info("Fetch a policy with uuid" + policy_uuid);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(policies_url + "/" + policy_uuid, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    //Update a Policy -TO BE CHECKED
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ResponseEntity updatePolicyDescriptor(@RequestBody String tobject) {
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
            //log.info(e.getMessage());
            PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_UPDATED_FAILURE, "Check connection with tng-cat");
            return buildResponseEntity(response);
        }

        PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_UPDATED, tobject);
        return buildResponseEntity(response);

    }

    //Delete a Policy
    @RequestMapping(value = "/{policy_uuid}", method = RequestMethod.DELETE)
    public ResponseEntity deletePolicyDescriptor(@PathVariable("policy_uuid") String policy_uuid) {

        log.info("i call catalogues with spring rest template so as to delete the policy descriptor");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", "application/json");

        Gson gson = new Gson();

        HttpHeaders responseHeaders = new HttpHeaders();

        try {
            restTemplate.delete(policies_url + "/" + policy_uuid);
            rulesEngineService.deletePolicyDescriptor(policy_uuid);

        } catch (Exception e) {
            PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_DELETED_FAILURE, e.getMessage());
            String responseAsString = gson.toJson(response);

            responseHeaders.set("Content-Length", String.valueOf(responseAsString.length()));
            ResponseEntity responseEntity = new ResponseEntity(responseAsString, responseHeaders, HttpStatus.OK);
            return responseEntity;
        }

        PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_DELETED, true);
        return buildResponseEntity(response);
    }

    // Bind a Policy to an SLA
    // Define a Policy as default
    @RequestMapping(value = "/{policy_uuid}", method = RequestMethod.PATCH)
    public ResponseEntity updateRuntimePolicy(@RequestBody RuntimePolicy tobject, @PathVariable("policy_uuid") String policy_uuid) {

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

        if (tobject.getSlaid() != null) {
            rp.setSlaid(tobject.getSlaid());
        }
        if (tobject.isDefaultPolicy() == true) {
            rp.setDefaultPolicy(tobject.isDefaultPolicy());
        }
        if (tobject.getNsid() != null) {
            rp.setNsid(tobject.getNsid());
        }
        runtimePolicyRepository.save(rp);

        PolicyRestResponse response = new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_METADATA_UPDATED, runtimepolicy);
        return buildResponseEntity(response);
    }


    /*Create placement policy
     {
     "policy": "prioritise",
     "datacenters": ["vim_city 1", "vim_city 2"]
     }*/
    @RequestMapping(value = "/placement", method = RequestMethod.POST)
    public ResponseEntity createPlacementPolicy(@RequestBody String tobject) {
        log.info("Create placement policy");
        HttpHeaders responseHeaders = new HttpHeaders();
        Gson gson = new Gson();
        placementPolicyRepository.deleteAll();

        JSONObject placementpolicy_object = new JSONObject(tobject);
        PlacementPolicy placementpolicy_tosave = new PlacementPolicy();

        String policy = placementpolicy_object.getString("policy");
        placementpolicy_tosave.setPolicy(policy);

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
        ResponseEntity responseEntity = new ResponseEntity(placementpolicy, responseHeaders, HttpStatus.OK);
        return responseEntity;

    }

    //GET a list of all placement policies
    @RequestMapping(value = "/placement", method = RequestMethod.GET)
    public String listPlacementPolicies() {
        log.info("Fetch placement policy");
        List<PlacementPolicy> placementPolicies = placementPolicyRepository.findAll();

        if (placementPolicies.size() > 0) {
            Gson gson = new Gson();
            return gson.toJson(placementPolicies.get(0));
        } else {
            return new JSONObject().toString();
        }
    }

    ResponseEntity buildResponseEntity(PolicyRestResponse response) {

        HttpHeaders responseHeaders = new HttpHeaders();
        Gson gson = new Gson();

        String responseAsString = gson.toJson(response);
        responseHeaders.set("Content-Length", String.valueOf(responseAsString.length()));
        ResponseEntity responseEntity = new ResponseEntity(responseAsString, responseHeaders, HttpStatus.OK);
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
        final static String POLICY_METADATA_UPDATED = "Policy metadata are sucesfully updated";
        final static String POLICY_DEFAULT = "Policy is set as default";
        final static String POLICY_NOT_EXISTS = "Policy does not exist";
        final static String POLICY_UPDATED = "Policy is succesfully updated";
        final static String POLICY_UPDATED_FAILURE = "Policy failed to be updated at catalogues";
        final static String CATALOGUES_CONNECTION_ERROR = "Check conneciton with tng-catalogues";

    }

}
