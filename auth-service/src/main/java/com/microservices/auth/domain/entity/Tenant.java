package com.microservices.auth.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "tenant",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_tenant_name",
          columnNames = {"name"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@lombok.experimental.SuperBuilder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Tenant extends BaseAuditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "status", nullable = false)
  private boolean status;

  /** 이 테넌트의 스키마가 실제로 위치한 물리 DB(db_instance.shard_key)를 가리킨다. */
  @Column(name = "shard_key", nullable = false, length = 50)
  @Builder.Default
  private String shardKey = "shard-1";
}
