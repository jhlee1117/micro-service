package com.microservices.tenant_provisioning_worker.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchemaProvisioningService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void createTenantSchema(String tenantName) {
        log.info("Starting schema provisioning for tenant: {}", tenantName);

        // 1. 보안 검증
        if (!tenantName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid tenant name format: " + tenantName);
        }

        // 2. 스키마 생성 쿼리 실행
        String createSchemaQuery = "CREATE SCHEMA IF NOT EXISTS " + tenantName;
        jdbcTemplate.execute(createSchemaQuery);

        log.info("Successfully created schema: {}", tenantName);
    }
}
