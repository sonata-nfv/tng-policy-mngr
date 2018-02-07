package eu.tng.policymanager;

import eu.tng.policymanager.transferobjects.MonitoringMessageTO;

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

}
