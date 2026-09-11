package com.microservices.board.tenant;

import java.util.regex.Pattern;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;

/** 새 테넌트 스키마가 만들어질 때, 그 스키마 안에 board-service 소유 테이블(boards)을 생성한다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class BoardSchemaMigrationService {

  private static final Pattern SAFE_SCHEMA_NAME = Pattern.compile("^[a-zA-Z0-9_]+$");

  private final DataSource dataSource;

  public void migrate(String tenantSchema) {
    if (tenantSchema == null || !SAFE_SCHEMA_NAME.matcher(tenantSchema).matches()) {
      throw new IllegalArgumentException("Invalid tenant schema identifier: " + tenantSchema);
    }

    log.info("Migrating board-service schema for tenant: {}", tenantSchema);

    Flyway.configure()
        .dataSource(dataSource)
        .schemas(tenantSchema)
        .locations("classpath:db/migration/board")
        .baselineOnMigrate(true)
        .load()
        .migrate();

    log.info("Finished migrating board-service schema for tenant: {}", tenantSchema);
  }
}
