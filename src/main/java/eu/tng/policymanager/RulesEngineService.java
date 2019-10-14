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
package eu.tng.policymanager;

import com.google.gson.Gson;
import eu.tng.policymanager.Exceptions.NSDoesNotExistException;
import eu.tng.policymanager.Exceptions.VNFDoesNotExistException;
import eu.tng.policymanager.Exceptions.VNFRDoesNotExistException;
import eu.tng.policymanager.Messaging.LogsFormat;
import eu.tng.policymanager.facts.RuleActionType;
import static eu.tng.policymanager.config.DroolsConfig.RULESPACKAGE;
import eu.tng.policymanager.facts.action.Action;
import eu.tng.policymanager.facts.action.ComponentResourceAllocationAction;
import eu.tng.policymanager.facts.LogMetric;
import eu.tng.policymanager.facts.MonitoredComponent;
import eu.tng.policymanager.facts.action.AlertAction;
import eu.tng.policymanager.facts.action.ElasticityAction;
import eu.tng.policymanager.facts.action.NetworkManagementAction;
import eu.tng.policymanager.facts.enums.ScalingType;
import eu.tng.policymanager.facts.enums.Status;
import eu.tng.policymanager.repository.Inertia;
import eu.tng.policymanager.repository.PolicyRule;
import eu.tng.policymanager.rules.generation.KieUtil;
import eu.tng.policymanager.repository.PolicyYamlFile;
import eu.tng.policymanager.repository.RuleCondition;
import eu.tng.policymanager.repository.dao.RecommendedActionRepository;
import eu.tng.policymanager.repository.domain.RecommendedAction;
import eu.tng.policymanager.rules.generation.RepositoryUtil;
import eu.tng.policymanager.rules.generation.Util;
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
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.drools.compiler.lang.DrlDumper;
import org.drools.compiler.lang.api.CEDescrBuilder;
import org.drools.compiler.lang.api.DescrFactory;
import org.drools.compiler.lang.api.PackageDescrBuilder;
import org.drools.compiler.lang.api.RuleDescrBuilder;
import org.drools.compiler.lang.descr.AndDescr;
import org.json.JSONObject;
import org.kie.api.KieBase;
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
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.internal.io.ResourceFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class RulesEngineService {

    //private static final Logger logger = Logger.getLogger(RulesEngineService.class.getName());
    private static final String rulesPackage = "rules";
    private static final String current_dir = System.getProperty("user.dir");
    ReleaseId releaseId = KieServices.Factory.get().newReleaseId("eu.tng", "policymanager", "1.0");
    private static final String FACTS_EXPIRATION = "3m";

    private final KieServices kieServices;
    private final KieFileSystem kieFileSystem;
    private final KieModuleModel kieModuleModel;
    static Map<String, KieBase> kieBaseCache = new HashMap<>();

    @Autowired
    KieContainer kieContainer;

    private final KieUtil kieUtil;

    @Autowired
    private RabbitTemplate template;

    @Autowired
    LogsFormat logsFormat;

    @Qualifier("runtimeActionsQueue")
    @Autowired
    private Queue queue;
    
    @Qualifier("reconfigureActionsQueue")
    @Autowired
    private Queue reconfigure_queue;

    @Autowired
    private TopicExchange exchange;

    @Autowired
    PolicyYamlFile policyYamlFile;

    @Autowired
    RecommendedActionRepository recommendedActionRepository;

    @Value("${tng.cat.vnfs}")
    private String vnfs_url;

    @Autowired
    CataloguesConnector cataloguesConnector;

    @Autowired
    RepositoryConnector repositoryConnector;

    @Autowired
    public RulesEngineService(KieUtil kieUtil) {
        //Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        //logsFormat.createLogInfo("I", timestamp.toString(), "Rule Engine Session initializing...", "", "200");
        this.kieServices = KieServices.Factory.get();
        this.kieFileSystem = kieServices.newKieFileSystem();
        this.kieModuleModel = kieServices.newKieModuleModel();
        this.kieUtil = kieUtil;

    }

//fireAllRules every 5 minutes 1min== 60000
    @Scheduled(fixedRate = 6000)
    public void searchForGeneratedActions() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        //logger.info("Search for actions");
        ConcurrentHashMap map = kieUtil.seeThreadMap();
        for (Object key : map.keySet()) {
            //System.out.println("factSessionName " + key.toString());
            String factSessionName = key.toString();
            KieSession kieSession = (KieSession) kieUtil.seeThreadMap().get(factSessionName);

            printFactsMessage(kieSession);

            List<Action> doactions = findAction(kieSession);

            if (doactions.size() > 0) {
                Gson gson = new Gson();

                for (Action doaction : doactions) {

                    if (doaction instanceof ComponentResourceAllocationAction) {
                        ComponentResourceAllocationAction doactionsubclass = (ComponentResourceAllocationAction) doaction;

                        //template.convertAndSend(queue.getName(), gson.toJson(doactionsubclass));
                        System.out.println(" [x] Sent '" + gson.toJson(doactionsubclass) + "'");
                    }

                    if (doaction instanceof NetworkManagementAction) {
                        NetworkManagementAction doactionsubclass = (NetworkManagementAction) doaction;
                        //template.convertAndSend(queue.getName(), doactionsubclass.toString());
                        System.out.println(" [x] Sent '" + gson.toJson(doactionsubclass) + "'");
                    }

                    // AlertAction
                    if (doaction instanceof AlertAction) {
                        AlertAction doactionsubclass = (AlertAction) doaction;

                        String nsrid = doactionsubclass.getService_instance_id().substring(1);
                        doactionsubclass.setService_instance_id(nsrid);

                        //save Recommended action to policy repository
                        RecommendedAction recommendedAction = new RecommendedAction();

                        recommendedAction.setAction(doactionsubclass);
                        recommendedAction.setInDateTime(new Date());
                        recommendedAction.setNsrid(nsrid);

                        JSONObject alert_action_msg = new JSONObject();
                        JSONObject alert_action_payload = new JSONObject();

                        alert_action_payload.put("vnf_name", doactionsubclass.getVnf_name());

                        try {
                            //get vnf_id by vnf_name , vendor, version
                            String vnfd_id = cataloguesConnector.getVnfId(vnfs_url, doactionsubclass.getVnf_name(), doactionsubclass.getVendor(), doactionsubclass.getVersion());
                            //elasticity_action_msg.put("vnfd_id", vnfd_id);
                            alert_action_payload.put("vnfd_uuid", vnfd_id);

                        } catch (VNFDoesNotExistException ex) {
                            Logger.getLogger(RulesEngineService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        }

                        alert_action_payload.put("log_message", doactionsubclass.getLogMessage());
                        alert_action_msg.put("service_instance_uuid", doactionsubclass.getService_instance_id());

                        alert_action_payload.put("value", doactionsubclass.getValue());

                        Optional<RecommendedAction> recent_action = recommendedActionRepository.findTopByNsridOrderByInDateTimeDesc(nsrid);

                        if (recent_action.isPresent()) {
                            LocalDateTime recent_date = recent_action.get().getInDateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                            LocalDateTime now = LocalDateTime.now();
                            Duration duration = Duration.between(now, recent_date);
                            long diff = Math.abs(duration.toMinutes());
                            //logger.info("Duration between last created action and now " + diff);
                            if (diff < doactionsubclass.getInertia()) {
                                return;
                            }

                        }

                        RecommendedAction newRecommendedAction = recommendedActionRepository.save(recommendedAction);
                        doactionsubclass.setCorrelation_id(newRecommendedAction.getCorrelation_id());
                        String correlation_id = doactionsubclass.getCorrelation_id();
                        alert_action_msg.put("reconfiguration_payload", alert_action_payload);
                        String alert_action_msg_as_yml = Util.jsonToYaml(alert_action_msg);

                        template.convertAndSend(exchange.getName(), reconfigure_queue.getName(), alert_action_msg_as_yml, m -> {
                            m.getMessageProperties().setAppId("tng-policy-mngr");
                            m.getMessageProperties().setReplyTo(reconfigure_queue.getName());
                            m.getMessageProperties().setCorrelationId(correlation_id);
                            return m;
                        });

                        logsFormat.createLogInfo("I", timestamp.toString(), " [x] Sent to topic '" + alert_action_msg_as_yml + "'", "", "200");
                    }

                    if (doaction instanceof ElasticityAction) {
                        ElasticityAction doactionsubclass = (ElasticityAction) doaction;

                        String nsrid = doactionsubclass.getService_instance_id().substring(1);
                        doactionsubclass.setService_instance_id(nsrid);

                        //save Recommended action to policy repository
                        RecommendedAction recommendedAction = new RecommendedAction();

                        recommendedAction.setAction(doactionsubclass);
                        recommendedAction.setInDateTime(new Date());
                        recommendedAction.setNsrid(nsrid);

                        JSONObject elasticity_action_msg = new JSONObject();

                        elasticity_action_msg.put("vnf_name", doactionsubclass.getVnf_name());

                        try {
                            //get vnf_id by vnf_name , vendor, version
                            String vnfd_id = cataloguesConnector.getVnfId(vnfs_url, doactionsubclass.getVnf_name(), doactionsubclass.getVendor(), doactionsubclass.getVersion());
                            //elasticity_action_msg.put("vnfd_id", vnfd_id);
                            elasticity_action_msg.put("vnfd_uuid", vnfd_id);

                            if (doactionsubclass.getScaling_type().equals(ScalingType.removevnf) && doactionsubclass.getCriterion().equalsIgnoreCase("random")) {
                                String vnfr_id = repositoryConnector.get_vnfr_id_to_remove_random(doactionsubclass.getService_instance_id(), vnfd_id);
                                if (vnfr_id == null) {
                                    //logsFormat.createLogInfo("I", timestamp.toString(), "Elasticity action was prevented from been generated.", "vnfr_id is null", "200");
                                    return;
                                }
                                //elasticity_action_msg.put("vnf_id", vnfr_id);
                                elasticity_action_msg.put("vnf_uuid", vnfr_id);
                            }

                        } catch (VNFDoesNotExistException | NSDoesNotExistException | VNFRDoesNotExistException ex) {
                            Logger.getLogger(RulesEngineService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        }

                        elasticity_action_msg.put("scaling_type", doactionsubclass.getScaling_type());
                        //elasticity_action_msg.put("service_instance_id", doactionsubclass.getService_instance_id());
                        elasticity_action_msg.put("service_instance_uuid", doactionsubclass.getService_instance_id());

                        //elasticity_action_msg.put("value", doactionsubclass.getValue());
                        elasticity_action_msg.put("number_of_instances", Integer.parseInt(doactionsubclass.getValue()));

                        //JSONArray constraints = new JSONArray();
                        ///HashMap constraint = new HashMap();
                        //constraint.put("vim_id", doactionsubclass.getVim_id());
                        //constraints.put(constraint);
                        //elasticity_action_msg.put("constraints", constraints);
                        //check if exists a recent recommended Action for the specific service 
                        //logger.info("check if exists a recent recommended Action for the specific service" + nsrid);
                        Optional<RecommendedAction> recent_action = recommendedActionRepository.findTopByNsridOrderByInDateTimeDesc(nsrid);

                        if (recent_action.isPresent()) {
                            LocalDateTime recent_date = recent_action.get().getInDateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                            LocalDateTime now = LocalDateTime.now();
                            Duration duration = Duration.between(now, recent_date);
                            long diff = Math.abs(duration.toMinutes());
                            //logger.info("Duration between last created action and now " + diff);
                            if (diff < doactionsubclass.getInertia()) {
                                return;
                            }

                        }

                        RecommendedAction newRecommendedAction = recommendedActionRepository.save(recommendedAction);
                        doactionsubclass.setCorrelation_id(newRecommendedAction.getCorrelation_id());
                        String correlation_id = doactionsubclass.getCorrelation_id();
                        String elasticity_action_msg_as_yml = Util.jsonToYaml(elasticity_action_msg);

                        template.convertAndSend(exchange.getName(), queue.getName(), elasticity_action_msg_as_yml, m -> {
                            m.getMessageProperties().setAppId("tng-policy-mngr");
                            m.getMessageProperties().setReplyTo(queue.getName());
                            m.getMessageProperties().setCorrelationId(correlation_id);
                            return m;
                        });

                        //logsFormat.createLogInfo("I", timestamp.toString(), " [x] Sent to topic '" + elasticity_action_msg_as_yml + "'", "", "200");
                    }

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
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Double newversion = Double.parseDouble(releaseId.getVersion()) + 0.1;
        ReleaseId releaseId2 = kieServices.newReleaseId("eu.tng", "policymanager", newversion.toString());

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);

        kieFileSystem.generateAndWritePomXML(releaseId2);

        kieFileSystem.writeKModuleXML(kieModuleModel.toXML());
        //logger.log(java.util.logging.Level.INFO, "kieModuleModel--ToXML\n{0}", kieModuleModel.toXML());

        this.loadRulesFromFile();

        kieBuilder.buildAll();

        if (kieBuilder.getResults()
                .hasMessages(Level.ERROR)) {
            //logsFormat.createLogInfo("E", timestamp.toString(), "Error with new kieModuleModel", kieBuilder.getResults().toString(), "200");
        }

        kieContainer = kieServices.newKieContainer(releaseId2);

        return kieContainer;

    }

    protected void createFact(MonitoringMessageTO monitoringMessageTO) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        String factKnowledgebase = "GSGKnowledgeBase_gsg" + monitoringMessageTO.getGsgid();

        Collection<String> kiebases = kieContainer.getKieBaseNames();

        if (!kiebases.contains(factKnowledgebase)) {
            //logger.log(java.util.logging.Level.WARNING, "Missing Knowledge base {0}", factKnowledgebase);
            return;
        }

        String factSessionName = "RulesEngineSession_gsg" + monitoringMessageTO.getGsgid();
        KieSession kieSession = (KieSession) kieUtil.seeThreadMap().get(factSessionName);

        //logger.info("FactCount " + kieSession.getFactCount() + " to session " + factSessionName);
        EntryPoint monitoringStream = kieSession.getEntryPoint("MonitoringStream");

        MonitoredComponent monitoredComponent = new MonitoredComponent(monitoringMessageTO.getNodeid(),
                monitoringMessageTO.getMetricName(),
                Double.valueOf(monitoringMessageTO.getMetricValue()),
                monitoringMessageTO.getGsgid(),
                monitoringMessageTO.getNsrid(),
                monitoringMessageTO.getVnf_id(),
                monitoringMessageTO.getVnfd_id(),
                monitoringMessageTO.getVim_id());

        //System.out.println("Ιnsert monitoredComponent to session  " + monitoredComponent.toString());
        //logger.info(monitoringStream.getEntryPointId() + "  -------  " + monitoringStream.getFactCount());
        //logsFormat.createLogInfo("I", timestamp.toString(), "Ιnsert monitoredComponent " + monitoredComponent.toString() + " to session " + factSessionName, "", "200");
        monitoringStream.insert(monitoredComponent);

    }

    public void createLogFact(LogMetric logMetric) {

        String factKnowledgebase = "GSGKnowledgeBase_gsg" + logMetric.getNsrid().replaceAll("-", "");

        Collection<String> kiebases = kieContainer.getKieBaseNames();

        if (!kiebases.contains(factKnowledgebase)) {
            //logger.log(java.util.logging.Level.WARNING, "Missing Knowledge base {0}", factKnowledgebase);
            return;
        }

        String factSessionName = "RulesEngineSession_gsg" + logMetric.getNsrid().replaceAll("-", "");
        KieSession kieSession = (KieSession) kieUtil.seeThreadMap().get(factSessionName);

        //System.out.println("Ιnsert logmetric fact: " + logMetric.toString());
        //kieSession.insert(logMetric);
        EntryPoint monitoringStream = kieSession.getEntryPoint("MonitoringStream");

        //logger.info("monitoringStream entrypoint " + monitoringStream.getEntryPointId());
        monitoringStream.insert(logMetric);
        //logger.info("monitoringStream fact count " + monitoringStream.getFactCount());

    }

    /**
     * Search the {@link KieSession} for bus passes.
     */
    private List<Action> findAction(KieSession kieSession) {

        // Find all DoAction facts and 1st generation child classes of DoAction.
        ObjectFilter doActionFilter = new ObjectFilter() {
            @Override
            public boolean accept(Object object) {
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
        //System.out.println("------New FACT----------");

        List<Action> facts = new ArrayList<Action>();
        List<Action> factsToBeUpdated = new ArrayList<>();

        for (FactHandle handle : kieSession.getFactHandles(doActionFilter)) {

            Action action = (Action) kieSession.getObject(handle);

            if (action.getStatus() == Status.not_send) {
                action.setStatus(Status.send);
                facts.add(action);
                factsToBeUpdated.add(action);
            }

        }

        for (Action actionToBeUpdated : factsToBeUpdated) {
            kieSession.insert(actionToBeUpdated);

        }

//        if (facts.size() > 0) {
//            logger.log(java.util.logging.Level.INFO, "Num of created facts{0}", facts.size());
//        }
        return facts;
    }

    /**
     * Print out details of all facts in working memory. Handy for debugging.
     */
    @SuppressWarnings("unused")
    private void printFactsMessage(KieSession kieSession) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Collection<FactHandle> allHandles = kieSession.getFactHandles();

        //String msg = "\nAll facts:\n";
        String msg = "";
        for (FactHandle handle : allHandles) {
            msg += "    " + kieSession.getObject(handle) + "\n";
        }
        if (!msg.equalsIgnoreCase("")) {
            //logsFormat.createLogInfo("I", timestamp.toString(), "All facts: ", msg, "200");

        }

        EntryPoint monitoringstream = kieSession.getEntryPoint("MonitoringStream");
        Collection<FactHandle> allHandles1 = monitoringstream.getFactHandles();

        String msg1 = "";
        for (FactHandle handle : allHandles1) {
            msg1 += "    " + monitoringstream.getObject(handle) + "\n";
        }

        if (!msg1.equalsIgnoreCase("")) {
            //logsFormat.createLogInfo("I", timestamp.toString(), "All facts of stream: ", msg1, "200");

        }

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
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        //logsFormat.createLogInfo("I", timestamp.toString(), "Loading Rules from File to production memory", "", "200");
        String knowledgebasename = "gsgpilotTranscodingService";
        String drlPath4deployment = "/rules/gsgpilotTranscodingService/gsgpilotTranscodingService.drl";
        try {

            Path policyPackagePath = Paths.get(current_dir + "/" + RULESPACKAGE + "/" + knowledgebasename);
            String data = "";
            //1st add default rules
            data += getRulesFromFile();

            //logger.info("rules : " + data);
            Files.createDirectories(policyPackagePath);
            FileOutputStream out = new FileOutputStream(current_dir + "/" + drlPath4deployment);
            out.write(data.getBytes());
            out.close();
            //logger.info("Writing rules at: " + current_dir + "/" + RULESPACKAGE + "/" + knowledgebasename + "/" + knowledgebasename + ".drl");
            kieFileSystem.write(ResourceFactory.newFileResource(current_dir + "/" + RULESPACKAGE + "/" + knowledgebasename + "/" + knowledgebasename + ".drl"));
        } catch (Exception ex) {
            //logsFormat.createLogInfo("E", timestamp.toString(), "Error during the creation of production memory:" + ex.getMessage(), "", "200");
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
            Logger.getLogger(RulesEngineService.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return ret;
    }

    /*
     Add a new knowledge base & session & corresponding rules so as to update kieModule
     */
    public boolean addNewKnowledgebase(String gnsid, String policyname, String policyfile) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Collection<String> kiebases = kieContainer.getKieBaseNames();
        String factKnowledgebase = "GSGKnowledgeBase_gsg" + gnsid;

        if ("null".equals(policyname) || policyname == null) {
            //logsFormat.createLogInfo("W", timestamp.toString(), "NS is deploed with none policy assigned", "", "200");

            return true;
        }

        if (kiebases.contains(factKnowledgebase)) {
            //logsFormat.createLogInfo("W", timestamp.toString(), "Knowledge base already added " + factKnowledgebase, "", "200");

            //updateToVersion();
            return true;
        }

        String knowledgebasename = "gsg" + gnsid;

        System.out.println("knowledgebasename" + knowledgebasename);
        KieBaseModel kieBaseModel1 = kieModuleModel.newKieBaseModel("GSGKnowledgeBase_" + knowledgebasename)
                .setDefault(true).setEventProcessingMode(EventProcessingOption.STREAM).setEqualsBehavior(EqualityBehaviorOption.EQUALITY);

        kieBaseModel1.addPackage(rulesPackage + "." + knowledgebasename);

        String factSessionName = "RulesEngineSession_" + knowledgebasename;
        kieBaseModel1.newKieSessionModel(factSessionName).setClockType(ClockTypeOption.get("realtime"));

        Double newversion = Double.parseDouble(releaseId.getVersion()) + 0.1;

        ReleaseId releaseId2 = kieServices.newReleaseId("eu.tng", "policymanager", newversion.toString());

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);

        kieFileSystem.generateAndWritePomXML(releaseId2);

        kieFileSystem.writeKModuleXML(kieModuleModel.toXML());
        //logger.log(java.util.logging.Level.INFO, "kieModuleModel--ToXML\n{0}", kieModuleModel.toXML());

        addPolicyRules(gnsid, policyname, policyfile);

        kieBuilder.buildAll();

        if (kieBuilder.getResults()
                .hasMessages(Level.ERROR)) {
            //logsFormat.createLogInfo("E", timestamp.toString(), "Error with new kieModuleModel", kieBuilder.getResults().toString(), "200");
        }

        kieContainer = kieServices.newKieContainer(releaseId2);

        KieSession kieSession = kieContainer.newKieSession(factSessionName);
        kieUtil.fireKieSession(kieSession, factSessionName);

        return true;

    }

    private static String createPolicyRules(String nsrid, String policyname, String policyfile) {

        //TODO convert yml to drools
        //1. Fech yml file
        //File policydescriptor = new File(current_dir + "/" + POLICY_DESCRIPTORS_PACKAGE + "/" + policyname + ".yml");
        //logger.info("get file from - " + current_dir + "/" + POLICY_DESCRIPTORS_PACKAGE + "/" + policyname + ".yml");
        //PolicyYamlFile policyyml = PolicyYamlFile.readYamlFile(policydescriptor);
        PolicyYamlFile policyyml = PolicyYamlFile.readYaml(policyfile);

        //logger.info("get mi first policy rule name" + policyyml.getPolicyRules().get(0).getName());
        List<PolicyRule> policyrules = policyyml.getPolicyRules();

        Gson gson = new Gson();

        PackageDescrBuilder packageDescrBuilder = DescrFactory.newPackage();
        packageDescrBuilder
                .name(rulesPackage + ".s" + nsrid)
                .newImport().target("eu.tng.policymanager.facts.*").end()
                .newImport().target("eu.tng.policymanager.facts.action.*").end()
                .newImport().target("eu.tng.policymanager.facts.enums.*").end()
                .newDeclare().type().name("MonitoredComponent").newAnnotation("role").value("event").end()
                .newAnnotation("expires").value(FACTS_EXPIRATION).end().end()
                .newDeclare().type().name("ComponentResourceAllocationAction").newAnnotation("role").value("event").end()
                .newAnnotation("expires").value(FACTS_EXPIRATION).end().end()
                .newDeclare().type().name("ElasticityAction").newAnnotation("role").value("event").end()
                .newAnnotation("expires").value(FACTS_EXPIRATION).end().end()
                .newDeclare().type().name("LogMetric").newAnnotation("role").value("event").end()
                .newAnnotation("expires").value(FACTS_EXPIRATION).end().end();

        for (PolicyRule policyrule : policyrules) {
//            logger.info("rule name " + policyrule.getName());
//
//            RuleCondition rulecondition = policyrule.getConditions();
//            logger.info("rule conditions as json " + gson.toJson(rulecondition));
//

            RuleDescrBuilder droolrule = packageDescrBuilder
                    .newRule()
                    .name(policyrule.getName());

            CEDescrBuilder<RuleDescrBuilder, AndDescr> when = droolrule.lhs();

            RuleCondition rulecondition = policyrule.getConditions();

            //logger.info("the rules to pass "+gson.toJson(rulecondition.getRules()));
            JSONObject jsonrulewhen = new JSONObject(gson.toJson(rulecondition));

            when = RepositoryUtil.constructDroolsRule(when, jsonrulewhen, policyrule.getConditions().getCondition());
            String rhs_actions = "";

            List<eu.tng.policymanager.repository.Action> ruleactions = policyrule.getActions();
            //logger.info("rule actions as json " + gson.toJson(ruleactions));

            for (eu.tng.policymanager.repository.Action ruleaction : ruleactions) {
                String action_object = ruleaction.getAction_object();

//                rhs_actions += "insertLogical( new " + action_object + "($m1.getNsrid(),\"" + ruleaction.getTarget() + "\","
//                        + ruleaction.getAction_type() + "." + ruleaction.getName() + ",\"" + ruleaction.getValue() + "\",$m1.getVnfd_id(),$m1.getVim_id(),Status.not_send)); \n";
                Inertia inertia = policyrule.getInertia();
                int inertia_value = Integer.parseInt(inertia.getValue());
                String inertia_unit = inertia.getDuration_unit();

                long inertia_minutes = inertia_value;
                if (inertia_unit.equalsIgnoreCase("s")) {
                    inertia_minutes = TimeUnit.SECONDS.toMinutes(inertia_value);
                } else if (inertia_unit.equalsIgnoreCase("h")) {
                    inertia_minutes = TimeUnit.HOURS.toMinutes(inertia_value);

                }

                rhs_actions += "insertLogical( new " + action_object + "($m0.getNsrid(),"
                        + "\"" + ruleaction.getTarget().getName() + "\","
                        + "\"" + ruleaction.getTarget().getVendor() + "\","
                        + "\"" + ruleaction.getTarget().getVersion() + "\","
                        + ruleaction.getAction_type() + "." + ruleaction.getName() + ","
                        + "\"" + ruleaction.getValue() + "\","
                        + "\"" + ruleaction.getCriterion() + "\","
                        + inertia_minutes + ","
                        + "Status.not_send)); \n";

            }
            droolrule.rhs(rhs_actions);
            droolrule.end();

        }

        String created_rules = new DrlDumper().dump(packageDescrBuilder.getDescr());
        created_rules = created_rules.replace("|", "over");

        created_rules += "\n"
                + "rule \"inertiarule\"\n"
                + "when\n"
                + "  $e1: ElasticityAction($service_instance_id : service_instance_id)\n"
                + "  $e2: ElasticityAction(service_instance_id == $service_instance_id, this after[ 1ms, 3m ] $e1 )  \n"
                + "then\n"
                + "   System.out.println(\"Retracting ElasticityAction: \" + $e2.getService_instance_id());\n"
                + "   retract($e2);\n"
                + "end";

        System.out.println(created_rules);
        return created_rules;

    }

    public void addPolicyRules(String gnsid, String policyname, String policyfile) {

        String created_rules = createPolicyRules(gnsid, policyname, policyfile);
        try {
            String knowledgebasename = "gsg" + gnsid;

            Path policyPackagePath = Paths.get(current_dir + "/" + RULESPACKAGE + "/" + knowledgebasename);
            Files.createDirectories(policyPackagePath);
            String drlPath4deployment = "rules" + "/" + knowledgebasename + "/" + knowledgebasename + ".drl";
            FileOutputStream out = new FileOutputStream(current_dir + "/" + drlPath4deployment);
            out.write(created_rules.getBytes());
            out.close();
            kieFileSystem.write(ResourceFactory.newFileResource(current_dir + "/" + rulesPackage + "/" + knowledgebasename + "/" + knowledgebasename + ".drl"));

        } catch (IOException ex) {
            Logger.getLogger(RulesEngineService.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    public String savePolicyDescriptor(String policyDescriptor, String policy_uuid) {
        FileOutputStream out = null;
        String drlPath4deployment = null;
        try {

            JSONObject runtimedescriptor = new JSONObject(policyDescriptor);
            //String policyname = runtimedescriptor.getString("name");
            String policyname = policy_uuid;

            drlPath4deployment = "/descriptors/" + policyname + ".yml";
            out = new FileOutputStream(current_dir + "/" + drlPath4deployment);
            out.write(Util.jsonToYaml(runtimedescriptor).getBytes());
            out.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(RulesEngineService.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RulesEngineService.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();

            } catch (IOException ex) {
                Logger.getLogger(RulesEngineService.class
                        .getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        }
        return (current_dir + "/" + drlPath4deployment);
    }

    public boolean deletePolicyDescriptor(String policyDescriptorname) {
        try {
            Path policyDescriptorPath = Paths.get(current_dir + "/descriptors/" + policyDescriptorname + ".yml");
            Files.delete(policyDescriptorPath);

        } catch (IOException ex) {
            Logger.getLogger(RulesEngineService.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return true;
    }

    /*
     Remove a new knowledge base & session & corresponding rules so as to update kieModule
     */
    public void removeKnowledgebase(String nsr_id) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        try {

            Collection<String> kiebases = kieContainer.getKieBaseNames();

            String factKnowledgebase = "GSGKnowledgeBase_gsg" + nsr_id;

            if (!kiebases.contains(factKnowledgebase)) {
                //logsFormat.createLogInfo("W", timestamp.toString(), "Knowledge base already removed " + factKnowledgebase, "", "200");

                return;
            }

            String knowledgebasename = "gsg" + nsr_id;
            kieModuleModel.removeKieBaseModel("GSGKnowledgeBase_" + knowledgebasename);
            kieFileSystem.writeKModuleXML(kieModuleModel.toXML());
            //logger.log(java.util.logging.Level.INFO, "kieModuleModel--ToXML\n{0}", kieModuleModel.toXML());

            //TODO remove policy rules
            String current_dir = System.getProperty("user.dir");
            FileUtils.deleteDirectory(new File(current_dir + "/rules" + "/" + knowledgebasename));
            //delete session
            String factSessionName = "RulesEngineSession_gsg" + nsr_id;
            kieUtil.haltKieSession(factSessionName);

            Double newversion = Double.parseDouble(releaseId.getVersion()) + 0.1;
            ReleaseId releaseId2 = kieServices.newReleaseId("eu.tng", "policymanager", newversion.toString());
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            if (kieBuilder.getResults()
                    .hasMessages(Level.ERROR)) {
                //logsFormat.createLogInfo("E", timestamp.toString(), "Error with new kieModuleModel", kieBuilder.getResults().toString(), "200");

            }

            kieContainer = kieServices.newKieContainer(releaseId2);

        } catch (IOException ex) {
            //logsFormat.createLogInfo("E", timestamp.toString(), "Error with removeKnowledgebase function", ex.getMessage(), "200");

        }

    }

}//EoC
