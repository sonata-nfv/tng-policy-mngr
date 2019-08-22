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

import eu.tng.policymanager.RulesEngineApp;
import eu.tng.policymanager.RulesEngineService;
import eu.tng.policymanager.repository.MonitoringRule;
import eu.tng.policymanager.repository.PolicyYamlFile;
import eu.tng.policymanager.repository.dao.RuntimePolicyRecordRepository;
import eu.tng.policymanager.repository.dao.RuntimePolicyRepository;
import eu.tng.policymanager.repository.domain.RuntimePolicy;
import eu.tng.policymanager.repository.domain.RuntimePolicyRecord;
import eu.tng.policymanager.rules.generation.Util;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import org.springframework.stereotype.Component;
import java.util.logging.Logger;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class DeployedNSListener {

    private static final Logger logger = Logger.getLogger(DeployedNSListener.class.getName());

    @Autowired
    RulesEngineService rulesEngineService;

    @Autowired
    RuntimePolicyRepository runtimePolicyRepository;

    @Autowired
    RuntimePolicyRecordRepository runtimePolicyRecordRepository;

    @Value("${monitoring.manager}")
    private String monitoring_manager;

    @Value("${tng.cat.policies}")
    private String policies_url;

    //private static final String current_dir = System.getProperty("user.dir");
    @RabbitListener(queues = RulesEngineApp.NS_INSTATIATION_QUEUE)
    public void deployedNSMessageReceived(byte[] message) {

        logger.log(Level.INFO, "A new message has been received");
        
        String deployedNSasYaml = new String(message, StandardCharsets.UTF_8);
        
        try {
            enforceRuntimePolicy(deployedNSasYaml);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception message {0}", e.getMessage());
        }

    }



    private void enforceRuntimePolicy(String deployedNSasYaml) {

        String jsonobject = Util.convertYamlToJson(deployedNSasYaml);
        JSONObject newDeployedGraph = new JSONObject(jsonobject);

        if (newDeployedGraph.has("status")) {

            String status = newDeployedGraph.get("status").toString();

            if (status.equalsIgnoreCase("READY")) {

                if (newDeployedGraph.has("nsr")) {

                    String ns_id = newDeployedGraph.getJSONObject("nsr").getString("descriptor_reference");

                    logger.log(Level.INFO, "status {0} for nsr_id {1}", new Object[]{status, ns_id});

                    logger.log(Level.INFO, "A new service is Deployed: {0}", deployedNSasYaml);

                    String nsr_id = newDeployedGraph.getJSONObject("nsr").getString("id");
                    RuntimePolicy runtimepolicy = null;

                    if (newDeployedGraph.has("sla_id")) {

                        Object sla_id = newDeployedGraph.get("sla_id");

                        if (!sla_id.equals(null)) {

                            logger.log(Level.INFO, "Check for policy  binded with SLA {0} and NS {1}", new Object[]{sla_id.toString(), ns_id});

                            List<RuntimePolicy> p_list = runtimePolicyRepository.findBySlaidAndNsid(sla_id.toString(), ns_id);

                            if (p_list.size() > 0) {
                                runtimepolicy = p_list.get(0);
                            }

                        } else {

                            logger.log(Level.INFO, "Check for default policy for ns {0}", ns_id);

                            Optional<RuntimePolicy> plc = runtimePolicyRepository.findByNsidAndDefaultPolicyTrue(ns_id);
                            if (plc.isPresent()) {
                                runtimepolicy = plc.get();
                            }

                        }

                    } else {
                        logger.log(Level.INFO, "Check for default policy for ns {0}", ns_id);
                        runtimepolicy = runtimePolicyRepository.findByNsidAndDefaultPolicyTrue(ns_id).get();
                    }

                    if (runtimepolicy != null) {

                        String runtimepolicy_id = runtimepolicy.getPolicyid();

                        //1. Fech yml file from catalogues
                        RestTemplate restTemplate = new RestTemplate();
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                        HttpEntity<String> entity = new HttpEntity<>(headers);
                        ResponseEntity<String> response;
                        try {
                            response = restTemplate.exchange(policies_url + "/" + runtimepolicy_id, HttpMethod.GET, entity, String.class);
                        } catch (HttpClientErrorException e) {
                            logger.warning("{\"error\": \"The PLD ID " + runtimepolicy_id + " does not exist at catalogues. Message : "
                                    + e.getMessage() + "\"}");
                            return;
                        }

                        JSONObject policydescriptorRaw = new JSONObject(response.getBody());
                        logger.info("response" + policydescriptorRaw.toString());

                        JSONObject pld = policydescriptorRaw.getJSONObject("pld");

                        String policyAsYaml = Util.jsonToYaml(pld);

                        logger.log(Level.INFO, "Activate policy for NSR {0}", nsr_id);
                        boolean is_enforcement_succesfull = rulesEngineService.addNewKnowledgebase("s" + nsr_id.replaceAll("-", ""), runtimepolicy.getPolicyid(), policyAsYaml);

                        if (is_enforcement_succesfull) {

                            //submit monitoring-rules to son-broker
                            //fecth monitoring rules from policy
                            // update dbpolicy mongo repo
                            RuntimePolicyRecord policyrecord = new RuntimePolicyRecord();
                            policyrecord.setNsrid(nsr_id);
                            policyrecord.setPolicyid(runtimepolicy_id);
                            runtimePolicyRecordRepository.save(policyrecord);

                            PolicyYamlFile policyyml = PolicyYamlFile.readYaml(policyAsYaml);

                            //2. create hashmap with monitoring rules
                            List<MonitoringRule> monitoringRules = policyyml.getMonitoring_rules();

                            //3. construct prometheus rules
                            JSONObject prometheous_rules = new JSONObject();
                            prometheous_rules.put("plc_cnt", nsr_id);
                            prometheous_rules.put("sonata_service_id", nsr_id);
                            

                            //parse newDeployedGraph
                            JSONArray vnfrs = newDeployedGraph.getJSONArray("vnfrs");

                            for (int i = 0; i < vnfrs.length(); i++) {

                                JSONObject vnfr_object = vnfrs.getJSONObject(i);

                                logger.info("vnfr_object--> " + vnfr_object);

                                JSONArray prometheous_vnfs = new JSONArray();
                                if (vnfr_object.has("virtual_deployment_units")) {
                                    prometheous_vnfs = Util.compose_monitoring_rules_os(nsr_id, vnfr_object, monitoringRules);
                                } else if (vnfr_object.has("cloudnative_deployment_units")) {
                                    prometheous_vnfs = Util.compose_monitoring_rules_k8s(nsr_id, vnfr_object, monitoringRules);
                                }

                                prometheous_rules.put("vnfs", prometheous_vnfs);

                                logger.info("prometheous_rules " + prometheous_rules);

                                // Create PLC rules to son-monitor
                                //String monitoring_url = "http://" + monitoring_manager + "/api/v1/policymng/rules/service/" + nsr_id + "/configuration";
                                String monitoring_url = "http://" + monitoring_manager + "/api/v2/policies/monitoring-rules";

                                
                                logger.info("monitoring_manager " + monitoring_url);
                                try {
                                    String monitoring_response = Util.sendPrometheusRulesToMonitoringManager(monitoring_url, prometheous_rules);
                                    logger.info("monitoring_response " + monitoring_response);
                                } catch (IOException ex) {
                                    Logger.getLogger(DeployedNSListener.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }

                        } else {
                            logger.log(Level.INFO, "NSR " + nsr_id + " is deployed withoun any policy");

                        }

                    }

                }

            }
        }

    }


}
