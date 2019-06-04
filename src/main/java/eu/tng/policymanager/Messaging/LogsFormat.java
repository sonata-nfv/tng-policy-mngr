/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.Messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author eleni
 */
public class LogsFormat {
    
    private  final Logger log = LoggerFactory.getLogger(LogsFormat.class);
    
    public void createLog(String type,String timestamps,String operation,String message,String status) {
        
        log.info("{\"type\":\"{}\",\"timestamp\":\"{}\",\"start_stop\":\"\",\"component\":\"tng-policy-mngr\",\"operation\":\"{}\",\"message\":\"{}\",\"status\":\"{}\",\"time_elapsed\":\"\"}",type, timestamps, operation, message, status);
    }
    
}
