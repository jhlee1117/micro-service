package com.microservices.auth.dto;

import java.util.List;

public record PermissionAssignmentRequest(List<Long> permissionIds) {}
