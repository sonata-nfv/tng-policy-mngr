/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager;

import eu.tng.policymanager.Exceptions.NSDoesNotExistException;
import eu.tng.policymanager.Exceptions.VNFDoesNotExistException;
import eu.tng.policymanager.Messaging.LogsFormat;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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

    @Autowired
    LogsFormat logsFormat;

    @Value("${tng.cat.policies}")
    private String policies_url;

    @Value("${tng.cat.slas}")
    private String slas_url;

    @Value("${tng.cat.network.services}")
    private String services_url;

    @Value("${tng.cat.vnfs}")
    private String vnfs_url;

    @Value("${tng.ia.vims}")
    private String vims_url;

    public CataloguesConnector() {
    }

    public boolean checkifPolicyDescriptorExists(String policy_uuid) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(policies_url + "/" + policy_uuid, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return false;
            }
        } catch (HttpClientErrorException e) {
            log.info("HttpClientErrorException " + e.getMessage());
            return false;
        }
        return true;

    }

    public boolean checkifPolicyDescriptorExistsForNS(String policy_uuid, String ns_uuid) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(policies_url + "/" + policy_uuid, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return false;
            }
            JSONObject policy_descriptor = new JSONObject(response.getBody());
            JSONObject pld_json = policy_descriptor.getJSONObject("pld");
            JSONObject ns_json = pld_json.getJSONObject("network_service");

            String services_url_complete = services_url
                    + "?name=" + ns_json.getString("name")
                    + "&vendor=" + ns_json.getString("vendor")
                    + "&version=" + ns_json.getString("version");

            ResponseEntity<String> response1 = restTemplate.exchange(services_url_complete, HttpMethod.GET, entity, String.class);

            JSONArray network_services = new JSONArray(response1.getBody());

            String policy_ns_uuid = network_services.getJSONObject(0).getString("uuid");

            if (policy_ns_uuid.equalsIgnoreCase(ns_uuid)) {
                return true;
            }
        } catch (HttpClientErrorException e) {
            log.info("HttpClientErrorException " + e.getMessage());
            return false;
        }
        return false;

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

    public boolean checkifSlaDescriptorExistsForNS(String sla_uuid, String ns_uuid) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> sla_response = restTemplate.exchange(slas_url + "/" + sla_uuid, HttpMethod.GET, entity, String.class);

            JSONObject sla_descriptor = new JSONObject(sla_response.getBody());
            String sla_ns_uuid = sla_descriptor.getJSONObject("slad")
                    .getJSONObject("sla_template").getJSONObject("service")
                    .getString("ns_uuid");
            if (sla_ns_uuid.equalsIgnoreCase(ns_uuid)) {
                return true;
            }

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

    String getVnfId(String vnfs_url, String name, String vendor, String version) throws VNFDoesNotExistException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String vnfs_url_complete = vnfs_url
                + "?name=" + name
                + "&vendor=" + vendor
                + "&version=" + version;

        ResponseEntity<String> response1 = restTemplate.exchange(vnfs_url_complete, HttpMethod.GET, entity, String.class);

        //log.info("invoke the " + vnfs_url_complete);
        JSONArray vnfs = new JSONArray(response1.getBody());
        if (vnfs.length() == 0) {
            throw new VNFDoesNotExistException("Vnf with name:" + name + " vendor:" + vendor + " version:" + version + " does not exist at the catalogues");
        }

        String ns_uuid = vnfs.getJSONObject(0).getString("uuid");
        return ns_uuid;
    }

    String getNSid(String services_url, String name, String vendor, String version) throws NSDoesNotExistException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String services_url_complete = services_url
                + "?name=" + name
                + "&vendor=" + vendor
                + "&version=" + version;

        ResponseEntity<String> response1 = restTemplate.exchange(services_url_complete, HttpMethod.GET, entity, String.class);

        //log.info("invoke the " + vnfs_url_complete);
        JSONArray vnfs = new JSONArray(response1.getBody());
        if (vnfs.length() == 0) {
            throw new NSDoesNotExistException("NS with name:" + name + " vendor:" + vendor + " version:" + version + " does not exist at the catalogues");
        }

        String ns_uuid = vnfs.getJSONObject(0).getString("uuid");
        return ns_uuid;

    }

    public boolean checkifVIMExists(String vim_uuid) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        try {
            URL obj = new URL(vims_url + "/" + vim_uuid);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();

            if (responseCode != 200) {
                logsFormat.createLogError("E", timestamp.toString(), "Error in placement policy creation: " + vims_url + "/" + vim_uuid, "VIM with uuid " + vim_uuid + " does not exist", "404");
                return false;
            }

        } catch (MalformedURLException ex) {
            logsFormat.createLogError("E", timestamp.toString(), "Error in placement policy creation: " + vims_url + "/" + vim_uuid, "VIM with uuid " + vim_uuid + " does not exist", "404");
            return false;
        } catch (IOException ex) {
            logsFormat.createLogError("E", timestamp.toString(), "Error in placement policy creation: " + vims_url + "/" + vim_uuid, "VIM with uuid " + vim_uuid + " does not exist", "404");
            return false;
        }
        return true;

    }

    public JSONArray getNSMetrics(String ns_uuid) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> nsdResponse = restTemplate.exchange(services_url + "/" + ns_uuid, HttpMethod.GET, entity, String.class);
        JSONObject ns_descriptor = new JSONObject(nsdResponse.getBody());
        JSONArray vnfds = ns_descriptor.getJSONObject("nsd").getJSONArray("network_functions");

        JSONArray ns_metrics = new JSONArray();

        for (int i = 0; i < vnfds.length(); i++) {

            JSONObject vnf = vnfds.getJSONObject(i);

            String services_url_complete = vnfs_url
                    + "?name=" + vnf.getString("vnf_name")
                    + "&vendor=" + vnf.getString("vnf_vendor")
                    + "&version=" + vnf.getString("vnf_version");

            ResponseEntity<String> response1 = restTemplate.exchange(services_url_complete, HttpMethod.GET, entity, String.class);

            JSONArray vnfs_list = new JSONArray(response1.getBody());

            if (vnfs_list.length() > 0) {
                JSONObject vnf_descriptor = vnfs_list.getJSONObject(0);

                if (vnf_descriptor.getJSONObject("vnfd").has("virtual_deployment_units")) {
                    JSONArray virtual_deployment_units = vnf_descriptor.getJSONObject("vnfd").getJSONArray("virtual_deployment_units");

                    for (int j = 0; j < virtual_deployment_units.length(); j++) {
                        JSONObject virtual_deployment_unit = virtual_deployment_units.getJSONObject(j);

                        if (virtual_deployment_unit.has("monitoring_parameters")) {

                            JSONArray monitoring_parameters = virtual_deployment_unit.getJSONArray("monitoring_parameters");

                            for (int k = 0; k < monitoring_parameters.length(); k++) {
                                ns_metrics.put(vnf.getString("vnf_name") + ":" + monitoring_parameters.getJSONObject(k).getString("name"));
                            }

                        }

                        if (virtual_deployment_unit.has("snmp_parameters")) {
                            JSONObject snmp_parameters = virtual_deployment_unit.getJSONObject("snmp_parameters");

                            JSONArray oids = snmp_parameters.getJSONArray("oids");

                            for (int h = 0; h < oids.length(); h++) {

                                JSONObject oid = oids.getJSONObject(h);

                                ns_metrics.put(vnf.getString("vnf_name") + ":" + oid.getString("metric_name"));

                            }
                        }

                    }
                    ns_metrics.put(vnf.getString("vnf_name") + ":cpu_util");
                    ns_metrics.put(vnf.getString("vnf_name") + ":memory_usage");
                    ns_metrics.put(vnf.getString("vnf_name") + ":network_outgoing_bytes_rate");
                    ns_metrics.put(vnf.getString("vnf_name") + ":network_incoming_bytes_rate");

                } else if (vnf_descriptor.getJSONObject("vnfd").has("cloudnative_deployment_units")) {

                    JSONArray cloudnative_deployment_units = vnf_descriptor.getJSONObject("vnfd").getJSONArray("cloudnative_deployment_units");

                    for (int j = 0; j < cloudnative_deployment_units.length(); j++) {
                        JSONObject virtual_deployment_unit = cloudnative_deployment_units.getJSONObject(j);

                        if (virtual_deployment_unit.has("monitoring_parameters")) {

                            JSONArray monitoring_parameters = virtual_deployment_unit.getJSONArray("monitoring_parameters");

                            for (int k = 0; k < monitoring_parameters.length(); k++) {
                                ns_metrics.put(vnf.getString("vnf_name") + ":" + monitoring_parameters.getJSONObject(k).getString("name"));
                            }

                        }

                        if (virtual_deployment_unit.has("snmp_parameters")) {
                            JSONObject snmp_parameters = virtual_deployment_unit.getJSONObject("snmp_parameters");

                            JSONArray oids = snmp_parameters.getJSONArray("oids");

                            for (int h = 0; h < oids.length(); h++) {

                                JSONObject oid = oids.getJSONObject(h);

                                ns_metrics.put(vnf.getString("vnf_name") + ":" + oid.getString("metric_name"));

                            }
                        }

                    }

                    ns_metrics.put(vnf.getString("vnf_name") + ":container_cpu_load_average_10s");
                    ns_metrics.put(vnf.getString("vnf_name") + ":container_memory_usage_bytes");
                    ns_metrics.put(vnf.getString("vnf_name") + ":container_network_receive_bytes_total");
                    ns_metrics.put(vnf.getString("vnf_name") + ":container_network_transmit_bytes_total");

                }

            }
        }

        return ns_metrics;

    }

}
