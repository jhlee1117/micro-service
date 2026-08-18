package com.microservices.auth.repository;

import com.microservices.auth.domain.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

  Optional<Role> findByName(String name);

  boolean existsByName(String name);

  Optional<Role> findById(Long id);
}
