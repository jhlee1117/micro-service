package com.microservices.auth.dto;

import java.util.List;

public record RolePermissionAssignmentDto(Long roleId, List<Long> permissionIds) {}
