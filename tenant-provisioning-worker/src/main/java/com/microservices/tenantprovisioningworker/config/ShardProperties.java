package com.microservices.tenantprovisioningworker.config;

import lombok.Getter;
import lombok.Setter;

/** shard-1 이외에 추가로 연결할 물리 DB 인스턴스 하나의 접속 정보. */
@Getter
@Setter
public class ShardProperties {

  private String url;
  private String username;
  private String password;
  private String driverClassName = "org.postgresql.Driver";
}
