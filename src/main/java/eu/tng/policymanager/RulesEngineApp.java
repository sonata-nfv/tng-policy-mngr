package eu.tng.policymanager;

import eu.tng.policymanager.GPolicy.GPolicy;
import eu.tng.policymanager.Messaging.DeployedNSListener;
import eu.tng.policymanager.Messaging.MonitoringListener;
import eu.tng.policymanager.Messaging.RuntimeActionsListener;
import eu.tng.policymanager.config.DroolsConfig;
import eu.tng.policymanager.repository.PolicyYamlFile;

import java.util.Arrays;

import org.springframework.amqp.core.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * The main class, which Spring Boot uses to bootstrap the application.
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
@ComponentScan({
    "eu.tng.policymanager",
    "eu.tng.policymanager.rules",
    "rules",
    "dsl",
    "descriptors"
}
)
//Import component specific configurations
@Import({DroolsConfig.class})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@EnableMongoRepositories("eu.tng.policymanager.repository.dao")
@EnableScheduling
//@EnableJms
public class RulesEngineApp {

    private static Logger log = LoggerFactory.getLogger(RulesEngineApp.class);

    public static final String RUNTIME_ACTIONS_QUEUE = "eu.tng.policy.runtime.actions";
    final static String monitoringqueue = "son.monitoring.policy";
    final static String NS_INSTATIATION_QUEUE = "service.instances.create";

    public static void main(String[] args) {

        ApplicationContext ctx = SpringApplication.run(RulesEngineApp.class, args);
        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);

    }

    @Bean
    public PolicyYamlFile policyYamlFile() {
        return new PolicyYamlFile();
    }

    @Bean
    public GPolicy gpolicy() {
        return new GPolicy();
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange("spring-boot-exchange");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Configure connection with rabbit mq for NS runtime Actions Queue
    @Bean
    public Queue runtimeActionsQueue() {
        return new Queue(RUNTIME_ACTIONS_QUEUE);
    }

    @Bean
    Binding binding1(TopicExchange exchange) {
        return BindingBuilder.bind(runtimeActionsQueue()).to(exchange).with(runtimeActionsQueue().getName());
    }

    @Qualifier("listenerAdapter1")
    @Bean
    MessageListenerAdapter listenerAdapter1(RuntimeActionsListener receiver) {

        MessageListenerAdapter msgadapter = new MessageListenerAdapter(receiver, "expertSystemMessageReceived");
        msgadapter.setMessageConverter(jackson2JsonMessageConverter());
        return msgadapter;
    }

    @Qualifier("container1")
    @Bean
    SimpleMessageListenerContainer container1(ConnectionFactory connectionFactory,
            @Qualifier("listenerAdapter1") MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(RUNTIME_ACTIONS_QUEUE);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    // Configure connection with rabbit mq for prometheus alerts Queue
    @Bean
    public Queue monitoringAlerts() {
        return new Queue("son.monitoring.policy", false);
    }

    @Bean
    Binding binding2(TopicExchange exchange) {
        return BindingBuilder.bind(monitoringAlerts()).to(exchange).with(monitoringAlerts().getName());

    }

    @Qualifier("listenerAdapter2")
    @Bean
    MessageListenerAdapter listenerAdapter2(MonitoringListener receiver) {
        MessageListenerAdapter msgadapter = new MessageListenerAdapter(receiver, "monitoringAlertReceived");
        msgadapter.setMessageConverter(jackson2JsonMessageConverter());
        return msgadapter;
    }

    @Qualifier("container2")
    @Bean
    SimpleMessageListenerContainer container2(ConnectionFactory connectionFactory,
            @Qualifier("listenerAdapter2") MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(monitoringAlerts().getName());
        container.setMessageListener(listenerAdapter);
        return container;
    }

    // Configure connection with rabbit mq for NS instantiatin Queue
    @Bean
    public Queue nsInstantiationQueue() {
        return new Queue(NS_INSTATIATION_QUEUE, false);
    }

    @Bean
    Binding bindingNSInstantiationQueue(TopicExchange exchange) {
        return BindingBuilder.bind(nsInstantiationQueue()).to(exchange).with(nsInstantiationQueue().getName());
    }

    @Qualifier("nsInstantiationlistenerAdapter")
    @Bean
    MessageListenerAdapter nsInstantiationlistenerAdapter(DeployedNSListener receiver) {
        MessageListenerAdapter msgadapter = new MessageListenerAdapter(receiver, "deployedNSMessageReceived");
        msgadapter.setMessageConverter(jackson2JsonMessageConverter());
        return msgadapter;
    }

    @Qualifier("nsInstantiationcontainer")
    @Bean
    SimpleMessageListenerContainer nsInstantiationcontainer(ConnectionFactory connectionFactory,
            @Qualifier("nsInstantiationlistenerAdapter") MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(nsInstantiationQueue().getName());
        container.setMessageListener(listenerAdapter);
        return container;
    }

}
