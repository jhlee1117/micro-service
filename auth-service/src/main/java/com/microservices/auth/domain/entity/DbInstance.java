package com.microservices.auth.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * 테넌트 스키마가 실제로 배치되는 물리 DB(shard)의 메타데이터. 접속 정보(URL/자격증명)는 여기에 저장하지 않고 각 서비스의 설정(config
 * server)에서 shardKey를 키로 관리한다.
 */
@Entity
@Table(
    name = "db_instance",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_db_instance_shard_key", columnNames = {"shard_key"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@lombok.experimental.SuperBuilder
public class DbInstance extends BaseAuditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "shard_key", nullable = false, length = 50)
  private String shardKey;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private DbInstanceStatus status = DbInstanceStatus.ACTIVE;

  @Column(name = "capacity_weight", nullable = false)
  @Builder.Default
  private int capacityWeight = 100;
}
