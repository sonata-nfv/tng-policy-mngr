/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.rules.generation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import eu.tng.policymanager.Messaging.DeployedNSListener;
import eu.tng.policymanager.repository.MonitoringRule;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
@Component
public class Util {

    private static final Logger logger = Logger.getLogger(Util.class.getName());

    public static String convertYamlToJson(String yaml) {
        try {
            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            Object obj = yamlReader.readValue(yaml, Object.class);

            ObjectMapper jsonWriter = new ObjectMapper();
            return jsonWriter.writeValueAsString(obj);
        } catch (IOException ex) {
            Logger.getLogger(DeployedNSListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public static String jsonToYaml(JSONObject jsonobject) {
        Yaml yaml = new Yaml();

        // get json string
        String prettyJSONString = jsonobject.toString(4);
        // mapping
        Map<String, Object> map = (Map<String, Object>) yaml.load(prettyJSONString);
        // convert to yaml string (yaml formatted string)
        String output = yaml.dump(map);
        //logger.info(output);
        return output;
    }

    public static JSONObject compose_monitoring_rules_os(String nsr_id, JSONObject vnfr_object, List<MonitoringRule> monitoringRules) {

        //JSONObject prometheous_vnfs = new JSONObject();

        JSONObject prometheus_vnf = new JSONObject();

        String vnfr_id = vnfr_object.getString("id"); //or descriptor_reference to ask
        prometheus_vnf.put("vnf_id", vnfr_id);

        JSONArray prometheus_vdus = new JSONArray();
        JSONArray virtual_deployment_units = vnfr_object.getJSONArray("virtual_deployment_units");

        for (int j = 0; j < virtual_deployment_units.length(); j++) {

            JSONObject virtual_deployment_unit = virtual_deployment_units.getJSONObject(j);

            logger.info("virtual_deployment_unit--> " + virtual_deployment_unit);

            String vdu_reference = virtual_deployment_unit.getString("vdu_reference");
            JSONArray vnfc_instances = virtual_deployment_unit.getJSONArray("vnfc_instance");

            //MonitoringRule monitoringRule = (MonitoringRule) monitoring_rules_hashmap.get(vdu_reference);
            for (int k = 0; k < vnfc_instances.length(); k++) {

                JSONObject vnfc_instance = vnfc_instances.getJSONObject(k);

                logger.info("vnfc_instance--> " + vnfc_instance);

                JSONObject prometheus_vdu = new JSONObject();
                String vc_id = vnfc_instance.getString("vc_id");
                prometheus_vdu.put("vdu_id", vc_id);

                //add prometheus rules
                JSONArray prometheus_rules = new JSONArray();

                for (MonitoringRule monitoringRule : monitoringRules) {

                    logger.info("MonitoringRule--> " + monitoringRule.toString());

                    //Formatted like this : <vnf_name>:<vdu_id>-<record_id>
                    String policy_vdu_reference = monitoringRule.getName().split(":")[0]
                            + ":" + monitoringRule.getName().split(":")[1]
                            + "-" + vnfr_id;

                    logger.info("policy_vdu_reference--> " + policy_vdu_reference);
                    logger.info("vdu_reference--> " + vdu_reference);

                    if (vdu_reference.equals(policy_vdu_reference)) {

                        JSONObject prometheus_rule = new JSONObject();

                        String rule_prefix = nsr_id.substring(0, Math.min(nsr_id.length(), 8));
                        String rule_name = monitoringRule.getName().replace(":", "_").replace("-", "_") + "_" + rule_prefix;

                        if (rule_name.length() > 60) {
                            logger.info("Monitoring rule name is too large.it must not be more than 50 characters");
                            rule_name = rule_name.substring(0, Math.min(rule_name.length(), 59));
                        }

                        prometheus_rule.put("name", rule_name);

                        logger.info("rule name-->" + rule_name);

                        prometheus_rule.put("duration", monitoringRule.getDuration() + monitoringRule.getDuration_unit());
                        prometheus_rule.put("description", monitoringRule.getDescription());
                        prometheus_rule.put("summary", "");
                        prometheus_rule.put("notification_type", new JSONObject("{\"id\": 2,\"type\":\"rabbitmq\"}"));
                        logger.info("monitoringRule condition " + monitoringRule.getCondition());

                        prometheus_rule.put("condition", monitoringRule.getCondition() + "{resource_id=\"" + vc_id + "\"} " + monitoringRule.getThreshold());

                        prometheus_rules.put(prometheus_rule);
                    }
                }

                prometheus_vdu.put("rules", prometheus_rules);
                prometheus_vdus.put(prometheus_vdu);

            }

        }

        prometheus_vnf.put("vdus", prometheus_vdus);
        //prometheous_vnfs.put(prometheus_vnf);

        return prometheus_vnf;

    }

    public static JSONObject compose_monitoring_rules_k8s(String nsr_id, JSONObject vnfr_object, List<MonitoringRule> monitoringRules) {

        //JSONArray prometheous_vnfs = new JSONArray();

        JSONObject prometheus_vnf = new JSONObject();

        String vnfr_id = vnfr_object.getString("id"); //or descriptor_reference to ask
        prometheus_vnf.put("vnf_id", vnfr_id);

        JSONArray prometheus_vdus = new JSONArray();
        JSONArray cloudnative_deployment_units = vnfr_object.getJSONArray("cloudnative_deployment_units");

        for (int j = 0; j < cloudnative_deployment_units.length(); j++) {

            JSONObject cloudnative_deployment_unit = cloudnative_deployment_units.getJSONObject(j);

            logger.log(Level.INFO, "cloudnative_deployment_unit--> {0}", cloudnative_deployment_unit);

            String vdu_reference = cloudnative_deployment_unit.getString("cdu_reference");

            JSONObject prometheus_vdu = new JSONObject();
            String vc_id = vdu_reference.split(":")[1];
            prometheus_vdu.put("vdu_id", vc_id);

            //add prometheus rules
            JSONArray prometheus_rules = new JSONArray();

            for (MonitoringRule monitoringRule : monitoringRules) {

                logger.info("MonitoringRule--> " + monitoringRule.toString());

                //Formatted like this : <vnf_name>:<vdu_id>-<record_id>
                String policy_vdu_reference = monitoringRule.getName().split(":")[0]
                        + ":" + monitoringRule.getName().split(":")[1]
                        + "-" + vnfr_id;

                logger.info("policy_vdu_reference--> " + policy_vdu_reference);
                logger.info("vdu_reference--> " + vdu_reference);

                if (vdu_reference.equals(policy_vdu_reference)) {

                    JSONObject prometheus_rule = new JSONObject();

                    String rule_prefix = nsr_id.substring(0, Math.min(nsr_id.length(), 8));
                    String rule_name = monitoringRule.getName().replace(":", "_").replace("-", "_") + "_" + rule_prefix;

                    if (rule_name.length() > 60) {
                        logger.info("Monitoring rule name is too large.it must not be more than 50 characters");
                        rule_name = rule_name.substring(0, Math.min(rule_name.length(), 59));
                    }

                    prometheus_rule.put("name", rule_name);

                    logger.info("rule name-->" + rule_name);

                    prometheus_rule.put("duration", monitoringRule.getDuration() + monitoringRule.getDuration_unit());
                    prometheus_rule.put("description", monitoringRule.getDescription());
                    prometheus_rule.put("summary", "");
                    prometheus_rule.put("notification_type", new JSONObject("{\"id\": 2,\"type\":\"rabbitmq\"}"));
                    logger.info("monitoringRule condition " + monitoringRule.getCondition());

                    prometheus_rule.put("condition", monitoringRule.getCondition() + "{container_name=\"" + vc_id + "\"} " + monitoringRule.getThreshold());

                    prometheus_rules.put(prometheus_rule);
                    System.out.println("prometheus_rule--->" + prometheus_rule);

                }
            }

            prometheus_vdu.put("rules", prometheus_rules);
            System.out.println("prometheus_rules--->" + prometheus_rules);
            prometheus_vdus.put(prometheus_vdu);

        }

        prometheus_vnf.put("vdus", prometheus_vdus);
        //prometheous_vnfs.put(prometheus_vnf);

        return prometheus_vnf;

    }

    public static String sendPrometheusRulesToMonitoringManager(String url, JSONObject prometheous_rules) throws IOException {

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
