package com.microservices.auth.dto;

import com.microservices.auth.domain.entity.TenantModule;

public record TenantModuleAssignmentDto(
    Long tenantId, Long moduleId, String planType, boolean enabled) {

  public static TenantModuleAssignmentDto fromEntity(TenantModule tenantModule) {
    return new TenantModuleAssignmentDto(
        tenantModule.getTenant().getId(),
        tenantModule.getModule().getId(),
        tenantModule.getPlanType(),
        tenantModule.isEnabled());
  }
}
