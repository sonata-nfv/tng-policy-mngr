/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class CataloguesConnector {

    private static final Logger log = LoggerFactory.getLogger(CataloguesConnector.class);

    @Value("${tng.cat.policies}")
    private String policies_url;

    @Value("${tng.cat.slas}")
    private String slas_url;

    @Value("${tng.cat.network.services}")
    private String services_url;

    public CataloguesConnector() {
    }

    public boolean checkifPolicyDescriptorExists(String policy_uuid) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            restTemplate.exchange(policies_url + "/" + policy_uuid, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException e) {
            log.info("HttpClientErrorException " + e.getMessage());
            return false;
        }
        return true;

    }

    public boolean checkifSlaDescriptorExists(String sla_uuid) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            restTemplate.exchange(slas_url + "/" + sla_uuid, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException e) {
            log.info("HttpClientErrorException " + e.getMessage());
            return false;
        }
        return true;

    }

    public boolean checkifNSDescriptorExists(String ns_uuid) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            restTemplate.exchange(services_url + "/" + ns_uuid, HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException e) {
            log.info("HttpClientErrorException " + e.getMessage());
            return false;
        }
        return true;

    }

    String getVnfId(String vnfs_url, String name, String vendor, String version) throws VnfDoesNotExistException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String vnfs_url_complete = vnfs_url
                + "?name=" + name
                + "&vendor=" + vendor
                + "&version=" + version;

        ResponseEntity<String> response1 = restTemplate.exchange(vnfs_url_complete, HttpMethod.GET, entity, String.class);

        log.info("invoke the " + vnfs_url_complete);

        JSONArray vnfs = new JSONArray(response1.getBody());
        if (vnfs.length() == 0) {
            throw new VnfDoesNotExistException("Vnf with name:" + name + " vendor:" + vendor + " version:" + version + " does not exist at the catalogues");
        }

        String ns_uuid = vnfs.getJSONObject(0).getString("uuid");
        return ns_uuid;
    }

    String get_vnfr_id_to_remove(String nsrid, String vnfd_id) {

         //to be implemented
        return "123pigastinkiria";

    }

    class VnfDoesNotExistException extends Exception {

        // Parameterless Constructor
        public VnfDoesNotExistException() {
        }

        // Constructor that accepts a message
        public VnfDoesNotExistException(String message) {
            super(message);
        }
    }

}
