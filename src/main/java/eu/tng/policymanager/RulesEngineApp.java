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

import com.mongodb.MongoClient;
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
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.QueueBuilder;
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
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
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
//@EnableScheduling
//@SpringBootApplication
//@EnableJms
public class RulesEngineApp {

    private static Logger log = LoggerFactory.getLogger(RulesEngineApp.class);

    public static final String RUNTIME_ACTIONS_QUEUE = "service.instance.scale";
    public final static String monitoringqueue = "son.monitoring.policy";

    public final static String NS_INSTATIATION_QUEUE = "policies.service.instances.create";
    public final static String NS_INSTATIATION_TOPIC = "service.instances.create";

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
    TopicExchange exchange() {
        return new TopicExchange("son-kernel", false, false);
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
        //return QueueBuilder.durable(NS_INSTATIATION_QUEUE).build();
    }

    @Bean
    Binding bindingNSInstantiationQueue(TopicExchange exchange) {
        return BindingBuilder.bind(nsInstantiationQueue()).to(exchange).with(NS_INSTATIATION_TOPIC);
    }

    @Bean
    public SimpleMessageConverter simpleMessageConverter() {
        return new SimpleMessageConverter();
    }

    @Qualifier("nsInstantiationlistenerAdapter")
    @Bean
    MessageListenerAdapter nsInstantiationlistenerAdapter(DeployedNSListener receiver) {
        MessageListenerAdapter msgadapter = new MessageListenerAdapter(receiver, "deployedNSMessageReceived");
        //msgadapter.setMessageConverter(simpleMessageConverter());
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
