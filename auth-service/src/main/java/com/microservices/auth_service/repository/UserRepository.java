package com.microservices.auth_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import com.microservices.auth_service.domain.entity.User;

import java.util.List;
import jakarta.persistence.LockModeType;

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
