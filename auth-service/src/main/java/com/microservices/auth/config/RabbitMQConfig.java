package com.microservices.auth.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String TENANT_EXCHANGE = "tenant.exchange";
  public static final String TENANT_CREATED_ROUTING_KEY = "tenant.created";
  public static final String TENANT_DROPPED_ROUTING_KEY = "tenant.dropped";

  @Bean
  public TopicExchange tenantExchange() {
    return new TopicExchange(TENANT_EXCHANGE);
  }

  @Bean
  public MessageConverter jackson2JsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
