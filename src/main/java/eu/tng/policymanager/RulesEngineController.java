package eu.tng.policymanager;

import eu.tng.policymanager.transferobjects.MonitoringMessageTO;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
@RestController
@RequestMapping("/expertsystem")
public class RulesEngineController {

    private static final Logger log = LoggerFactory.getLogger(RulesEngineController.class);

    @Autowired
    RulesEngineService rulesEngineService;

    @RequestMapping(value = "/newMonitoringMessage", method = RequestMethod.POST)
    public boolean newMonitoringMessage(@RequestBody MonitoringMessageTO tobject) {

        rulesEngineService.createFact(tobject);

        return true;

    }

    @RequestMapping(value = "/savePolicyDescriptor", method = RequestMethod.POST)
    public boolean savePolicyDescriptor(@RequestBody String tobject) {
        rulesEngineService.savePolicyDescriptor(tobject);
        return true;
    }

    @RequestMapping(value = "/deletePolicyDescriptor", method = RequestMethod.POST)
    public boolean deletePolicyDescriptor(@RequestBody String policynamejson) {
        JSONObject policyname = new JSONObject(policynamejson);
        rulesEngineService.deletePolicyDescriptor(policyname.getString("policyname"));
        return true;
    }

    //This REST API should be replaced by asyncronous interaction within son-broker
    @RequestMapping(value = "/addKnowledgebase", method = RequestMethod.POST)
    public boolean addKnowledgebase(@RequestBody String SLMObject) {
        JSONObject SLMJsonObject = new JSONObject(SLMObject);
        log.info("Rest create addKnowledgebase" + SLMJsonObject.toString());
        rulesEngineService.addNewKnowledgebase(SLMJsonObject.getString("gnsid"), SLMJsonObject.getString("policyname"));

        return true;

    }

}
