package com.microservices.auth.dto;

import java.util.List;

public record TenantModulesAssignmentDto(
    Long tenantId, List<TenantModuleAssignmentDto> assignments) {}
