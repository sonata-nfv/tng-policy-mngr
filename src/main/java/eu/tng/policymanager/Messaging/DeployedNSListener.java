package eu.tng.policymanager.Messaging;

import eu.tng.policymanager.RulesEngineService;
import eu.tng.policymanager.repository.dao.RuntimePolicyRepository;
import eu.tng.policymanager.repository.domain.RuntimePolicy;
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
                runtimepolicy = runtimePolicyRepository.findByNsridAndDefaultPolicyTrue(ns_id);
            } else {
                logger.log(Level.INFO, "Check for policy  binded with SLA " + sla_id);
                runtimepolicy = runtimePolicyRepository.findBySlaid(sla_id);
            }
            
            if (runtimepolicy.isPresent()) {
                logger.log(Level.INFO, "Activate policy for NSR " + nsr_id);
                rulesEngineService.addNewKnowledgebase(nsr_id.replaceAll("-", ""), runtimepolicy.get().getPolicyid());
            } else {
                logger.log(Level.INFO, "NSR " + nsr_id + " is deployed withoun any policy");

            }

        }

    }
}
