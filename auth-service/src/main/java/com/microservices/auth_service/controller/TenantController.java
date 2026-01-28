package com.microservices.auth_service.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.microservices.auth_service.dto.TenantDto;
import com.microservices.auth_service.service.TenantService;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/tenant")
@Tag(name = "Tenant API", description = "????????? API ????")
public class TenantController {

    @Autowired
    TenantService tenantService;

    @GetMapping("/list")
    public List<TenantDto> getTenantList() {
        return tenantService.getTenantList();
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createTenant(@RequestBody TenantDto tenantDto) {
        return tenantService.createTenant(tenantDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateTenant(@PathVariable("id") Long id, @RequestBody TenantDto tenantDto) {
        return tenantService.updateTenant(id, tenantDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTenant(@PathVariable("id") Long id) {
        return tenantService.deleteTenant(id);
    }

}
