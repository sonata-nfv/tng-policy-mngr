package eu.tng.policymanager.Messaging;

import eu.tng.policymanager.facts.RuleActionType;
import eu.tng.policymanager.RulesEngineApp;
import java.util.logging.Level;

import org.springframework.stereotype.Component;

import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.web.client.RestTemplate;

@Component
public class RuntimeActionsListener {


    @Value("${transcodingserver.url}")
    private String TRANSCODING_SERVER_URL;

    //@Autowired
    //OpenStackAdapter openStackAdapter;
    /**
     * Instead of just using the annotation here, we can configure a
     * DefaultMessageListenerContainer which has more features.
     */
    private static final Logger logger = Logger.getLogger(RuntimeActionsListener.class.getName());

    @JmsListener(destination = RulesEngineApp.RUNTIME_ACTIONS_TOPIC, containerFactory = "myJmsContainerFactory",
            selector = "context = 'runtime_action'" /*, concurrency="5-10"*/
    /*, subscription="durable"*/
    )
    public void expertSystemMessageReceived(ExpertSystemMessage message) {

        //logger.log(Level.INFO, "Receive to RUNTIME_ACTIONS_TOPIC action for ggid{0} and nodeid {1} with proposed action type {2} and value {3}", new Object[]{message.getGgid(), message.getNodeid(), message.getRuleActionType(), message.getValue()});
        logger.log(Level.INFO, "ExpertSystemMessageTO   is like this " + message.toString());

        String activityDescription = "";
        if (message.getRuleActionType().toString() == RuleActionType.VIRTUAL_FUNCTION.toString()) {
            activityDescription = "The component with nodeid " + message.getNodeid() + " should do " + message.getAction() + " by " + message.getValue() + " . Msg from Grounded graph ";

        } else if (message.getRuleActionType().toString() == RuleActionType.IAAS_MANAGEMENT.toString()) {
            activityDescription = "The component with nodeid " + message.getNodeid() + " should do " + message.getAction() + " by " + message.getValue() + " . Msg from Grounded graph ";

        } else if (message.getRuleActionType().toString() == RuleActionType.ALERT_MESSAGE.toString()) {

            activityDescription = "Alert: \"" + message.getValue() + "\" . Msg from Grounded graph ";

        } else if (message.getRuleActionType().toString() == RuleActionType.COMPONENT_CONFIGURATION.toString()) {

            activityDescription = "The configuration parameter \"" + message.getAction() + "\" of the component with nodeid " + message.getNodeid() + " should be updated to value \" " + message.getValue() + "\" . Msg from Grounded graph ";

        } else if (message.getRuleActionType().toString() == RuleActionType.COMPONENT_LIFECYCLE_MANAGEMENT.toString()) {
            activityDescription = "The component with nodeid " + message.getNodeid() + " should do " + message.getAction() + " by " + message.getValue() + " . Msg from Grounded graph ";
            //addTranscondingWorkers(message);
        }

        logger.info(activityDescription);
    }

    private void addTranscondingWorkers(ExpertSystemMessage message) {

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = null;
        if (message.getAction().contains("start")) {
            response = restTemplate.getForEntity(TRANSCODING_SERVER_URL + "/infrastructure/start/" + message.getValue(), String.class);
        } else if (message.getAction().contains("stop")) {
            response = restTemplate.getForEntity(TRANSCODING_SERVER_URL + "/infrastructure/stop/" + message.getValue(), String.class);
        }

        logger.info("invocation of transconding servise with response " + response);

    }

}
