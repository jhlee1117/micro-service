package com.microservices.auth.repository;

import com.microservices.auth.domain.entity.Tenant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

  Optional<Tenant> findByName(String name);

  Optional<Tenant> findById(Long id);

  boolean existsByName(String name);

  boolean existsById(Long id);

  boolean existsByStatus(boolean status);
}
