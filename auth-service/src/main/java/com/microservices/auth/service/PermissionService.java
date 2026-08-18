package com.microservices.auth.service;

import com.common.exceptions.BusinessException;
import com.common.exceptions.code.PermissionErrorCode;
import com.microservices.auth.domain.entity.Permission;
import com.microservices.auth.dto.PermissionDto;
import com.microservices.auth.repository.PermissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionService {

  private final PermissionRepository permissionRepository;

  @Transactional(readOnly = true)
  public List<PermissionDto> getPermissionList() {
    return permissionRepository.findAll().stream().map(PermissionDto::fromEntity).toList();
  }

  @Transactional(readOnly = true)
  public PermissionDto getPermissionById(Long id) {
    return PermissionDto.fromEntity(findPermission(id));
  }

  @Transactional
  public PermissionDto createPermission(PermissionDto permissionDto) {
    validatePermissionUniqueness(permissionDto.getCode(), null);

    Permission permission =
        Permission.builder()
            .code(permissionDto.getCode())
            .resource(permissionDto.getResource())
            .action(permissionDto.getAction())
            .description(permissionDto.getDescription())
            .build();

    return PermissionDto.fromEntity(permissionRepository.save(permission));
  }

  @Transactional
  public PermissionDto updatePermission(Long id, PermissionDto permissionDto) {
    Permission existingPermission = findPermission(id);
    validatePermissionUniqueness(permissionDto.getCode(), existingPermission.getId());

    Permission updatedPermission =
        Permission.builder()
            .id(existingPermission.getId())
            .code(permissionDto.getCode())
            .resource(permissionDto.getResource())
            .action(permissionDto.getAction())
            .description(permissionDto.getDescription())
            .build();

    return PermissionDto.fromEntity(permissionRepository.save(updatedPermission));
  }

  @Transactional
  public void deletePermission(Long id) {
    Permission permission = findPermission(id);
    permissionRepository.delete(permission);
  }

  private Permission findPermission(Long id) {
    return permissionRepository
        .findById(id)
        .orElseThrow(() -> new BusinessException(PermissionErrorCode.PERMISSION_NOT_FOUND));
  }

  private void validatePermissionUniqueness(String code, Long excludePermissionId) {
    permissionRepository
        .findByCode(code)
        .filter(
            permission ->
                excludePermissionId == null || !permission.getId().equals(excludePermissionId))
        .ifPresent(
            permission -> {
              throw new BusinessException(PermissionErrorCode.ALREADY_EXISTS_PERMISSION);
            });
  }
}
