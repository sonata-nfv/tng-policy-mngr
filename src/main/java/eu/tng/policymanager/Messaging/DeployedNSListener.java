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
import static eu.tng.policymanager.config.DroolsConfig.POLICY_DESCRIPTORS_PACKAGE;
import eu.tng.policymanager.repository.MonitoringRule;
import eu.tng.policymanager.repository.PolicyYamlFile;
import eu.tng.policymanager.repository.dao.RuntimePolicyRecordRepository;
import eu.tng.policymanager.repository.dao.RuntimePolicyRepository;
import eu.tng.policymanager.repository.domain.RuntimePolicy;
import eu.tng.policymanager.repository.domain.RuntimePolicyRecord;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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

    private static final String current_dir = System.getProperty("user.dir");

    public void deployedNSMessageReceived(LinkedHashMap message) {
        logger.log(Level.INFO, "A new service is Deployed: {0}", message);

        String status = message.get("status").toString();
        logger.log(Level.INFO, "status " + status);

        if (status.equalsIgnoreCase("READY")) {

            JSONObject newDeployedGraph = new JSONObject(message);

            String nsr_id = newDeployedGraph.getJSONObject("nsr").getString("id");
            String sla_id = newDeployedGraph.getString("sla_id");
            String ns_id = newDeployedGraph.getJSONObject("nsr").getString("descriptor_reference");

            Optional<RuntimePolicy> runtimepolicy;
            if (sla_id == null || sla_id.equalsIgnoreCase("null")) {
                logger.log(Level.INFO, "Check for default policy for ns " + ns_id);
                runtimepolicy = runtimePolicyRepository.findByNsidAndDefaultPolicyTrue(ns_id);
            } else {
                logger.log(Level.INFO, "Check for policy  binded with SLA " + sla_id + " and NS " + ns_id);
                runtimepolicy = runtimePolicyRepository.findBySlaidAndNsid(sla_id, ns_id);
            }

            if (runtimepolicy.isPresent()) {
                logger.log(Level.INFO, "Activate policy for NSR " + nsr_id);
                rulesEngineService.addNewKnowledgebase("s" + nsr_id.replaceAll("-", ""), runtimepolicy.get().getPolicyid());

                // update dbpolicy mongo repo
                RuntimePolicyRecord policyrecord = new RuntimePolicyRecord();
                policyrecord.setNsrid(nsr_id);
                policyrecord.setPolicyid(runtimepolicy.get().getPolicyid());
                runtimePolicyRecordRepository.save(policyrecord);

                //submit monitoring-rules to son-broker
                //fecth monitoring rules from policy
                //1. Fech yml file
                File policydescriptor = new File(current_dir + "/" + POLICY_DESCRIPTORS_PACKAGE + "/" + runtimepolicy.get().getPolicyid() + ".yml");
                logger.info("get file from - " + current_dir + "/" + POLICY_DESCRIPTORS_PACKAGE + "/" + runtimepolicy.get().getPolicyid() + ".yml");
                PolicyYamlFile policyyml = PolicyYamlFile.readYaml(policydescriptor);

                //2. create hashmap with monitoring rules
                List<MonitoringRule> monitoringRules = policyyml.getMonitoring_rules();

                //3. construct prometheus rules
                JSONObject prometheous_rules = new JSONObject();
                prometheous_rules.put("plc_cnt", nsr_id);

                JSONArray prometheous_vnfs = new JSONArray();

                //parse newDeployedGraph
                JSONArray vnfrs = newDeployedGraph.getJSONArray("vnfrs");

                for (int i = 0; i < vnfrs.length(); i++) {

                    JSONObject prometheus_vnf = new JSONObject();

                    JSONObject vnfr_object = vnfrs.getJSONObject(i);

                    String vnfr_id = vnfr_object.getString("id"); //or descriptor_reference to ask
                    prometheus_vnf.put("nvfid", vnfr_id);

                    JSONArray prometheus_vdus = new JSONArray();
                    JSONArray virtual_deployment_units = vnfr_object.getJSONArray("virtual_deployment_units");

                    for (int j = 0; j < virtual_deployment_units.length(); j++) {

                        JSONObject virtual_deployment_unit = virtual_deployment_units.getJSONObject(j);

                        String vdu_reference = virtual_deployment_unit.getString("vdu_reference");
                        JSONArray vnfc_instances = virtual_deployment_unit.getJSONArray("vnfc_instance");

                        //MonitoringRule monitoringRule = (MonitoringRule) monitoring_rules_hashmap.get(vdu_reference);
                        for (int k = 0; k < vnfc_instances.length(); k++) {

                            JSONObject vnfc_instance = vnfc_instances.getJSONObject(k);
                            JSONObject prometheus_vdu = new JSONObject();
                            String vc_id = vnfc_instance.getString("vc_id");
                            prometheus_vdu.put("vdu_id", vc_id);

                            //add prometheus rules
                            JSONArray prometheus_rules = new JSONArray();

                            for (MonitoringRule monitoringRule : monitoringRules) {

                                String policy_vdu_reference = monitoringRule.getName().split(":")[2] + ":" + monitoringRule.getName().split(":")[4];

                                if (policy_vdu_reference.equalsIgnoreCase(vdu_reference)) {

                                    JSONObject prometheus_rule = new JSONObject();

                                    prometheus_rule.put("name", monitoringRule.getName().replace(":", "_").replace("-", "_"));
                                    prometheus_rule.put("duration", monitoringRule.getDuration() + monitoringRule.getDuration_unit());
                                    prometheus_rule.put("description", monitoringRule.getDescription());
                                    prometheus_rule.put("summary", "");
                                    prometheus_rule.put("notification_type", new JSONObject("{\"id\": 2,\"type\":\"rabbitmq\"}"));
                                    logger.info("monitoringRule condition " + monitoringRule.getCondition());
                                    
                                    prometheus_rule.put("condition", monitoringRule.getCondition() + "{resource_id=\"" + vc_id + "\"}" + monitoringRule.getThreshold());

                                    prometheus_rules.put(prometheus_rule);
                                }
                            }

                            prometheus_vdu.put("rules", prometheus_rules);
                            prometheus_vdus.put(prometheus_vdu);

                        }

                    }
                    prometheus_vnf.put("vdus", prometheus_vdus);
                    prometheous_vnfs.put(prometheus_vnf);

                }

                prometheous_rules.put("vnfs", prometheous_vnfs);

                logger.info("prometheous_rules " + prometheous_rules);

                // Create PLC rules to son-monitor
                String monitoring_url = "http://" + monitoring_manager + "/api/v1/policymng/rules/service/" + nsr_id + "/configuration";
                logger.info("monitoring_manager " + monitoring_url);
                try {
                    String monitoring_response = dopostcall(monitoring_url, prometheous_rules);
                    logger.info("monitoring_response " + monitoring_response);
                } catch (IOException ex) {
                    Logger.getLogger(DeployedNSListener.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                logger.log(Level.INFO, "NSR " + nsr_id + " is deployed withoun any policy");

            }

        }

    }

    private String dopostcall(String url, JSONObject prometheous_rules) throws IOException {

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, prometheous_rules.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("content-type", "application/json")
                .build();

        Response response = client.newCall(request).execute();

        return response.body().string() + " with message " + response.message();
    }
}
