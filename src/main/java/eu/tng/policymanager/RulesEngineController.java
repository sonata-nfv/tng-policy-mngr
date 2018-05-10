package eu.tng.policymanager;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import eu.tng.policymanager.response.BasicResponseCode;
import eu.tng.policymanager.response.PolicyRestResponse;
import eu.tng.policymanager.transferobjects.MonitoringMessageTO;
import java.io.BufferedReader;
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
        String output = "";
        //save locally
        rulesEngineService.savePolicyDescriptor(tobject);
        //save to catalogues
        log.info("i call catalogues with spring rest template");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", "application/json");
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("Accept", MediaType.APPLICATION_JSON.toString());

        HttpEntity<String> httpEntity = new HttpEntity<>(tobject, httpHeaders);

        String responseone = null;
        try {
            responseone = restTemplate.postForObject(policies_url, httpEntity, String.class);

            log.info("responseOne" + responseone);

        } catch (Exception e) {
            log.info("-------------------------------responseOne Exception-------------------------------");
            e.printStackTrace();
            //return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_CREATED_FAILURE, "Failed : HTTP error code : " + responseone);
            //log.info("responseOne" + responseone);
        }
        ResponseEntity responseTwo = null;
        try {
            responseTwo = restTemplate.exchange(policies_url, HttpMethod.POST, httpEntity, String.class);
        } catch (Exception e) {
            log.info("-------------------------------responseTwo Exception-------------------------------");
            e.printStackTrace();
            //return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_CREATED_FAILURE, "Failed : HTTP error code : " + responseTwo);

        }
        ResponseEntity<String> responseThree = null;
        try {
            responseThree = restTemplate.postForEntity(policies_url, httpEntity, String.class);
        } catch (Exception e) {
            log.info("-------------------------------responseThree Exception-------------------------------");
            e.printStackTrace();
            //return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_CREATED_FAILURE, "Failed : HTTP error code : " + responseThree);

        }

        log.info("i call catalogues with basic Jersey library");
        ClientResponse response = null;
        try {

            Client client = Client.create();

            WebResource webResource = client.resource(policies_url);

            webResource.accept(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response = webResource.type("application/json").post(ClientResponse.class, tobject);

            if (response.getStatus() != 201) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatus());
            }

            System.out.println("Output from Server .... \n");
            output = response.getEntity(String.class);
            System.out.println(output);

        } catch (Exception e) {
            log.info("-------------------------------response with Jersey Exception-------------------------------");
            log.info("Failed : HTTP error code : " + response.getStatus());
            //e.printStackTrace();
            //return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICY_CREATED_FAILURE, "Failed : HTTP error code : " + response.getStatus());

        }
        log.info("i call catalogues HttpURLConnection - same way as slas");
        try {

            URL object = new URL(policies_url);

            HttpURLConnection con = (HttpURLConnection) object.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestMethod("POST");
            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(tobject);
            wr.flush();

            StringBuilder sb = new StringBuilder();
            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                System.out.println("" + sb.toString());
            } else {
                System.out.println(con.getResponseMessage());
            }
        } catch (Exception e) {
            log.info("-------------------------------response with HttpURLConnection Exception-------------------------------");
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

        ///////////////////////////////////Test
//        String tobject ="{\n  \"name\": \"samplepolicydemo11\",\n  \"descriptor_schema\": \"https://raw.githubusercontent.com/sonata-nfv/tng-schema/master/policy-descriptor/policy-schema.yml\",\n  \"policyRules\": [\n    {\n      \"salience\": 1,\n      \"inertia\": {\n        \"value\": 30,\n        \"duration_unit\": \"m\"\n      },\n      \"name\": \"actionUponAlert\",\n      \"conditions\": {\n        \"condition\": \"AND\",\n        \"rules\": [\n          {\n            \"input\": \"text\",\n            \"field\": \"vnf1.LogMetric\",\n            \"id\": \"vnf1.LogMetric\",\n            \"type\": \"string\",\n            \"value\": \"mon_rule_vm_cpu_perc\",\n            \"operator\": \"equal\"\n          }\n        ]\n      },\n      \"actions\": [\n        {\n          \"action_type\": \"InfrastructureType\",\n          \"name\": \"ApplyFlavour\",\n          \"value\": \"3\",\n          \"action_object\": \"ComponentResourceAllocationAction\",\n          \"target\": \"vnf1\"\n        }\n      ]\n    },\n    {\n      \"salience\": 1,\n      \"duration\": {\n        \"value\": 10,\n        \"duration_unit\": \"m\"\n      },\n      \"inertia\": {\n        \"value\": 30,\n        \"duration_unit\": \"m\"\n      },\n      \"name\": \"highTranscodingRateRule\",\n      \"aggregation\": \"avg\",\n      \"conditions\": {\n        \"condition\": \"AND\",\n        \"rules\": [\n          {\n            \"input\": \"number\",\n            \"field\": \"vnf1.CPULoad\",\n            \"id\": \"vnf1.CPULoad\",\n            \"type\": \"double\",\n            \"value\": \"70\",\n            \"operator\": \"greater\"\n          },\n          {\n            \"input\": \"select\",\n            \"field\": \"vnf2.RAM\",\n            \"id\": \"vnf2.RAM\",\n            \"type\": \"integer\",\n            \"value\": \"8\",\n            \"operator\": \"less\"\n          }\n        ]\n      },\n      \"actions\": [\n        {\n          \"action_type\": \"InfrastructureType\",\n          \"name\": \"ApplyFlavour\",\n          \"value\": \"3\",\n          \"action_object\": \"ComponentResourceAllocationAction\",\n          \"target\": \"vnf1\"\n        }\n      ]\n    }\n  ]\n}";
//        HttpHeaders headers1 = new HttpHeaders();
//        headers1.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<String> entity1 = new HttpEntity<>(tobject, headers1);
//
//        ResponseEntity<String> response1 = restTemplate.exchange("http://localhost:8081/api/policymngr/v1/policy-descriptor", HttpMethod.POST, entity1, String.class);
//
//        log.info(response1.getBody());
//        
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
