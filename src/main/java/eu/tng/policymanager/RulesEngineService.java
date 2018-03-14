/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager;

import com.google.gson.Gson;
import eu.tng.policymanager.GPolicy.GPolicy;
import eu.tng.policymanager.facts.RuleActionType;
import static eu.tng.policymanager.config.DroolsConfig.POLICY_DESCRIPTORS_PACKAGE;
import static eu.tng.policymanager.config.DroolsConfig.RULESPACKAGE;
import eu.tng.policymanager.facts.action.Action;
import eu.tng.policymanager.facts.action.ComponentResourceAllocationAction;
import eu.tng.policymanager.facts.LogMetric;
import eu.tng.policymanager.facts.MonitoredComponent;
import eu.tng.policymanager.facts.action.NetworkManagementAction;
import eu.tng.policymanager.repository.PolicyRule;
import eu.tng.policymanager.rules.generation.KieUtil;
import eu.tng.policymanager.repository.PolicyYamlFile;
import eu.tng.policymanager.repository.RuleCondition;
import eu.tng.policymanager.transferobjects.MonitoringMessageTO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.rule.FactHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.kie.api.builder.ReleaseId;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.internal.io.ResourceFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.yaml.snakeyaml.Yaml;

@Service
public class RulesEngineService {

    private static final Logger logger = Logger.getLogger(RulesEngineService.class.getName());
    private static final String rulesPackage = "rules";
    private static final String current_dir = System.getProperty("user.dir");
    ReleaseId releaseId = KieServices.Factory.get().newReleaseId("eu.tng", "policymanager", "1.0");

    private final KieServices kieServices;
    private final KieFileSystem kieFileSystem;
    private final KieModuleModel kieModuleModel;

    @Autowired
    KieContainer kieContainer;

    private final KieUtil kieUtil;

    @Autowired
    private RabbitTemplate template;

    @Qualifier("runtimeActionsQueue")
    @Autowired
    private Queue queue;

    @Autowired
    PolicyYamlFile policyYamlFile;

    @Autowired
    GPolicy gPolicy;

    @Autowired
    public RulesEngineService(KieUtil kieUtil) {
        logger.info("Rule Engine Session initializing...");
        this.kieServices = KieServices.Factory.get();
        this.kieFileSystem = kieServices.newKieFileSystem();
        this.kieModuleModel = kieServices.newKieModuleModel();
        this.kieUtil = kieUtil;

    }

    //fireAllRules every 5 minutes 1min== 60000
    @Scheduled(fixedRate = 60000)
    public void fireAllRulesOfRecommendationEngine() {

        logger.info("Search for actions");

        String factSessionName = "RulesEngineSession_gsgpilotTranscodingService";
        KieSession kieSession = (KieSession) kieUtil.seeThreadMap().get(factSessionName);

        List<Action> doactions = findAction(kieSession);

        if (doactions.size() > 0) {

            for (Action doaction : doactions) {

                if (doaction instanceof ComponentResourceAllocationAction) {
                    ComponentResourceAllocationAction doactionsubclass = (ComponentResourceAllocationAction) doaction;
                    template.convertAndSend(queue.getName(), doactionsubclass.toString());
                    System.out.println(" [x] Sent '" + doactionsubclass.toString() + "'");
                }

                if (doaction instanceof NetworkManagementAction) {
                    NetworkManagementAction doactionsubclass = (NetworkManagementAction) doaction;
                    template.convertAndSend(queue.getName(), doactionsubclass.toString());
                    System.out.println(" [x] Sent '" + doactionsubclass.toString() + "'");
                }

            }

        }

    }

    /*
     Add a new knowledge base & session & corresponding rules so as to update kieModule
     */
    public void addKnowledgebasePerGroundedGraphTR() {

        String knowledgebasename = "gsgpilotTranscodingService";

        System.out.println("knowledgebasename" + knowledgebasename);
        KieBaseModel kieBaseModel1 = kieModuleModel.newKieBaseModel("GSGKnowledgeBase_" + knowledgebasename).setDefault(true)
                .setEventProcessingMode(EventProcessingOption.STREAM);

        kieBaseModel1.addPackage(rulesPackage + "." + knowledgebasename);

        String factSessionName = "RulesEngineSession_" + knowledgebasename;
        kieBaseModel1.newKieSessionModel(factSessionName).setClockType(ClockTypeOption.get("realtime"));

    }

