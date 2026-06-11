package com.microservices.auth_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 1. 이름 정의
    public static final String TENANT_EXCHANGE = "tenant.exchange";
    public static final String TENANT_SCHEMA_QUEUE = "tenant.schema-create.queue";
    public static final String TENANT_CREATED_ROUTING_KEY = "tenant.created";

    // 2. Queue(우체통) 등록: 메시지가 쌓이는 곳
    @Bean
    public Queue tenantSchemaQueue() {
        return new Queue(TENANT_SCHEMA_QUEUE, true);
    }

    // 3. Exchange(우체국) 등록: 메시지를 분류해서 큐로 보내주는 역할
    @Bean
    public TopicExchange tenantExchange() {
        return new TopicExchange(TENANT_EXCHANGE);
    }

    // 4. Binding(배달 규칙): 특정 Routing Key 로 들어온 메시지를 특정 큐로 연결
    @Bean
    public Binding tenantBinding(Queue tenantSchemaQueue, TopicExchange topicExchange) {
        return BindingBuilder
            .bind(tenantSchemaQueue)
            .to(topicExchange)
            .with(TENANT_CREATED_ROUTING_KEY);
    }

    // 5. MessageConverter 등록: Java 객체를 JSON 으로 자동 변환
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }




}
