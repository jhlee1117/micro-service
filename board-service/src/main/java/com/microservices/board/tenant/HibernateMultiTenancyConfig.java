package com.microservices.board.tenant;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class HibernateMultiTenancyConfig {

  @Bean
  public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(
      @Lazy SchemaMultiTenantConnectionProvider connectionProvider,
      @Lazy TenantIdentifierResolver tenantIdentifierResolver) {
    return hibernateProperties -> {
      hibernateProperties.put(
          AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
      hibernateProperties.put(
          AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantIdentifierResolver);
    };
  }
}
