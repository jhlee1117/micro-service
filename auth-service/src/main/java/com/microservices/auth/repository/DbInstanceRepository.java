package com.microservices.auth.repository;

import com.microservices.auth.domain.entity.DbInstance;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DbInstanceRepository extends JpaRepository<DbInstance, Long> {

  Optional<DbInstance> findByShardKey(String shardKey);
}
