package com.microservices.auth_service.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.microservices.auth_service.dto.RoleDto;
import com.microservices.auth_service.repository.RoleRepository;

@Service
public class RoleService {

    @Autowired
    RoleRepository roleRepository;

    // @Cacheable(value = "roles")
    public List<RoleDto> getRoleList() {
        return roleRepository.findAll().
            stream().map(role -> new RoleDto(role.getId(), role.getName(), role.getDescription(), role.isSystemRole())).toList();
    }
}
