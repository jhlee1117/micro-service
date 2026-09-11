package com.microservices.board.tenant;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 단일 DataSource 위에서 커넥션마다 {@code SET search_path}로 테넌트 스키마를 전환하는 커넥션 프로바이더. */
@Component
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

  private static final Pattern SAFE_SCHEMA_NAME = Pattern.compile("^[a-zA-Z0-9_]+$");

  @Autowired private transient DataSource dataSource;

  @Override
  public Connection getAnyConnection() throws SQLException {
    return dataSource.getConnection();
  }

  @Override
  public void releaseAnyConnection(Connection connection) throws SQLException {
    resetSearchPath(connection);
    connection.close();
  }

  @Override
  public Connection getConnection(String tenantIdentifier) throws SQLException {
    Connection connection = dataSource.getConnection();
    setSearchPath(connection, tenantIdentifier);
    return connection;
  }

  @Override
  public void releaseConnection(String tenantIdentifier, Connection connection)
      throws SQLException {
    resetSearchPath(connection);
    connection.close();
  }

  @Override
  public boolean supportsAggressiveRelease() {
    return false;
  }

  @Override
  public boolean isUnwrappableAs(Class<?> unwrapType) {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> unwrapType) {
    throw new UnsupportedOperationException("Cannot unwrap " + unwrapType);
  }

  private void setSearchPath(Connection connection, String schema) throws SQLException {
    validateSchemaName(schema);
    try (Statement statement = connection.createStatement()) {
      statement.execute("SET search_path TO \"" + schema + "\", public");
    }
  }

  private void resetSearchPath(Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute("SET search_path TO public");
    }
  }

  private void validateSchemaName(String schema) {
    if (schema == null || !SAFE_SCHEMA_NAME.matcher(schema).matches()) {
      throw new IllegalArgumentException("Invalid tenant schema identifier: " + schema);
    }
  }
}
