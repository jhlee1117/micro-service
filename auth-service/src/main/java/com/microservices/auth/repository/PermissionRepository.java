package com.microservices.auth.repository;

import com.microservices.auth.domain.entity.Permission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

  Optional<Permission> findByCode(String code);

  boolean existsByCode(String code);
}
