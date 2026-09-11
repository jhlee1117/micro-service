package com.microservices.tenantprovisioningworker.listener;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.microservices.tenantprovisioningworker.event.TenantCreatedEvent;
import com.microservices.tenantprovisioningworker.event.TenantDroppedEvent;
import com.microservices.tenantprovisioningworker.service.SchemaProvisioningService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantEventListenerTest {

  @Mock private SchemaProvisioningService schemaProvisioningService;

  @InjectMocks private TenantEventListener tenantEventListener;

  @Test
  void onTenantCreated_callsCreateTenantSchema() {
    // Given
    TenantCreatedEvent event =
        new TenantCreatedEvent("event-1", 1L, "test_tenant", "shard-1", LocalDateTime.now());

    // When
    tenantEventListener.onTenantCreated(event);

    // Then
    verify(schemaProvisioningService, times(1)).createTenantSchema("test_tenant", "shard-1");
  }

  @Test
  void onTenantCreated_propagatesExceptionWhenSchemaCreationFails() {
    // Given
    TenantCreatedEvent event =
        new TenantCreatedEvent("event-1", 1L, "test_tenant", "shard-1", LocalDateTime.now());
    doThrow(new IllegalStateException("schema create failed"))
        .when(schemaProvisioningService)
        .createTenantSchema("test_tenant", "shard-1");

    // When & Then
    assertThrows(IllegalStateException.class, () -> tenantEventListener.onTenantCreated(event));
    verify(schemaProvisioningService, times(1)).createTenantSchema("test_tenant", "shard-1");
  }

  @Test
  void onTenantDropped_callsDropTenantSchema() {
    // Given
    TenantDroppedEvent event =
        new TenantDroppedEvent("event-2", 1L, "test_tenant", "shard-1", LocalDateTime.now());

    // When
    tenantEventListener.onTenantDropped(event);

    // Then
    verify(schemaProvisioningService, times(1)).dropTenantSchema("test_tenant", "shard-1");
  }
}
