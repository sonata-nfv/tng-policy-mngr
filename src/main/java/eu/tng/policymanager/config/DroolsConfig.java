package eu.tng.policymanager.config;

import eu.tng.policymanager.RulesEngineService;
import eu.tng.policymanager.rules.generation.KieUtil;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
@Configuration
@ComponentScan(basePackages = {"rules"})
public class DroolsConfig {

    public static final String PACKAGE_NAME = "eu.tng.policymanager";
    public static final String KIE_BASE_MODEL_PREFIX = "KnowledgeBase_";
    public static final String KNOWLEDGEBASE_PREFIX = "kb";
    public static final String FACT_KNOWLEDGEBASE_BASE_PREFIX = "KnowledgeBase_kb";
    public static final String RULESPACKAGE = "rules";
    public static final String POLICY_DESCRIPTORS_PACKAGE = "policy_descriptors";
    public static final String SESSION_PREFIX = "RulesEngineSession_";
    public static final String FACT_SESSION_PREFIX = "RulesEngineSession_kb";

    /**
     * Load all Knowledge bases By defining the {@link KieContainer} as a bean
     * here, we ensure that Drools will hunt out the kmodule.xml and rules on
     * application startup. Those can be found in
     * <code>src/main/resources</code>.
     *
     *
     * @param rulesEngineService
     * @return
     */
    @Order(1)
    @Bean
    public KieContainer kieContainer(RulesEngineService rulesEngineService) {

        rulesEngineService.addKnowledgebasePerGroundedGraphTR();
        return rulesEngineService.lanchKieContainerTR();

    }

    @Order(2)
    @Bean
    public boolean createSessions(KieContainer kieContainer, KieUtil kieUtil) {

        String factSessionName = "RulesEngineSession_gsgpilotTranscodingService";
        KieSession kieSession = kieContainer.newKieSession(factSessionName);
        kieUtil.fireKieSession(kieSession, factSessionName);

        return true;

    }

}
