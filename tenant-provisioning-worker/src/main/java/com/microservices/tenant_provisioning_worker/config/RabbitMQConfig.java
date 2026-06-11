package com.microservices.tenant_provisioning_worker.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.microservices.tenant_provisioning_worker.event.TenantCreatedEvent;

@Configuration
public class RabbitMQConfig {

    public static final String TENANT_EXCHANGE = "tenant.exchange";
    public static final String TENANT_SCHEMA_QUEUE = "tenant.schema-create.queue";
    public static final String TENANT_CREATED_ROUTING_KEY = "tenant.created";

    @Bean
    public Queue tenantSchemaQueue() {
        return new Queue(TENANT_SCHEMA_QUEUE, true);
    }

    @Bean
    public TopicExchange tenantExchange() {
        return new TopicExchange(TENANT_EXCHANGE);
    }

    @Bean
    public Binding tenantBinding(Queue tenantSchemaQueue, TopicExchange tenantExchange) {
        return BindingBuilder
            .bind(tenantSchemaQueue)
            .to(tenantExchange)
            .with(TENANT_CREATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("*");

        Map<String, Class<?>> isClassMapping = new HashMap<>();
        isClassMapping.put("com.microservices.auth_service.event.TenantCreatedEvent", TenantCreatedEvent.class);

        classMapper.setIdClassMapping(isClassMapping);
        converter.setClassMapper(classMapper);

        return converter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, 
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
