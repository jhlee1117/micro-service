package com.microservices.auth.repository;

import com.microservices.auth.domain.entity.User;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<User> findBySignupTokenHash(String signupTokenHash);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  List<User> findAll();

  boolean existsByTenantId(Long tenantId);
}
