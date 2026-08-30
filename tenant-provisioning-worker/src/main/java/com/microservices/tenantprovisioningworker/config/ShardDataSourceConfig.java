package com.microservices.tenantprovisioningworker.config;

import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 테넌트 스키마가 배치될 수 있는 물리 DB(shard)별 JdbcTemplate 레지스트리. shard-1은 이 서비스에 기본 설정된 spring.datasource를
 * 그대로 재사용하고, 그 외 shard는 {@link ShardDataSourceProperties}로 추가된 만큼만 별도 커넥션 풀을 만든다.
 */
@Configuration
@EnableConfigurationProperties(ShardDataSourceProperties.class)
@RequiredArgsConstructor
public class ShardDataSourceConfig {

  public static final String DEFAULT_SHARD_KEY = "shard-1";

  private final JdbcTemplate jdbcTemplate;
  private final ShardDataSourceProperties shardDataSourceProperties;

  @Bean
  public Map<String, JdbcTemplate> shardJdbcTemplates() {
    Map<String, JdbcTemplate> shardJdbcTemplates = new HashMap<>();
    shardJdbcTemplates.put(DEFAULT_SHARD_KEY, jdbcTemplate);

    shardDataSourceProperties
        .getShards()
        .forEach((shardKey, props) -> shardJdbcTemplates.put(shardKey, buildJdbcTemplate(props)));

    return Map.copyOf(shardJdbcTemplates);
  }

  private JdbcTemplate buildJdbcTemplate(ShardProperties props) {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(props.getUrl());
    dataSource.setUsername(props.getUsername());
    dataSource.setPassword(props.getPassword());
    dataSource.setDriverClassName(props.getDriverClassName());
    return new JdbcTemplate(dataSource);
  }
}
