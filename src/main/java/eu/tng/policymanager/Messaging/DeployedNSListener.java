package eu.tng.policymanager.Messaging;

import eu.tng.policymanager.RulesEngineService;
import eu.tng.policymanager.repository.dao.RuntimePolicyRecordRepository;
import eu.tng.policymanager.repository.dao.RuntimePolicyRepository;
import eu.tng.policymanager.repository.domain.RuntimePolicy;
import eu.tng.policymanager.repository.domain.RuntimePolicyRecord;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.logging.Level;
import org.springframework.stereotype.Component;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class DeployedNSListener {

    private static final Logger logger = Logger.getLogger(DeployedNSListener.class.getName());

    @Autowired
    RulesEngineService rulesEngineService;

    @Autowired
    RuntimePolicyRepository runtimePolicyRepository;

    @Autowired
    RuntimePolicyRecordRepository runtimePolicyRecordRepository;

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
                logger.log(Level.INFO, "Check for policy  binded with SLA " + sla_id);
                runtimepolicy = runtimePolicyRepository.findBySlaid(sla_id);
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
                
            } else {
                logger.log(Level.INFO, "NSR " + nsr_id + " is deployed withoun any policy");

            }

        }

    }
}
