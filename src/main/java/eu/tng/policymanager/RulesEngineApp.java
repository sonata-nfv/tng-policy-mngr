package eu.tng.policymanager;

import  eu.tng.policymanager.config.DroolsConfig;

import java.util.Arrays;
import javax.jms.ConnectionFactory;
import javax.jms.Topic;
import org.apache.activemq.command.ActiveMQTopic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerContainerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The main class, which Spring Boot uses to bootstrap the application.
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
@ComponentScan({ 
    "eu.tng.policymanager",
    "eu.tng.policymanager.rules",
    "rules"
}
)
//Import component specific configurations
@Import({DroolsConfig.class})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
@EnableJms
public class RulesEngineApp {

    private static Logger log = LoggerFactory.getLogger(RulesEngineApp.class);

    public static final String POLICY_ENFORCEMENT_TOPIC = "eu.tng.policy.enforcement";
    public static final String RUNTIME_ACTIONS_TOPIC = "eu.tng.runtime.actions";

    static {
        System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "eu.tng.policymanager,java.util,java.lang");
    }

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(RulesEngineApp.class, args);

        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
    }

    @Bean // Strictly speaking this bean is not necessary as boot creates a default
    JmsListenerContainerFactory<?> myJmsContainerFactory(ConnectionFactory connectionFactory) {
        SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(true);
        return factory;
    }

    @Bean
    public Topic policyEnforcementTopic() {
        return new ActiveMQTopic(POLICY_ENFORCEMENT_TOPIC);
    }

    @Bean
    public Topic runtimeActionsTopic() {
        return new ActiveMQTopic(RUNTIME_ACTIONS_TOPIC);
    }

}
