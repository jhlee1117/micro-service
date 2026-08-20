package com.microservices.auth.repository;

import com.microservices.auth.domain.entity.Role;
import com.microservices.auth.domain.entity.RolePermission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

  @Query(
      "select rolePermission from RolePermission rolePermission "
          + "where rolePermission.role = :role order by rolePermission.permission.id")
  List<RolePermission> findAllByRoleOrderByPermissionId(Role role);

  void deleteByRole(Role role);
}
