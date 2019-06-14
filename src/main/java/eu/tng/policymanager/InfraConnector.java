/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager;

import eu.tng.policymanager.*;
import eu.tng.policymanager.Exceptions.VNFDoesNotExistException;
import eu.tng.policymanager.Messaging.LogsFormat;
import java.sql.Timestamp;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class InfraConnector {

    private static final Logger log = LoggerFactory.getLogger(InfraConnector.class);

    @Autowired
    LogsFormat logsFormat;

    @Value("${tng.gtk.vims}")
    private String vims_url;

    public InfraConnector() {
    }

    public boolean checkifVIMExists(String vim_uuid) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            restTemplate.exchange(vims_url + "/" + vim_uuid, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException e) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logsFormat.createLogError("E", timestamp.toString(), "Error in placement policy creation: "+ vims_url + "/" + vim_uuid , "VIM with uuid "+vim_uuid+" does not exist", "400");
            return false;
        }
        return true;

    }

}
