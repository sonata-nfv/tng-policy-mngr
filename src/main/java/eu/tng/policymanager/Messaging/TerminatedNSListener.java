/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.Messaging;

import eu.tng.policymanager.RulesEngineApp;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.logging.Level;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author eleni
 */
@Component
public class TerminatedNSListener {

    @Autowired
    LogsFormat logsFormat;

    @RabbitListener(queues = RulesEngineApp.NS_TERMINATION_QUEUE)
    public void terminatedNSMessageReceived(byte[] message) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        try {
            String terminatedNSasYaml = new String(message, StandardCharsets.UTF_8);
            logsFormat.createLogInfo("I", timestamp.toString(), "NS Termination Message received", terminatedNSasYaml, "200");
        } catch (Exception e) {
            logsFormat.createLogInfo("E", timestamp.toString(), "Problem in receiving NS Termination Message", e.getMessage(), "200");
        }

    }

}
