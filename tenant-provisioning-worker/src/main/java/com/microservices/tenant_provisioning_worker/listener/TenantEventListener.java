package com.microservices.tenant_provisioning_worker.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.microservices.tenant_provisioning_worker.config.RabbitMQConfig;
import com.microservices.tenant_provisioning_worker.event.TenantCreatedEvent;
import com.microservices.tenant_provisioning_worker.service.SchemaProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
            log.error("Failed to provision schema for tenant: {}. Error: {}", event.getTenantName(), e.getMessage());
            throw e;
        }
    }
}
