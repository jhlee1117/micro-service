package com.microservices.auth.service;

import com.common.exceptions.BusinessException;
import com.common.exceptions.code.PermissionErrorCode;
import com.common.exceptions.code.RoleErrorCode;
import com.common.exceptions.code.UserErrorCode;
import com.microservices.auth.domain.entity.Permission;
import com.microservices.auth.domain.entity.Role;
import com.microservices.auth.domain.entity.RolePermission;
import com.microservices.auth.domain.entity.User;
import com.microservices.auth.dto.PermissionAssignmentRequest;
import com.microservices.auth.dto.RoleDto;
import com.microservices.auth.dto.RolePermissionAssignmentDto;
import com.microservices.auth.repository.PermissionRepository;
import com.microservices.auth.repository.RolePermissionRepository;
import com.microservices.auth.repository.RoleRepository;
import com.microservices.auth.repository.UserRepository;
import com.microservices.auth.repository.UserRoleRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

  @Autowired RoleRepository roleRepository;

  @Autowired RolePermissionRepository rolePermissionRepository;

  @Autowired PermissionRepository permissionRepository;

  @Autowired UserRoleRepository userRoleRepository;

  @Autowired UserRepository userRepository;

  private static final String SUPER_ROLE = "ROLE_SUPER_ADMIN";

  @Transactional(readOnly = true)
  public List<RoleDto> getRoleList() {
    return roleRepository.findAll().stream().map(RoleDto::fromEntity).toList();
  }

  @Transactional(readOnly = true)
  public RoleDto getRoleById(Long id) {
    return RoleDto.fromEntity(findRole(id));
  }

  @Transactional
  public RoleDto createRole(RoleDto roleDto) {
    validateRoleUniqueness(roleDto.getName(), null);

    Role role =
        Role.builder()
            .name(roleDto.getName())
            .description(roleDto.getDescription())
            .isSystemRole(roleDto.isSystemRole())
            .build();

    return RoleDto.fromEntity(roleRepository.save(role));
  }

  @Transactional
  public RoleDto updateRole(Long roleId, RoleDto roleDto) {
    Role existingRole = findRole(roleId);
    validateRoleUniqueness(roleDto.getName(), existingRole.getId());

    Role updatedRole =
        Role.builder()
            .id(existingRole.getId())
            .name(roleDto.getName())
            .description(roleDto.getDescription())
            .isSystemRole(roleDto.isSystemRole())
            .build();

    return RoleDto.fromEntity(roleRepository.save(updatedRole));
  }

  @Transactional
  public void deleteRole(Long roleId, String userName) {
    validateDeletePermission(userName);

    Role role = findRole(roleId);
    roleRepository.delete(role);
  }

  @Transactional(readOnly = true)
  public RolePermissionAssignmentDto getRolePermissions(Long roleId) {
    Role role = findRole(roleId);
    List<Long> permissionIds =
        rolePermissionRepository.findAllByRoleOrderByPermissionId(role).stream()
            .map(rolePermission -> rolePermission.getPermission().getId())
            .toList();

    return new RolePermissionAssignmentDto(role.getId(), permissionIds);
  }

  @Transactional
  public RolePermissionAssignmentDto updateRolePermissions(
      Long roleId, PermissionAssignmentRequest request) {
    Role role = findRole(roleId);
    List<Permission> permissions = findPermissions(request.permissionIds());

    rolePermissionRepository.deleteByRole(role);
    rolePermissionRepository.flush();

    List<RolePermission> rolePermissions =
        permissions.stream().map(permission -> createRolePermission(role, permission)).toList();
    rolePermissionRepository.saveAll(rolePermissions);

    return new RolePermissionAssignmentDto(
        role.getId(), permissions.stream().map(Permission::getId).toList());
  }

  private Role findRole(Long roleId) {
    return roleRepository
        .findById(roleId)
        .orElseThrow(() -> new BusinessException(RoleErrorCode.ROLE_NOT_FOUND));
  }

  private void validateRoleUniqueness(String name, Long excludeRoleId) {
    roleRepository
        .findByName(name)
        .filter(role -> excludeRoleId == null || !role.getId().equals(excludeRoleId))
        .ifPresent(
            role -> {
              throw new BusinessException(RoleErrorCode.ALREADY_EXISTS_ROLE);
            });
  }

  private void validateDeletePermission(String userName) {
    User user =
        userRepository
            .findByUsername(userName)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

    boolean hasDeletePermission =
        userRoleRepository.existsByUserIdAndRoleNameAndSystemRole(user.getId(), SUPER_ROLE, true);

    if (!hasDeletePermission) {
      throw new BusinessException(RoleErrorCode.NOT_PERMMITTED_ROLE);
    }
  }

  private RolePermission createRolePermission(Role role, Permission permission) {
    RolePermission rolePermission = new RolePermission();
    rolePermission.setRole(role);
    rolePermission.setPermission(permission);
    return rolePermission;
  }

  private List<Permission> findPermissions(List<Long> permissionIds) {
    Set<Long> uniquePermissionIds =
        permissionIds == null ? Set.of() : new LinkedHashSet<>(permissionIds);
    List<Permission> permissions = permissionRepository.findAllById(uniquePermissionIds);

    if (permissions.size() != uniquePermissionIds.size()) {
      throw new BusinessException(PermissionErrorCode.PERMISSION_NOT_FOUND);
    }

    return permissions;
  }
}
