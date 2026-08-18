package com.microservices.tenantprovisioningworker.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class SchemaProvisioningServiceTest {

  @Mock private JdbcTemplate jdbcTemplate;

  @InjectMocks private SchemaProvisioningService schemaProvisioningService;

  @Test
  void createTenantSchema_executesCreateSchemaQuery() {
    // When
    schemaProvisioningService.createTenantSchema("test_tenant");

    // Then
    verify(jdbcTemplate, times(1)).execute("CREATE SCHEMA IF NOT EXISTS test_tenant");
  }

  @Test
  void createTenantSchema_throwsExceptionWhenTenantNameIsInvalid() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> schemaProvisioningService.createTenantSchema("test-tenant"));
    verify(jdbcTemplate, never()).execute(anyString());
  }

  @Test
  void dropTenantSchema_executesDropSchemaQuery() {
    // When
    schemaProvisioningService.dropTenantSchema("test_tenant");

    // Then
    verify(jdbcTemplate, times(1)).execute("DROP SCHEMA IF EXISTS test_tenant CASCADE ");
  }
}
