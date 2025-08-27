package com.microservices.auth_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.microservices.auth_service.domain.entity.UserRole;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByRoleId(Long roleId);

    List<UserRole> findByGrantedById(Long grantedById);
    
    @Transactional
    void deleteByUserId(Long userId);
    
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
}
