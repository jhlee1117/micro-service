package com.microservices.auth.repository;

import com.microservices.auth.domain.entity.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

  List<UserRole> findByUserId(Long userId);

  List<UserRole> findByRoleId(Long roleId);

  List<UserRole> findByGrantedById(Long grantedById);

  @Transactional
  void deleteByUserId(Long userId);

  boolean existsByUserIdAndRoleId(Long userId, Long roleId);

  @Query(
      "select count(ur) > 0 "
          + "from UserRole ur "
          + "where ur.user.id = :userId "
          + "and ur.role.name = :roleName "
          + "and ur.role.isSystemRole = :isSystemRole")
  boolean existsByUserIdAndRoleNameAndSystemRole(
      @Param("userId") Long userId,
      @Param("roleName") String roleName,
      @Param("isSystemRole") boolean isSystemRole);
}
