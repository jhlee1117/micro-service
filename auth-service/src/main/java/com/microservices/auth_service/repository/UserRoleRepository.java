package com.microservices.auth_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.microservices.auth_service.domain.entity.UserRole;
import com.microservices.auth_service.domain.entity.UserRoleId;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByRoleId(Long roleId);

    List<UserRole> findByGrantedById(Long grantedById);
}
