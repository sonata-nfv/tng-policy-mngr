package eu.tng.policymanager;

import eu.tng.policymanager.response.BasicResponseCode;
import eu.tng.policymanager.response.PolicyRestResponse;
import eu.tng.policymanager.transferobjects.MonitoringMessageTO;
import java.util.Optional;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping(value = "/newMonitoringMessage", method = RequestMethod.POST)
    public boolean newMonitoringMessage(@RequestBody MonitoringMessageTO tobject) {
        rulesEngineService.createFact(tobject);
        return true;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public boolean savePolicyDescriptor(@RequestBody String tobject) {
        rulesEngineService.savePolicyDescriptor(tobject);
        return true;
    }

    @RequestMapping(value = "/{policy_descriptor_uuid}", method = RequestMethod.DELETE)
    public boolean deletePolicyDescriptor(@RequestBody String policynamejson, @PathVariable("policy_descriptor_uuid") String policy_descriptor_uuid) {
        JSONObject policyname = new JSONObject(policynamejson);
        rulesEngineService.deletePolicyDescriptor(policyname.getString("policyname"));
        return true;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public PolicyRestResponse listPolicies() {
        log.info("Pending to be implemented");
        return new PolicyRestResponse(BasicResponseCode.SUCCESS, Message.POLICIES_LIST, Optional.empty());
    }

    //This REST API should be replaced by asyncronous interaction within son-broker
    @RequestMapping(value = "/{policy_descriptor_uuid}/activations", method = RequestMethod.POST)
    public boolean addKnowledgebase(@RequestBody String SLMObject, @PathVariable("policy_descriptor_uuid") String policy_descriptor_uuid) {
        JSONObject SLMJsonObject = new JSONObject(SLMObject);
        log.info("Rest create addKnowledgebase" + SLMJsonObject.toString());
        rulesEngineService.addNewKnowledgebase(SLMJsonObject.getString("gnsid").replaceAll("-", ""), SLMJsonObject.getString("policyname"));

        return true;

    }

    //This REST API should be replaced by asyncronous interaction within son-broker
    @RequestMapping(value = "/{policy_descriptor_uuid}/deactivations", method = RequestMethod.POST)
    public boolean removeKnowledgebase(@RequestBody String SLMObject, @PathVariable("policy_descriptor_uuid") String policy_descriptor_uuid) {
        log.info("Pending to be implemented");
        return true;

    }

    /**
     * Inner class containing all the static messages which will be used in an
     * EntropyRestResponse.
     *
     */
    private final static class Message {

        final static String POLICIES_LIST = "Return policies list";
    }

}
