package com.microservices.tenant_provisioning_worker.config;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microservices.tenant_provisioning_worker.event.TenantDroppedEvent;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.microservices.tenant_provisioning_worker.event.TenantCreatedEvent;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitMQConfig {

    public static final String TENANT_DLX = "tenant.dlx";
    public static final String TENANT_EXCHANGE = "tenant.exchange";

    public static final String TENANT_SCHEMA_QUEUE = "tenant.schema-create.queue";
    public static final String TENANT_CREATED_ROUTING_KEY = "tenant.created";

    // 데드 레터 큐(생성)
    public static final String TENANT_SCHEMA_CREATE_DLQ = "tenant.schema-create.dlq";
    public static final String TENANT_SCHEMA_CREATE_DLQ_ROUTING_KEY = "tenant.schema-create.failed";

    public static final String TENANT_SCHEMA_DROP_QUEUE = "tenant.schema-drop.queue";
    public static final String TENANT_DROPPED_ROUTING_KEY = "tenant.dropped";

    // 데드 레터 큐(삭제)
    public static final String TENANT_SCHEMA_DROP_DLQ = "tenant.schema-drop.dlq";
    public static final String TENANT_SCHEMA_DROP_DLQ_ROUTING_KEY = "tenant.schema-drop.failed";


    @Bean
    public Queue tenantSchemaCreatedQueue() {
        return QueueBuilder.durable(TENANT_SCHEMA_QUEUE)
            .withArgument("x-dead-letter-exchange", TENANT_DLX)
            .withArgument("x-dead-letter-routing-key", TENANT_SCHEMA_CREATE_DLQ_ROUTING_KEY)
            .build();
    }

    @Bean
    public Queue tenantSchemaDroppedQueue() {
        return QueueBuilder.durable(TENANT_SCHEMA_DROP_QUEUE)
            .withArgument("x-dead-letter-exchange", TENANT_DLX)
            .withArgument("x-dead-letter-routing-key", TENANT_SCHEMA_DROP_DLQ_ROUTING_KEY)
            .build();
    }

    @Bean
    public Queue tenantSchemaCreateDlq() {
        return QueueBuilder.durable(TENANT_SCHEMA_CREATE_DLQ).build();
    }

    @Bean
    public Queue tenantSchemaDropDlq() {
        return QueueBuilder.durable(TENANT_SCHEMA_DROP_DLQ).build();
    }

    @Bean
    public TopicExchange tenantExchange() {
        return new TopicExchange(TENANT_EXCHANGE);
    }

    @Bean
    public DirectExchange tenantDeadLetterExchange() {
        return new DirectExchange(TENANT_DLX);
    }

    @Bean
    public Binding tenantCreatedBinding( Queue tenantSchemaCreatedQueue, TopicExchange tenantExchange) {
        return BindingBuilder
            .bind(tenantSchemaCreatedQueue)
            .to(tenantExchange)
            .with(TENANT_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding tenantDroppedBinding(Queue tenantSchemaDroppedQueue, TopicExchange topicExchange) {
        return BindingBuilder
            .bind(tenantSchemaDroppedQueue)
            .to(topicExchange)
            .with(TENANT_DROPPED_ROUTING_KEY);
    }

    @Bean
    public Binding tenantSchemaCreateDlqBinding(
        Queue tenantSchemaCreateDlq,
        DirectExchange tenantDeadLetterExchange) {
        return BindingBuilder
            .bind(tenantSchemaCreateDlq)
            .to(tenantDeadLetterExchange)
            .with(TENANT_SCHEMA_CREATE_DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding tenantSchemaDropDlqBinding(
        Queue tenantSchemaDropDlq,
        DirectExchange tenantDeadLetterExchange) {
        return BindingBuilder
            .bind(tenantSchemaDropDlq)
            .to(tenantDeadLetterExchange)
            .with(TENANT_SCHEMA_DROP_DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);

        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages(
            "com.microservices.auth_service.event",
            "com.microservices.tenant_provisioning_worker.event"
        );

        Map<String, Class<?>> isClassMapping = new HashMap<>();
        isClassMapping.put("com.microservices.auth_service.event.TenantCreatedEvent", TenantCreatedEvent.class);
        isClassMapping.put("com.microservices.auth_service.event.TenantDroppedEvent", TenantDroppedEvent.class);

        classMapper.setIdClassMapping(isClassMapping);
        converter.setClassMapper(classMapper);

        return converter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, 
            MessageConverter messageConverter,
            RetryOperationsInterceptor retryOperationsInterceptor) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAdviceChain(retryOperationsInterceptor);
        factory.setDefaultRequeueRejected(false);
        factory.setMissingQueuesFatal(false);

        return factory;
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
            .maxAttempts(3)
            .backOffOptions(1000, 2.0, 10000)
            .recoverer(new RejectAndDontRequeueRecoverer())
            .build();
    }
}
