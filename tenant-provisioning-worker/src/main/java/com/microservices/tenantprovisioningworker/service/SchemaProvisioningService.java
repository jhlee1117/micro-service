package com.microservices.tenantprovisioningworker.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchemaProvisioningService {

  private final Map<String, JdbcTemplate> shardJdbcTemplates;

  @Transactional
  public void createTenantSchema(String tenantName, String shardKey) {
    log.info("Starting schema provisioning for tenant: {} on shard: {}", tenantName, shardKey);

    // 1. 보안 검증
    checkTenantName(tenantName);
    JdbcTemplate jdbcTemplate = resolveJdbcTemplate(shardKey);

    // 2. 스키마 생성 쿼리 실행
    String createSchemaQuery = "CREATE SCHEMA IF NOT EXISTS " + tenantName;
    jdbcTemplate.execute(createSchemaQuery);

    log.info("Successfully created schema: {} on shard: {}", tenantName, shardKey);
  }

  private static void checkTenantName(String tenantName) {
    if (!tenantName.matches("^[a-zA-Z0-9_]+$")) {
      throw new IllegalArgumentException("Invalid tenant name format: " + tenantName);
    }
  }

  private JdbcTemplate resolveJdbcTemplate(String shardKey) {
    JdbcTemplate jdbcTemplate = shardJdbcTemplates.get(shardKey);
    if (jdbcTemplate == null) {
      throw new IllegalStateException("Unknown shard key: " + shardKey);
    }
    return jdbcTemplate;
  }

  @Transactional
  public void dropTenantSchema(String tenantName, String shardKey) {
    log.info("Deleting schema provisioning for tenant: {} on shard: {}", tenantName, shardKey);

    // 1. 보안 검증
    checkTenantName(tenantName);
    JdbcTemplate jdbcTemplate = resolveJdbcTemplate(shardKey);

    // 2. 스키마 삭제 쿼리 실행
    String dropSchemaQuery = "DROP SCHEMA IF EXISTS " + tenantName + " CASCADE ";
    jdbcTemplate.execute(dropSchemaQuery);

    log.info("Successfully deleted schema: {} on shard: {}", tenantName, shardKey);
  }
}
