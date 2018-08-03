/*
 * Copyright (c) 2015 SONATA-NFV, 2017 5GTANGO [, ANY ADDITIONAL AFFILIATION]
 * ALL RIGHTS RESERVED.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Neither the name of the SONATA-NFV, 5GTANGO [, ANY ADDITIONAL AFFILIATION]
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * This work has been performed in the framework of the SONATA project,
 * funded by the European Commission under Grant number 671517 through
 * the Horizon 2020 and 5G-PPP programmes. The authors would like to
 * acknowledge the contributions of their colleagues of the SONATA
 * partner consortium (www.sonata-nfv.eu).
 *
 * This work has been performed in the framework of the 5GTANGO project,
 * funded by the European Commission under Grant number 761493 through
 * the Horizon 2020 and 5G-PPP programmes. The authors would like to
 * acknowledge the contributions of their colleagues of the 5GTANGO
 * partner consortium (www.5gtango.eu).
 */
package eu.tng.policymanager.Messaging;

import eu.tng.policymanager.RulesEngineService;
import eu.tng.policymanager.facts.LogMetric;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.logging.Level;

import org.springframework.stereotype.Component;

import java.util.logging.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class MonitoringListener {

    private static final Logger logger = Logger.getLogger(MonitoringListener.class.getName());

    @Autowired
    RulesEngineService rulesEngineService;

    @Value("${tng.rep}")
    private String tng_rep;

    public void monitoringAlertReceived(LinkedHashMap message) {

        logger.log(Level.INFO, "A new monitoring alert has been received");
        try {

            logger.log(Level.INFO, "monitoring alert   is like this " + message.toString());
            logger.log(Level.INFO, "alert name: " + message.get("alertname"));

            //Consumption of alerts from son-broker
            if (message.containsKey("alertname")) {

                String gnsid = message.get("serviceID").toString();
                String alertname = message.get("alertname").toString();
                String vnfr_id = message.get("functionID").toString();

                //get info from tng-rep
                String repo_url = "http://" + tng_rep + "/vnfrs/" + vnfr_id;

                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> repo_response;
                try {
                    repo_response = restTemplate.exchange(repo_url, HttpMethod.GET, entity, String.class);

                } catch (Exception e) {
                    logger.info("There was a communication problem with tng-repo" + repo_url);
                    logger.warning(" tng-repo response status code: " + e.getMessage());
                    return;
                }

                JSONObject vnfr_info = new JSONObject(repo_response.getBody());

                JSONObject vdu_ref = vnfr_info.getJSONArray("virtual_deployment_units").getJSONObject(0);

                String vnfd_id = vnfr_info.getString("descriptor_reference");

                if (vdu_ref.has("vdu_reference")) {
                    String vnf_name = vdu_ref.getString("vdu_reference").split(":")[0];

                    JSONObject vnfc_instance = vdu_ref.getJSONArray("vnfc_instance").getJSONObject(0);

                    String vim_id = vnfc_instance.getString("vim_id");

                    LogMetric logMetric = new LogMetric("s" + gnsid.replaceAll("-", ""), vnf_name, alertname, vnfd_id, vim_id);

                    logger.info("create log fact " + logMetric.toString());
                    rulesEngineService.createLogFact(logMetric);
                }

            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception message {0}", e.getMessage());
        }

    }

}
