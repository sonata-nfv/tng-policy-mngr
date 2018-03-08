package eu.tng.policymanager.Messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import java.util.logging.Level;

import org.springframework.stereotype.Component;

import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;

@Component
public class MonitoringListener {

    @Value("${transcodingserver.url}")
    private String TRANSCODING_SERVER_URL;

    /**
     * Instead of just using the annotation here, we can configure a
     * DefaultMessageListenerContainer which has more features.
     */
    private static final Logger logger = Logger.getLogger(MonitoringListener.class.getName());

    public void monitoringAlertReceived(@Payload String message) {

        
        logger.log(Level.INFO, "monitoring alert   is like this " + message.toString());

    }

}
