package com.microservices.auth.dto;

import java.util.List;

public record MenuPermissionAssignmentDto(String menuCode, List<Long> permissionIds) {}
