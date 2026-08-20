package com.microservices.auth.service;

import com.common.exceptions.BusinessException;
import com.common.exceptions.code.ModuleErrorCode;
import com.common.exceptions.code.TenantErrorCode;
import com.microservices.auth.domain.entity.Module;
import com.microservices.auth.domain.entity.Tenant;
import com.microservices.auth.domain.entity.TenantModule;
import com.microservices.auth.dto.ModuleDto;
import com.microservices.auth.dto.ModuleTenantsAssignmentDto;
import com.microservices.auth.dto.TenantModuleAssignmentDto;
import com.microservices.auth.dto.TenantModuleAssignmentsRequest;
import com.microservices.auth.repository.ModuleRepository;
import com.microservices.auth.repository.TenantModuleRepository;
import com.microservices.auth.repository.TenantRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModuleService {

  private final ModuleRepository moduleRepository;
  private final TenantRepository tenantRepository;
  private final TenantModuleRepository tenantModuleRepository;

  @Transactional(readOnly = true)
  public List<ModuleDto> getModuleList() {
    return moduleRepository.findAll().stream().map(ModuleDto::fromEntity).toList();
  }

  @Transactional(readOnly = true)
  public ModuleDto getModuleById(Long id) {
    return ModuleDto.fromEntity(findModule(id));
  }

  @Transactional
  public ModuleDto createModule(ModuleDto moduleDto) {
    validateModuleUniqueness(moduleDto.getName(), null);

    Module module =
        Module.builder()
            .name(moduleDto.getName())
            .url(moduleDto.getUrl())
            .description(moduleDto.getDescription())
            .build();

    return ModuleDto.fromEntity(moduleRepository.save(module));
  }

  @Transactional
  public ModuleDto updateModule(Long id, ModuleDto moduleDto) {
    Module existingModule = findModule(id);
    validateModuleUniqueness(moduleDto.getName(), existingModule.getId());

    Module updatedModule =
        Module.builder()
            .id(existingModule.getId())
            .name(moduleDto.getName())
            .url(moduleDto.getUrl())
            .description(moduleDto.getDescription())
            .build();

    return ModuleDto.fromEntity(moduleRepository.save(updatedModule));
  }

  @Transactional
  public void deleteModule(Long id) {
    Module module = findModule(id);
    moduleRepository.delete(module);
  }

  @Transactional(readOnly = true)
  public ModuleTenantsAssignmentDto getModuleTenants(Long moduleId) {
    Module module = findModule(moduleId);
    List<TenantModuleAssignmentDto> assignments =
        tenantModuleRepository.findAllByModuleOrderByTenantId(module).stream()
            .map(TenantModuleAssignmentDto::fromEntity)
            .toList();

    return new ModuleTenantsAssignmentDto(module.getId(), assignments);
  }

  @Transactional
  public ModuleTenantsAssignmentDto updateModuleTenants(
      Long moduleId, TenantModuleAssignmentsRequest request) {
    Module module = findModule(moduleId);
    List<TenantModuleAssignmentDto> assignments = normalizeAssignments(request);

    tenantModuleRepository.deleteByModule(module);
    tenantModuleRepository.flush();

    List<TenantModule> tenantModules =
        assignments.stream().map(assignment -> createTenantModule(module, assignment)).toList();
    tenantModuleRepository.saveAll(tenantModules);

    return new ModuleTenantsAssignmentDto(
        module.getId(), tenantModules.stream().map(TenantModuleAssignmentDto::fromEntity).toList());
  }

  private Module findModule(Long id) {
    return moduleRepository
        .findById(id)
        .orElseThrow(() -> new BusinessException(ModuleErrorCode.MODULE_NOT_FOUND));
  }

  private TenantModule createTenantModule(Module module, TenantModuleAssignmentDto assignment) {
    Tenant tenant =
        tenantRepository
            .findById(assignment.tenantId())
            .orElseThrow(() -> new BusinessException(TenantErrorCode.TENANT_NOT_FOUND));

    TenantModule tenantModule = new TenantModule();
    tenantModule.setTenant(tenant);
    tenantModule.setModule(module);
    tenantModule.setPlanType(assignment.planType());
    tenantModule.setEnabled(assignment.enabled());
    return tenantModule;
  }

  private List<TenantModuleAssignmentDto> normalizeAssignments(
      TenantModuleAssignmentsRequest request) {
    List<TenantModuleAssignmentDto> assignments =
        request == null || request.assignments() == null ? List.of() : request.assignments();
    Map<Long, TenantModuleAssignmentDto> uniqueAssignments = new LinkedHashMap<>();

    for (TenantModuleAssignmentDto assignment : assignments) {
      if (assignment.tenantId() == null) {
        throw new BusinessException(TenantErrorCode.TENANT_NOT_FOUND);
      }
      uniqueAssignments.put(assignment.tenantId(), assignment);
    }

    return List.copyOf(uniqueAssignments.values());
  }

  private void validateModuleUniqueness(String name, Long excludeModuleId) {
    moduleRepository
        .findByName(name)
        .filter(module -> excludeModuleId == null || !module.getId().equals(excludeModuleId))
        .ifPresent(
            module -> {
              throw new BusinessException(ModuleErrorCode.ALREADY_EXISTS_MODULE);
            });
  }
}
