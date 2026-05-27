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
    public ResponseEntity<TenantDto> createTenant(@RequestBody TenantDto tenantDto) {
        TenantDto created = tenantService.createTenant(tenantDto);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantDto> updateTenant(@PathVariable("id") Long id, @RequestBody TenantDto tenantDto) {
        TenantDto updated = tenantService.updateTenant(id, tenantDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable("id") Long id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }

}
