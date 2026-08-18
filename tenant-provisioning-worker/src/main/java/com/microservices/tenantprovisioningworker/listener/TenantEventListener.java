package com.microservices.tenantprovisioningworker.listener;

import com.microservices.tenantprovisioningworker.config.RabbitMQConfig;
import com.microservices.tenantprovisioningworker.event.TenantCreatedEvent;
import com.microservices.tenantprovisioningworker.event.TenantDroppedEvent;
import com.microservices.tenantprovisioningworker.service.SchemaProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantEventListener {

  private final SchemaProvisioningService schemaProvisioningService;

  @RabbitListener(queues = RabbitMQConfig.TENANT_SCHEMA_QUEUE)
  public void onTenantCreated(TenantCreatedEvent event) {
    log.info("Received TenantCreatedEvent: {}", event);

    try {
      schemaProvisioningService.createTenantSchema(event.getTenantName());
      log.info("Finished processing for event: {}", event.getEventId());
    } catch (Exception e) {
      log.error(
          "Failed to provision schema for tenant: {}. Error: {}",
          event.getTenantName(),
          e.getMessage());
      throw e;
    }
  }

  @RabbitListener(queues = RabbitMQConfig.TENANT_SCHEMA_DROP_QUEUE)
  public void onTenantDropped(TenantDroppedEvent event) {
    log.info("Received TenantDroppedEvent: {}", event);

    try {
      schemaProvisioningService.dropTenantSchema(event.getTenantName());
      log.info("Finished processing for event: {}", event.getEventId());
    } catch (Exception e) {
      log.error(
          "Failed to provision schema for tenant: {}. Error: {}",
          event.getTenantName(),
          e.getMessage());
      throw e;
    }
  }
}
