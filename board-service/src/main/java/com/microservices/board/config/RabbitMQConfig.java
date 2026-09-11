package com.microservices.board.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microservices.board.event.TenantCreatedEvent;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

/** auth-service가 발행하는 tenant.created 이벤트를 구독해, 새 테넌트 스키마에 board-service 테이블을 만든다. */
@Configuration
public class RabbitMQConfig {

  public static final String TENANT_DLX = "tenant.dlx";
  public static final String TENANT_EXCHANGE = "tenant.exchange";
  public static final String TENANT_CREATED_ROUTING_KEY = "tenant.created";

  public static final String BOARD_SCHEMA_QUEUE = "board.tenant.schema-create.queue";
  public static final String BOARD_SCHEMA_DLQ = "board.tenant.schema-create.dlq";
  public static final String BOARD_SCHEMA_DLQ_ROUTING_KEY = "board.tenant.schema-create.failed";

  @Bean
  public Queue boardTenantSchemaQueue() {
    return QueueBuilder.durable(BOARD_SCHEMA_QUEUE)
        .withArgument("x-dead-letter-exchange", TENANT_DLX)
        .withArgument("x-dead-letter-routing-key", BOARD_SCHEMA_DLQ_ROUTING_KEY)
        .build();
  }

  @Bean
  public Queue boardTenantSchemaDlq() {
    return QueueBuilder.durable(BOARD_SCHEMA_DLQ).build();
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
  public Binding boardTenantSchemaBinding(
      Queue boardTenantSchemaQueue, TopicExchange tenantExchange) {
    return BindingBuilder.bind(boardTenantSchemaQueue)
        .to(tenantExchange)
        .with(TENANT_CREATED_ROUTING_KEY);
  }

  @Bean
  public Binding boardTenantSchemaDlqBinding(
      Queue boardTenantSchemaDlq, DirectExchange tenantDeadLetterExchange) {
    return BindingBuilder.bind(boardTenantSchemaDlq)
        .to(tenantDeadLetterExchange)
        .with(BOARD_SCHEMA_DLQ_ROUTING_KEY);
  }

  @Bean
  public MessageConverter jackson2JsonMessageConverter() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);

    DefaultClassMapper classMapper = new DefaultClassMapper();
    classMapper.setTrustedPackages("com.microservices.auth.event", "com.microservices.board.event");

    Map<String, Class<?>> idClassMapping = new HashMap<>();
    idClassMapping.put("com.microservices.auth.event.TenantCreatedEvent", TenantCreatedEvent.class);
    classMapper.setIdClassMapping(idClassMapping);
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
  public RetryOperationsInterceptor retryOperationsInterceptor() {
    return RetryInterceptorBuilder.stateless()
        .maxAttempts(3)
        .backOffOptions(1000, 2.0, 10000)
        .recoverer(new RejectAndDontRequeueRecoverer())
        .build();
  }
}
