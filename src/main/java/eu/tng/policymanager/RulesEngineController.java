package eu.tng.policymanager;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import eu.tng.policymanager.response.BasicResponseCode;
import eu.tng.policymanager.response.PolicyRestResponse;
import eu.tng.policymanager.transferobjects.MonitoringMessageTO;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
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
@RequestMapping("/api/policymngr/v1/policy-descriptor")
public class RulesEngineController {

    private static final Logger log = LoggerFactory.getLogger(RulesEngineController.class);

    @Autowired
    RulesEngineService rulesEngineService;

    @Value("${tng.cat.policies}")
    private String policies_url;

    @RequestMapping(value = "/newMonitoringMessage", method = RequestMethod.POST)
    public boolean newMonitoringMessage(@RequestBody MonitoringMessageTO tobject) {
        rulesEngineService.createFact(tobject);
        return true;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public PolicyRestResponse getInfoPolicyManager() {
        return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICIES_INFO, "https://github.com/sonata-nfv/tng-policy-mngr");
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public PolicyRestResponse savePolicyDescriptor(@RequestBody String tobject) {
        String output;
            //save locally
        //rulesEngineService.savePolicyDescriptor(tobject);
        //save to catalogues
        // RestTemplate restTemplate = new RestTemplate();
        // HttpHeaders httpHeaders = new HttpHeaders();
        // httpHeaders.set("Content-Type", "application/json");
        // httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        // httpHeaders.add("Accept", MediaType.APPLICATION_JSON.toString());

        //HttpEntity<String> httpEntity = new HttpEntity<>(tobject, httpHeaders);
//
//        log.info(httpEntity.getHeaders().toString());
//        log.info("policies_url" + policies_url);
        //String response = restTemplate.postForObject(policies_url, httpEntity, String.class);
        // ResponseEntity response = restTemplate.exchange(policies_url, HttpMethod.POST, httpEntity, String.class);
        // ResponseEntity<String> response = restTemplate.postForEntity(policies_url, httpEntity, String.class);
        ClientResponse response = null;
        try {

            Client client = Client.create();

            WebResource webResource = client.resource(policies_url);

            response = webResource.type("application/json").post(ClientResponse.class, tobject);

            if (response.getStatus() != 201) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatus());
            }

            System.out.println("Output from Server .... \n");
            output = response.getEntity(String.class);
            System.out.println(output);

        } catch (Exception e) {
            e.printStackTrace();
            return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_CREATED_FAILURE, "Failed : HTTP error code : "+ response.getStatus());

        }

        return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_CREATED, output);

    }

    @RequestMapping(value = "/{policy_descriptor_uuid}", method = RequestMethod.DELETE)
    public boolean deletePolicyDescriptor(@RequestBody String policynamejson, @PathVariable("policy_descriptor_uuid") String policy_descriptor_uuid
    ) {
        JSONObject policyname = new JSONObject(policynamejson);
        rulesEngineService.deletePolicyDescriptor(policyname.getString("policyname"));
        return true;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listPolicies() {
        log.info("Fecth all policies");
        RestTemplate restTemplate = new RestTemplate();
        //HttpHeaders headers = new HttpHeaders();
        //headers.add("Content-Type", "application/json");
        //ResponseEntity<String> response = restTemplate.getForEntity(policieslist, String.class);
        //JSONArray response = restTemplate.getForObject(policieslist, JSONArray.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(policies_url, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    //This REST API should be replaced by asyncronous interaction within son-broker
    @RequestMapping(value = "/{policy_descriptor_uuid}/activations", method = RequestMethod.POST)
    public PolicyRestResponse addKnowledgebase(@RequestBody String SLMObject, @PathVariable("policy_descriptor_uuid") String policy_descriptor_uuid
    ) {
        JSONObject SLMJsonObject = new JSONObject(SLMObject);
        log.info("Rest create addKnowledgebase" + SLMJsonObject.toString());
        rulesEngineService.addNewKnowledgebase(SLMJsonObject.getString("gnsid").replaceAll("-", ""), SLMJsonObject.getString("policyname"));

        return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_ACTIVATED, Optional.empty());

    }

    //This REST API should be replaced by asyncronous interaction within son-broker
    @RequestMapping(value = "/{policy_descriptor_uuid}/deactivations", method = RequestMethod.POST)
    public PolicyRestResponse removeKnowledgebase(@RequestBody String SLMObject, @PathVariable("policy_descriptor_uuid") String policy_descriptor_uuid
    ) {
        log.info("Pending to be implemented");
        return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_DEACTIVATED, Optional.empty());

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
        final static String POLICY_CREATED_FAILURE = "Policy failed to be created at catalogues";

    }

}
