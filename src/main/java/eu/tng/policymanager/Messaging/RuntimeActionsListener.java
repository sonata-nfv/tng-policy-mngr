package eu.tng.policymanager.Messaging;

import java.util.logging.Level;
import org.springframework.stereotype.Component;
import java.util.logging.Logger;

@Component
public class RuntimeActionsListener {

    private static final Logger logger = Logger.getLogger(RuntimeActionsListener.class.getName());

    
    public void expertSystemMessageReceived(String message) {

        logger.log(Level.INFO, "NetworkManagementAction   is like this {0}", message);

    }

}
