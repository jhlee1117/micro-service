package com.microservices.auth.repository;

import com.microservices.auth.domain.entity.Module;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

  Optional<Module> findByName(String name);

  boolean existsByName(String name);
}
