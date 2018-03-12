package eu.tng.policymanager.Messaging;

import eu.tng.policymanager.facts.RuleActionType;
import java.util.logging.Level;
import org.springframework.stereotype.Component;
import java.util.logging.Logger;

@Component
public class RuntimeActionsListener {

    /**
     * Instead of just using the annotation here, we can configure a
     * DefaultMessageListenerContainer which has more features.
     */
    private static final Logger logger = Logger.getLogger(RuntimeActionsListener.class.getName());

    public void expertSystemMessageReceived(ExpertSystemMessage message) {

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

}
