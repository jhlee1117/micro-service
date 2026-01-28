package com.microservices.auth_service.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.common.exceptions.BusinessException;
import com.common.exceptions.code.TenantErrorCode;
import com.microservices.auth_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.microservices.auth_service.domain.entity.Tenant;
import com.microservices.auth_service.dto.TenantDto;
import com.microservices.auth_service.repository.TenantRepository;

@Service
public class TenantService {

    private final Logger logger = LoggerFactory.getLogger(TenantService.class);

    @Autowired
    TenantRepository tenantRepository;

    @Autowired
    UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TenantDto> getTenantList() {
        return tenantRepository.findAll().
            stream().map(tenant -> new TenantDto(tenant.getId(), tenant.getName(), tenant.isStatus())).toList();
    }

    @Transactional
    public ResponseEntity<Object> createTenant(TenantDto tenantDto) {
        validateTenantUniqueness(tenantDto.getName(), null);

        Tenant tenant = Tenant.builder()
            .name(tenantDto.getName())
            .status(tenantDto.isStatus())
            .build();

        return ResponseEntity.ok(TenantDto.fromEntity(tenantRepository.save(tenant)));
    }

    private void validateTenantUniqueness(String name, Long excludeTenantId) {
        Optional<Tenant> existingTenant = tenantRepository.findByName(name);
        if (existingTenant.isPresent() &&
            (excludeTenantId == null || !existingTenant.get().getId().equals(excludeTenantId))) {
            Map<String, Object> params = Map.of("name", name);
            throw new BusinessException(TenantErrorCode.ALREADY_EXISTS_TENANT, params);
        }
    }

    @Transactional
    public ResponseEntity<Object> updateTenant(Long id, TenantDto tenantDto) {

        // 기존 테넌트 조회
        Optional<Tenant> existingTenantOpt = tenantRepository.findById(id);
        if (existingTenantOpt.isEmpty()) {
            throw new BusinessException(TenantErrorCode.TENANT_NOT_FOUND);
        }
        Tenant existingTenant = existingTenantOpt.get();

        Tenant updatedTenant = Tenant.builder()
            .id(existingTenant.getId())
            .name(existingTenant.getName())
            .status(tenantDto.isStatus())
            .createdAt(existingTenant.getCreatedAt())
            .build();

        return ResponseEntity.ok(TenantDto.fromEntity(tenantRepository.save(updatedTenant)));
    }

    @Transactional
    public ResponseEntity<String> deleteTenant(Long id) {
        // 1. 조회 및 검증 (실패 시 BusinessException이 던져짐)
        Tenant existingTenant = tenantRepository.findById(id)
            .orElseThrow(() -> new BusinessException(TenantErrorCode.TENANT_NOT_FOUND));

        if (userRepository.existsByTenantId(existingTenant.getId())) {
            Map<String, Object> params = Map.of("name", existingTenant.getName());
            throw new BusinessException(TenantErrorCode.ALREADY_EXISTS_TENANT, params);
        }

        // 2. 비즈니스 로직 수행
        tenantRepository.deleteById(existingTenant.getId());

        // 3. 성공 응답
        return ResponseEntity.ok().build();
    }
}
