package com.microservices.auth_service.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.microservices.auth_service.dto.TenantDto;
import com.microservices.auth_service.repository.TenantRepository;

@Service
public class TenantService {

    @Autowired
    TenantRepository tenantRepository;

    public List<TenantDto> getTenantList() {
        return tenantRepository.findAll().
            stream().map(tenant -> new TenantDto(tenant.getId(), tenant.getName(), tenant.getStatus())).toList();
    }
}
