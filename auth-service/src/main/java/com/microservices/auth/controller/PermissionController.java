package com.microservices.auth.controller;

import com.microservices.auth.dto.PermissionDto;
import com.microservices.auth.service.PermissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/permission")
@RequiredArgsConstructor
@Tag(name = "Permission API", description = "권한 API 문서")
public class PermissionController {

  private final PermissionService permissionService;

  @GetMapping("/list")
  public List<PermissionDto> getPermissionList() {
    return permissionService.getPermissionList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<PermissionDto> getPermission(@PathVariable("id") Long id) {
    return ResponseEntity.ok(permissionService.getPermissionById(id));
  }

  @PostMapping("/create")
  public ResponseEntity<PermissionDto> createPermission(@RequestBody PermissionDto permissionDto) {
    return ResponseEntity.status(201).body(permissionService.createPermission(permissionDto));
  }

  @PutMapping("/{id}")
  public ResponseEntity<PermissionDto> updatePermission(
      @PathVariable("id") Long id, @RequestBody PermissionDto permissionDto) {
    return ResponseEntity.ok(permissionService.updatePermission(id, permissionDto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePermission(@PathVariable("id") Long id) {
    permissionService.deletePermission(id);
    return ResponseEntity.noContent().build();
  }
}
