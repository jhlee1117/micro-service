package com.microservices.auth.controller;

import com.microservices.auth.dto.PermissionAssignmentRequest;
import com.microservices.auth.dto.RoleDto;
import com.microservices.auth.dto.RolePermissionAssignmentDto;
import com.microservices.auth.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/role", "/roles"})
@Tag(name = "Role API", description = "역할 API 문서")
public class RoleController {

  @Autowired RoleService roleService;

  @GetMapping("/list")
  public List<RoleDto> getRoleList() {
    return roleService.getRoleList();
  }

  @GetMapping("/{roleId}")
  public ResponseEntity<RoleDto> getRole(@PathVariable("roleId") Long roleId) {
    return ResponseEntity.ok(roleService.getRoleById(roleId));
  }

  @PostMapping("/create")
  public ResponseEntity<RoleDto> createRole(@RequestBody RoleDto roleDto) {
    return ResponseEntity.status(201).body(roleService.createRole(roleDto));
  }

  @PutMapping("/{roleId}")
  public ResponseEntity<RoleDto> updateRole(
      @PathVariable("roleId") Long roleId, @RequestBody RoleDto roleDto) {
    return ResponseEntity.ok(roleService.updateRole(roleId, roleDto));
  }

  @DeleteMapping("/{roleId}")
  public ResponseEntity<Void> deleteRole(
      @PathVariable("roleId") Long roleId, Authentication authentication) {
    String username = authentication.getName();
    roleService.deleteRole(roleId, username);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{roleId}/permissions")
  public ResponseEntity<RolePermissionAssignmentDto> getRolePermissions(
      @PathVariable("roleId") Long roleId) {
    return ResponseEntity.ok(roleService.getRolePermissions(roleId));
  }

  @PutMapping("/{roleId}/permissions")
  public ResponseEntity<RolePermissionAssignmentDto> updateRolePermissions(
      @PathVariable("roleId") Long roleId, @RequestBody PermissionAssignmentRequest request) {
    return ResponseEntity.ok(roleService.updateRolePermissions(roleId, request));
  }
}
