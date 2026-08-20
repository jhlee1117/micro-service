package com.microservices.auth.dto;

import java.util.List;

public record TenantModuleAssignmentsRequest(List<TenantModuleAssignmentDto> assignments) {}
