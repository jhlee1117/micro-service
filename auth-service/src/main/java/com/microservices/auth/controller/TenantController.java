package com.microservices.auth.controller;

import com.microservices.auth.dto.TenantDto;
import com.microservices.auth.dto.TenantModuleAssignmentsRequest;
import com.microservices.auth.dto.TenantModulesAssignmentDto;
import com.microservices.auth.service.TenantService;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/tenant")
@Tag(name = "Tenant API", description = "????????? API ????")
public class TenantController {

  @Autowired TenantService tenantService;

  @GetMapping("/list")
  public List<TenantDto> getTenantList() {
    return tenantService.getTenantList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<TenantDto> getTenant(@PathVariable("id") Long id) {
    return ResponseEntity.ok(tenantService.getTenantDtoById(id));
  }

  @PostMapping("/create")
  public ResponseEntity<TenantDto> createTenant(@RequestBody TenantDto tenantDto) {
    TenantDto created = tenantService.createTenant(tenantDto);
    return ResponseEntity.status(201).body(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<TenantDto> updateTenant(
      @PathVariable("id") Long id, @RequestBody TenantDto tenantDto) {
    TenantDto updated = tenantService.updateTenant(id, tenantDto);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTenant(@PathVariable("id") Long id) {
    tenantService.deleteTenant(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/modules")
  public ResponseEntity<TenantModulesAssignmentDto> getTenantModules(@PathVariable("id") Long id) {
    return ResponseEntity.ok(tenantService.getTenantModules(id));
  }

  @PutMapping("/{id}/modules")
  public ResponseEntity<TenantModulesAssignmentDto> updateTenantModules(
      @PathVariable("id") Long id, @RequestBody TenantModuleAssignmentsRequest request) {
    return ResponseEntity.ok(tenantService.updateTenantModules(id, request));
  }
}
