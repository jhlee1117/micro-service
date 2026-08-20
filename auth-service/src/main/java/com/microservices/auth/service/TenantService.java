package com.microservices.auth.service;

import com.common.exceptions.BusinessException;
import com.common.exceptions.code.ModuleErrorCode;
import com.common.exceptions.code.TenantErrorCode;
import com.microservices.auth.domain.entity.Module;
import com.microservices.auth.domain.entity.Tenant;
import com.microservices.auth.domain.entity.TenantModule;
import com.microservices.auth.dto.TenantDto;
import com.microservices.auth.dto.TenantModuleAssignmentDto;
import com.microservices.auth.dto.TenantModuleAssignmentsRequest;
import com.microservices.auth.dto.TenantModulesAssignmentDto;
import com.microservices.auth.event.TenantCreatedEvent;
import com.microservices.auth.event.TenantDroppedEvent;
import com.microservices.auth.repository.ModuleRepository;
import com.microservices.auth.repository.TenantModuleRepository;
import com.microservices.auth.repository.TenantRepository;
import com.microservices.auth.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {

  private final Logger logger = LoggerFactory.getLogger(TenantService.class);

  @Autowired TenantRepository tenantRepository;

  @Autowired ModuleRepository moduleRepository;

  @Autowired TenantModuleRepository tenantModuleRepository;

  @Autowired UserRepository userRepository;

  @Autowired ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  @Cacheable(value = "tenants")
  public List<TenantDto> getTenantList() {
    return tenantRepository.findAll().stream()
        .map(tenant -> new TenantDto(tenant.getId(), tenant.getName(), tenant.isStatus()))
        .toList();
  }

  @Cacheable(value = "tenants", key = "#id")
  public TenantDto getTenantDtoById(Long id) {
    logger.info("Cache miss for tenant ID: {}. Fetching from DB...", id);
    Tenant tenant =
        tenantRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(TenantErrorCode.TENANT_NOT_FOUND));
    return TenantDto.fromEntity(tenant);
  }

  public Optional<Tenant> findById(Long id) {
    return tenantRepository.findById(id);
  }

  @Transactional
  @CacheEvict(value = "tenants", allEntries = true)
  public TenantDto createTenant(TenantDto tenantDto) {
    validateTenantUniqueness(tenantDto.getName(), null);

    Tenant tenant = Tenant.builder().name(tenantDto.getName()).status(tenantDto.isStatus()).build();

    Tenant savedTenant = tenantRepository.save(tenant);

    // eventPublisher 에 테넌트 생성 이벤트가 실행되었다는 것을 알림
    eventPublisher.publishEvent(new TenantCreatedEvent(savedTenant));

    return TenantDto.fromEntity(savedTenant);
  }

  private void validateTenantUniqueness(String name, Long excludeTenantId) {
    Optional<Tenant> existingTenant = tenantRepository.findByName(name);
    if (existingTenant.isPresent()
        && (excludeTenantId == null || !existingTenant.get().getId().equals(excludeTenantId))) {
      Map<String, Object> params = Map.of("name", name);
      throw new BusinessException(TenantErrorCode.ALREADY_EXISTS_TENANT, params);
    }
  }

  @Transactional
  @CacheEvict(value = "tenants", allEntries = true)
  public TenantDto updateTenant(Long id, TenantDto tenantDto) {

    // 기존 테넌트 조회
    Tenant existingTenant =
        tenantRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(TenantErrorCode.TENANT_NOT_FOUND));

    Tenant updatedTenant =
        Tenant.builder()
            .id(existingTenant.getId())
            .name(existingTenant.getName())
            .status(tenantDto.isStatus())
            .build();

    return TenantDto.fromEntity(tenantRepository.save(updatedTenant));
  }

  @Transactional
  @CacheEvict(value = "tenants", allEntries = true)
  public void deleteTenant(Long id) {
    // 1. 조회 및 검증
    Tenant existingTenant =
        tenantRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(TenantErrorCode.TENANT_NOT_FOUND));

    if (userRepository.existsByTenantId(existingTenant.getId())) {
      Map<String, Object> params = Map.of("name", existingTenant.getName());
      throw new BusinessException(TenantErrorCode.ALREADY_EXISTS_TENANT, params);
    }

    // 2. 비즈니스 로직 수행
    tenantRepository.deleteById(existingTenant.getId());

    eventPublisher.publishEvent(new TenantDroppedEvent(existingTenant));
  }

  @Transactional(readOnly = true)
  public TenantModulesAssignmentDto getTenantModules(Long tenantId) {
    Tenant tenant = findTenant(tenantId);
    List<TenantModuleAssignmentDto> assignments =
        tenantModuleRepository.findAllByTenantOrderByModuleId(tenant).stream()
            .map(TenantModuleAssignmentDto::fromEntity)
            .toList();

    return new TenantModulesAssignmentDto(tenant.getId(), assignments);
  }

  @Transactional
  public TenantModulesAssignmentDto updateTenantModules(
      Long tenantId, TenantModuleAssignmentsRequest request) {
    Tenant tenant = findTenant(tenantId);
    List<TenantModuleAssignmentDto> assignments = normalizeAssignments(request);

    tenantModuleRepository.deleteByTenant(tenant);
    tenantModuleRepository.flush();

    List<TenantModule> tenantModules =
        assignments.stream().map(assignment -> createTenantModule(tenant, assignment)).toList();
    tenantModuleRepository.saveAll(tenantModules);

    return new TenantModulesAssignmentDto(
        tenant.getId(), tenantModules.stream().map(TenantModuleAssignmentDto::fromEntity).toList());
  }

  private Tenant findTenant(Long tenantId) {
    return tenantRepository
        .findById(tenantId)
        .orElseThrow(() -> new BusinessException(TenantErrorCode.TENANT_NOT_FOUND));
  }

  private TenantModule createTenantModule(Tenant tenant, TenantModuleAssignmentDto assignment) {
    Module module =
        moduleRepository
            .findById(assignment.moduleId())
            .orElseThrow(() -> new BusinessException(ModuleErrorCode.MODULE_NOT_FOUND));

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
      if (assignment.moduleId() == null) {
        throw new BusinessException(ModuleErrorCode.MODULE_NOT_FOUND);
      }
      uniqueAssignments.put(assignment.moduleId(), assignment);
    }

    return List.copyOf(uniqueAssignments.values());
  }
}
