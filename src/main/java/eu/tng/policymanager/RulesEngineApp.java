package eu.tng.policymanager;

import eu.tng.policymanager.Messaging.MonitoringListener;
import eu.tng.policymanager.Messaging.RuntimeActionsListener;
import eu.tng.policymanager.config.DroolsConfig;

import java.util.Arrays;
//import javax.jms.ConnectionFactory;
//import javax.jms.Topic;
//import org.apache.activemq.command.ActiveMQTopic;

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
//import org.springframework.jms.annotation.EnableJms;
//import org.springframework.jms.config.JmsListenerContainerFactory;
//import org.springframework.jms.config.SimpleJmsListenerContainerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;

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
//@EnableJms
public class RulesEngineApp {

    private static Logger log = LoggerFactory.getLogger(RulesEngineApp.class);

    public static final String RUNTIME_ACTIONS_QUEUE = "eu.tng.policy.runtime.actions";
    //final static String queueName = "hello";
    final static String monitoringqueue = "son.monitoring.policy";

//    static {
//        System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "eu.tng.policymanager,java.util,java.lang");
//    }

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(RulesEngineApp.class, args);

        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
    }

//    @Bean // Strictly speaking this bean is not necessary as boot creates a default
//    JmsListenerContainerFactory<?> myJmsContainerFactory(ConnectionFactory connectionFactory) {
//        SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory);
//        factory.setPubSubDomain(true);
//        return factory;
//    }
    @Bean
    public Queue runtimeActionsQueue() {
        return new Queue(RUNTIME_ACTIONS_QUEUE);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange("spring-boot-exchange");
    }

    @Bean
    Binding binding1(TopicExchange exchange) {
        return BindingBuilder.bind(runtimeActionsQueue()).to(exchange).with(runtimeActionsQueue().getName());
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

    @Qualifier("listenerAdapter1")
    @Bean
    MessageListenerAdapter listenerAdapter1(RuntimeActionsListener receiver) {
        return new MessageListenerAdapter(receiver, "expertSystemMessageReceived");
    }

    @Bean
    public Queue monitoringAlerts() {
        return new Queue("son.monitoring.policy",false);
    }

    @Bean
    Binding binding2(TopicExchange exchange) {
        return BindingBuilder.bind(monitoringAlerts()).to(exchange).with(monitoringAlerts().getName());

    }

    @Qualifier("listenerAdapter2")
    @Bean
    MessageListenerAdapter listenerAdapter2(MonitoringListener receiver) {
        MessageListenerAdapter msgadapter = new MessageListenerAdapter(receiver, "monitoringAlertReceived");
        msgadapter.setMessageConverter(converter());
        return msgadapter;
    }
    
     @Bean
    public Jackson2JsonMessageConverter converter() {
        return new Jackson2JsonMessageConverter();
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
    
}