    public KieContainer lanchKieContainerTR() {

        Double newversion = Double.parseDouble(releaseId.getVersion()) + 0.1;
        ReleaseId releaseId2 = kieServices.newReleaseId("eu.tng", "policymanager", newversion.toString());

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);

        kieFileSystem.generateAndWritePomXML(releaseId2);

        kieFileSystem.writeKModuleXML(kieModuleModel.toXML());
        logger.log(java.util.logging.Level.INFO, "kieModuleModel--ToXML\n{0}", kieModuleModel.toXML());

        this.loadRulesFromFile();

        kieBuilder.buildAll();

        if (kieBuilder.getResults()
                .hasMessages(Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kieBuilder.getResults().toString());
        }

        kieContainer = kieServices.newKieContainer(releaseId2);

        return kieContainer;

    }

    protected void createFact(MonitoringMessageTO monitoringMessageTO) {

        String factKnowledgebase = "GSGKnowledgeBase_gsg" + monitoringMessageTO.getGsgid();

        Collection<String> kiebases = kieContainer.getKieBaseNames();

        if (!kiebases.contains(factKnowledgebase)) {
            logger.log(java.util.logging.Level.WARNING, "Missing Knowledge base {0}", factKnowledgebase);
            return;
        }

        String factSessionName = "RulesEngineSession_gsg" + monitoringMessageTO.getGsgid();
        KieSession kieSession = (KieSession) kieUtil.seeThreadMap().get(factSessionName);

        EntryPoint monitoringStream = kieSession.getEntryPoint("MonitoringStream");

        System.out.println("Ιnsert monitoredComponent to session with nodeid " + monitoringMessageTO.getNodeid() + " metric name " + monitoringMessageTO.getMetricName() + " , value = " + monitoringMessageTO.getMetricValue() + " , and gsgid " + monitoringMessageTO.getGsgid());
        MonitoredComponent monitoredComponent = new MonitoredComponent(monitoringMessageTO.getNodeid(),
                monitoringMessageTO.getMetricName(),
                Double.valueOf(monitoringMessageTO.getMetricValue()),
                monitoringMessageTO.getGsgid());

        monitoringStream.insert(monitoredComponent);

    }

    public void createLogFact(LogMetric logMetric) {

        String factKnowledgebase = "GSGKnowledgeBase_gsg" + logMetric.getGnsid();

        Collection<String> kiebases = kieContainer.getKieBaseNames();

        if (!kiebases.contains(factKnowledgebase)) {
            logger.log(java.util.logging.Level.WARNING, "Missing Knowledge base {0}", factKnowledgebase);
            return;
        }

        String factSessionName = "RulesEngineSession_gsg" + logMetric.getGnsid();
        KieSession kieSession = (KieSession) kieUtil.seeThreadMap().get(factSessionName);

        System.out.println("Ιnsert logmetric fact: " + logMetric.toString());

        kieSession.insert(logMetric);

    }

    /**
     * Search the {@link KieSession} for bus passes.
     */
    private List<Action> findAction(KieSession kieSession) {

        // Find all DoAction facts and 1st generation child classes of DoAction.
        ObjectFilter doActionFilter = new ObjectFilter() {
            @Override
            public boolean accept(Object object) {
                if (Action.class.equals(object.getClass())) {
                    return true;
                }

                if (Action.class.equals(object.getClass().getSuperclass())) {
                    return true;
                }
                return false;
            }
        };
        System.out.println("------New FACT----------");

        List<Action> facts = new ArrayList<Action>();

        for (FactHandle handle : kieSession.getFactHandles(doActionFilter)) {

            facts.add((Action) kieSession.getObject(handle));
        }

        if (facts.size() > 0) {
            logger.log(java.util.logging.Level.INFO, "Num of created facts{0}", facts.size());
        }

        return facts;
    }

    /**
     * Print out details of all facts in working memory. Handy for debugging.
     */
    @SuppressWarnings("unused")
    private void printFactsMessage(KieSession kieSession) {
        Collection<FactHandle> allHandles = kieSession.getFactHandles();

        String msg = "\nAll facts:\n";
        for (FactHandle handle : allHandles) {
            msg += "    " + kieSession.getObject(handle) + "\n";
        }
        System.out.println(msg);

        EntryPoint monitoringstream = kieSession.getEntryPoint("MonitoringStream");
        Collection<FactHandle> allHandles1 = monitoringstream.getFactHandles();

        String msg1 = "\nAll facts of stream:\n";
        for (FactHandle handle : allHandles1) {
            msg1 += "    " + monitoringstream.getObject(handle) + "\n";
        }
        System.out.println(msg1);

    }

    public String mapActionType(String actionType) {
        String enumedActionType;

        switch (actionType) {
            case "Alert Message":
                enumedActionType = RuleActionType.ALERT_MESSAGE.toString();
                break;
            case "Iaas Management Function":
                enumedActionType = RuleActionType.IAAS_MANAGEMENT.toString();
                break;
            case "Virtual Function":
                enumedActionType = RuleActionType.VIRTUAL_FUNCTION.toString();
                break;
            case "Component Conf Parameter":
                enumedActionType = RuleActionType.COMPONENT_CONFIGURATION.toString();
                break;
            case "Lifecycle Action":
                enumedActionType = RuleActionType.COMPONENT_LIFECYCLE_MANAGEMENT.toString();
                break;
            default:
                throw new IllegalArgumentException("Invalid action type: " + actionType);
        }
        return enumedActionType;
    }

    private void loadRulesFromFile() {
        logger.info("Loading Rules from File to production memory");
        String knowledgebasename = "gsgpilotTranscodingService";
        String drlPath4deployment = "/rules/gsgpilotTranscodingService/gsgpilotTranscodingService.drl";
        try {

            Path policyPackagePath = Paths.get(current_dir + "/" + RULESPACKAGE + "/" + knowledgebasename);
            String data = "";
            //1st add default rules
            data += getRulesFromFile();

            logger.info("rules : " + data);

            Files.createDirectories(policyPackagePath);
            FileOutputStream out = new FileOutputStream(current_dir + "/" + drlPath4deployment);
            out.write(data.getBytes());
            out.close();
            logger.info("Writing rules at: " + current_dir + "/" + RULESPACKAGE + "/" + knowledgebasename + "/" + knowledgebasename + ".drl");
            kieFileSystem.write(ResourceFactory.newFileResource(current_dir + "/" + RULESPACKAGE + "/" + knowledgebasename + "/" + knowledgebasename + ".drl"));
        } catch (Exception ex) {
            logger.warning("Error during the creation of production memory:" + ex.getMessage());
        }
    }//EoM 

    private String getRulesFromFile() {

        String ret = "";
        try {
            //InputStream inputstream = this.getClass().getResourceAsStream("/rules/gsgpilotTranscodingService/gsgpilotTranscodingService.drl");
            InputStream inputstream = new FileInputStream(current_dir + "/rules/gsgpilotTranscodingService/gsgpilotTranscodingService.drl");

            ret = IOUtils.toString(inputstream, "UTF-8");

        } //EoM  
        catch (IOException ex) {
            Logger.getLogger(RulesEngineService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return ret;
    }

    /*
     Add a new knowledge base & session & corresponding rules so as to update kieModule
     */
    public void addNewKnowledgebase(String gnsid, String policyname) {

        Collection<String> kiebases = kieContainer.getKieBaseNames();
        String factKnowledgebase = "GSGKnowledgeBase_gsg" + gnsid;

        if ("null".equals(policyname) || policyname == null) {
            logger.log(java.util.logging.Level.WARNING, "Grounded Service graph is deploed with none policy assigned");
            return;
        }

        if (kiebases.contains(factKnowledgebase)) {
            logger.log(java.util.logging.Level.WARNING, "Knowledge base {0} already added", factKnowledgebase);
            return;
        }

        File f = new File(current_dir + "/" + POLICY_DESCRIPTORS_PACKAGE + "/" + policyname + ".yml");
        if (!f.exists() && !f.isDirectory()) {
            logger.log(java.util.logging.Level.WARNING, "Policy with name {0} does not exist at Catalogues", policyname);
            return;
        }

        //GroundedServicegraph groundedServicegraph = (GroundedServicegraph) groundedServiceGraphManagement.getGroundedServicegraph(groundedServicegraphid);
        String knowledgebasename = "gsg" + gnsid;

        System.out.println("knowledgebasename" + knowledgebasename);
        KieBaseModel kieBaseModel1 = kieModuleModel.newKieBaseModel("ArcadiaGSGKnowledgeBase_" + knowledgebasename).setDefault(true).setEventProcessingMode(EventProcessingOption.STREAM);

        kieBaseModel1.addPackage(rulesPackage + "." + knowledgebasename);

        String factSessionName = "RulesEngineSession_" + knowledgebasename;
        kieBaseModel1.newKieSessionModel(factSessionName).setClockType(ClockTypeOption.get("realtime"));

        Double newversion = Double.parseDouble(releaseId.getVersion()) + 0.1;

        ReleaseId releaseId2 = kieServices.newReleaseId("eu.tng", "policymanager", newversion.toString());

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);

        kieFileSystem.generateAndWritePomXML(releaseId2);

        kieFileSystem.writeKModuleXML(kieModuleModel.toXML());
        logger.log(java.util.logging.Level.INFO, "kieModuleModel--ToXML\n{0}", kieModuleModel.toXML());

        addPolicyRules(gnsid, policyname);

        kieBuilder.buildAll();

        if (kieBuilder.getResults()
                .hasMessages(Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kieBuilder.getResults().toString());
        }

        kieContainer = kieServices.newKieContainer(releaseId2);

        KieSession kieSession = kieContainer.newKieSession(factSessionName);
        kieUtil.fireKieSession(kieSession, factSessionName);

    }

    private void addPolicyRules(String gnsid, String policyname) {
        //TODO convert yml to drools

        //1. Fech yml file
        File policydescriptor = new File(current_dir + "/" + POLICY_DESCRIPTORS_PACKAGE + "/" + policyname + ".yml");
        PolicyYamlFile policyyml = policyYamlFile.readYaml(policydescriptor);

        logger.info("get mi first policy rule name" + policyyml.getPolicyRules().get(0).getName());

        List<PolicyRule> policyrules = policyyml.getPolicyRules();
        Gson gson = new Gson();
        for (PolicyRule policyrule : policyrules) {
            logger.info("rule name " + policyrule.getName());

            RuleCondition rulecondition = policyrule.getConditions();
            logger.info("rule conditions as json " + gson.toJson(rulecondition));

            List<eu.tng.policymanager.repository.Action> ruleactions = policyrule.getActions();
            logger.info("rule actions as json " + gson.toJson(ruleactions));

            //2. convert yml to dsl
            //3.convert dsl to drl
            gPolicy.validateGpolicyClasses(new File(current_dir + "/dsl/policy.txt"));

        }
    }

    public boolean savePolicyDescriptor(String policyDescriptor) {
        FileOutputStream out = null;
        try {

            JSONObject runtimedescriptor = new JSONObject(policyDescriptor);
            String policyname = runtimedescriptor.getString("name");

            String drlPath4deployment = "/descriptors/" + policyname + ".yml";
            out = new FileOutputStream(current_dir + "/" + drlPath4deployment);
            out.write(jsonToYaml(runtimedescriptor).getBytes());
            out.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(RulesEngineService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RulesEngineService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(RulesEngineService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        }
        return true;
    }

    public boolean deletePolicyDescriptor(String policyDescriptorname) {
        try {
            Path policyDescriptorPath = Paths.get(current_dir + "/descriptors/" + policyDescriptorname + ".yml");
            Files.delete(policyDescriptorPath);

        } catch (IOException ex) {
            Logger.getLogger(RulesEngineService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return true;
    }

    private String jsonToYaml(JSONObject jsonobject) {
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

}//EoC
