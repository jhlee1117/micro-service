package com.microservices.auth.dto;

import java.util.List;

public record ModuleTenantsAssignmentDto(
    Long moduleId, List<TenantModuleAssignmentDto> assignments) {}
