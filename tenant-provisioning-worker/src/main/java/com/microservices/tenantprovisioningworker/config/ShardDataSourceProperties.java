package com.microservices.tenantprovisioningworker.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * shard-1(기본 spring.datasource)을 제외하고, 추가로 붙는 물리 DB 인스턴스를 shardKey로 등록한다.
 *
 * <pre>
 * provisioning:
 *   shards:
 *     shard-2:
 *       url: jdbc:postgresql://postgres-2:5432/micro_db
 *       username: ...
 *       password: ...
 * </pre>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "provisioning")
public class ShardDataSourceProperties {

  private Map<String, ShardProperties> shards = new HashMap<>();
}
