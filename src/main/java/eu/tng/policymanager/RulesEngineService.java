/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager;

import com.google.common.io.Resources;
import eu.tng.policymanager.facts.RuleActionType;
import eu.tng.policymanager.Messaging.ExpertSystemMessage;
import static eu.tng.policymanager.config.DroolsConfig.RULESPACKAGE;
import eu.tng.policymanager.facts.Action;
import eu.tng.policymanager.facts.DoActionToComponent;
import eu.tng.policymanager.facts.MonitoredComponent;
import eu.tng.policymanager.rules.generation.KieUtil;
import eu.tng.policymanager.transferobjects.MonitoringMessageTO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.jms.Message;
import javax.jms.Topic;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.internal.io.ResourceFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class RulesEngineService {

    private static final Logger logger = Logger.getLogger(RulesEngineService.class.getName());
    private static final String rulesPackage = "rules";
    ReleaseId releaseId = KieServices.Factory.get().newReleaseId("eu.tng", "policymanager", "1.0");

    private final KieServices kieServices;
    private final KieFileSystem kieFileSystem;
    private final KieModuleModel kieModuleModel;

    @Autowired
    KieContainer kieContainer;

    private final KieUtil kieUtil;

    @Autowired
    JmsTemplate jmsTemplate;

    @Autowired
    Topic runtimeActionsTopic;

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

                logger.info("Action Ready to send it to Pub/Sub to RUNTIME_ACTIONS_TOPIC:  " + doaction.toString());
                jmsTemplate.setTimeToLive(1200000);
                jmsTemplate.send(runtimeActionsTopic, (session) -> {

                    //note conf param is missing
                    ExpertSystemMessage expertSystemMessage = new ExpertSystemMessage();
                    expertSystemMessage.setRuleActionType(doaction.getRuleActionType());
                    expertSystemMessage.setAction(doaction.getAction());
                    expertSystemMessage.setGgid(doaction.getGsgid());
                    expertSystemMessage.setValue(String.valueOf(doaction.getValue()));

                    expertSystemMessage.setNodeid(doaction.getNodeid());
                    expertSystemMessage.setGname("pilotTranscodingService");

                    expertSystemMessage.setUsername("tngpolicymanager");

                    Message m = session.createObjectMessage(expertSystemMessage);
                    m.setStringProperty("context", "runtime_action");

                    return m;
                });

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

    /**
     * Search the {@link KieSession} for bus passes.
     */
    private List<Action> findAction(KieSession kieSession) {

        // Find all DoAction facts and 1st generation child classes of DoAction.
        ObjectFilter doActionFilter = new ObjectFilter() {
            @Override
            public
                    boolean accept(Object object) {
                if (Action.class
                        .equals(object.getClass())) {

                    return true;
                }

                if (Action.class
                        .equals(object.getClass().getSuperclass())) {

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

//    private Resource getResource(KieServices kieServices, String resourcePath) {
//        try {
//            InputStream is = Resources.getResource(resourcePath).openStream(); //guava
//            return kieServices.getResources()
//                    .newInputStreamResource(is)
//                    .setResourceType(ResourceType.DRL);
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to load drools resource file.", e);
//        }
//    }

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

    // Following functions are used only by unit tests - is code to get cleared
    /**
     * Search the {@link KieSession} for bus passes.
     */
    private DoActionToComponent findDoAction(KieSession kieSession) {

        // Find all DoAction facts and 1st generation child classes of DoAction.
        ObjectFilter doActionFilter = new ObjectFilter() {
            @Override
            public
                    boolean accept(Object object) {
                if (DoActionToComponent.class
                        .equals(object.getClass())) {

                    return true;
                }

                if (DoActionToComponent.class
                        .equals(object.getClass().getSuperclass())) {

                    return true;
                }
                return false;
            }
        };
        System.out.println("------New FACT----------");
        printFactsMessage(kieSession);

        List<DoActionToComponent> facts = new ArrayList<DoActionToComponent>();

        for (FactHandle handle : kieSession.getEntryPoint("MonitoringStream").getFactHandles(doActionFilter)) {
            facts.add((DoActionToComponent) kieSession.getObject(handle));
        }
        if (facts.size() == 0) {
            System.out.println("There are no facts at working memory");
            return null;
        }
        // Assumes that the rules will always be generating a single doaction. 
        return facts.get(0);
    }

    /**
     * create a new Fact insert a MonitoredComponent's details and fire rules to
     * determine what kind of runtime action is to be issued.
     *
     * @param monitoredComponent
     * @return
     */
    public DoActionToComponent getDoAction(MonitoredComponent monitoredComponent) {

        String factKnowledgebase = "GSGKnowledgeBase_" + monitoredComponent.getGroundedGraphid();
        String factSessionName = "RulesEngineSession_" + monitoredComponent.getGroundedGraphid();

        Collection<String> kiebases = kieContainer.getKieBaseNames();

        if (!kiebases.contains(factKnowledgebase)) {
            logger.log(java.util.logging.Level.WARNING, "Missing Knowledge base {0}", factKnowledgebase);
            return null;
        }

        KieSession kieSession = (KieSession) kieUtil.seeThreadMap().get(factSessionName);

        System.out.println("kiesessin info" + kieSession.getEntryPointId());

        EntryPoint monitoringStream = kieSession.getEntryPoint("MonitoringStream");

        monitoringStream.insert(monitoredComponent);

        DoActionToComponent doaction = findDoAction(kieSession);

        if (null != doaction) {

            jmsTemplate.send(runtimeActionsTopic, (session) -> {

                ExpertSystemMessage expertSystemMessage = new ExpertSystemMessage();
                expertSystemMessage.setAction(doaction.getAction().getRuleActionType().toString());
                expertSystemMessage.setValue(String.valueOf(doaction.getAction().getValue()));
                expertSystemMessage.setGgid(doaction.getMonitoredComponent().getGroundedGraphid());
                expertSystemMessage.setCid(doaction.getMonitoredComponent().getName());
                expertSystemMessage.setValue(String.valueOf(doaction.getAction().getValue()));

                Message m = session.createObjectMessage(expertSystemMessage);
                m.setStringProperty("context", "runtime_action");

                return m;
            });
        }

        return doaction;
    }

    private void loadRulesFromFile() {
        logger.info("Loading Rules from File to production memory");
        String knowledgebasename = "gsgpilotTranscodingService";
        String drlPath4deployment = "/rules/gsgpilotTranscodingService/gsgpilotTranscodingService.drl";
        try {
            String current_dir = System.getProperty("user.dir");
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

            InputStream inputstream = this.getClass().getResourceAsStream("/rules/gsgpilotTranscodingService/gsgpilotTranscodingService.drl");
            ret = IOUtils.toString(inputstream, "UTF-8");

        } //EoM  
        catch (IOException ex) {
            Logger.getLogger(RulesEngineService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return ret;
    }

}//EoC
