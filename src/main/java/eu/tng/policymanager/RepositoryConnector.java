/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager;

import eu.tng.policymanager.Exceptions.NSDoesNotExistException;
import eu.tng.policymanager.Exceptions.VNFRDoesNotExistException;
import org.json.JSONArray;
import org.json.JSONObject;
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
public class RepositoryConnector {

    private static final Logger log = LoggerFactory.getLogger(CataloguesConnector.class);

    @Value("${tng.rep}")
    private String tng_rep;

    String get_vnfr_id_to_remove_random(String nsrid, String vnfd_id) throws NSDoesNotExistException, VNFRDoesNotExistException {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String ns_list_request = "http://" + tng_rep + "/nsrs/" + nsrid;
        ResponseEntity<String> nsr_response = null;
        try {
            nsr_response = restTemplate.exchange(ns_list_request, HttpMethod.GET, entity, String.class);

        } catch (HttpClientErrorException e) {

            if (e.getMessage().contains("404")) {
                throw new NSDoesNotExistException("NS with id:" + nsrid + " does not exist at the repository");
            } else {
                log.error("Problem with repository " + e.getMessage());
            }

        }
        log.info("invoke the " + ns_list_request);

        JSONObject vnf = new JSONObject(nsr_response.getBody());

        JSONArray network_functions = vnf.getJSONArray("network_functions");

        boolean vnfr_id_found = false;
        for (int i = 0; i < network_functions.length(); i++) {
            JSONObject network_function = network_functions.getJSONObject(i);
            String vnfr_id = network_function.getString("vnfr_id");

            String vnfr_request = "http://" + tng_rep + "/vnfrs/" + vnfr_id;

            ResponseEntity<String> vnfr_response = restTemplate.exchange(vnfr_request, HttpMethod.GET, entity, String.class);

            log.info("invoke the " + vnfr_request);

            JSONObject vnfr = new JSONObject(vnfr_response.getBody());

            if (vnfr.getString("descriptor_reference").equalsIgnoreCase(vnfd_id) && vnfr.getString("status").equalsIgnoreCase("normal operation") ) {

                log.info("vnfr to be removed is " + vnfr_id);
                vnfr_id_found = true;
                return vnfr_id;
            }

        }

        if (vnfr_id_found == false) {
            throw new VNFRDoesNotExistException("Vnf with vnfd_id:" + vnfd_id + " not found for service :" + nsrid);
        }
        return null;

    }

}
