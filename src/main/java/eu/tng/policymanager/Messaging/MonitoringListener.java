package eu.tng.policymanager.Messaging;

import java.util.LinkedHashMap;
import java.util.logging.Level;

import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class MonitoringListener {

    /**
     * Instead of just using the annotation here, we can configure a
     * DefaultMessageListenerContainer which has more features.
     */
    private static final Logger logger = Logger.getLogger(MonitoringListener.class.getName());

    public void monitoringAlertReceived(LinkedHashMap message) {

        
        logger.log(Level.INFO, "monitoring alert   is like this " + message.toString());
        logger.log(Level.INFO, "alert name: " + message.get("alertname"));

    }

}
