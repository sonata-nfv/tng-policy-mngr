/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.Messaging;

/**
 *
 * @author eleni
 */
public class LogsFormat {
    
    public void createLog(type,timestamps,operation,message,status) {
        
        logger.info("{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-sla-mgmt\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",type, timestamps, operation, message, status);
    }
    
}
