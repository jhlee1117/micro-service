package com.microservices.auth_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.microservices.auth_service.domain.entity.Tenant;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByName(String name);

    Optional<Tenant> findById(Long id);

    boolean existsByName(String name);

    boolean existsById(Long id);

    boolean existsByStatus(boolean status);
    
}
