package com.microservices.auth.controller;

import com.microservices.auth.dto.ModuleDto;
import com.microservices.auth.service.ModuleService;
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
@RequestMapping("/module")
@RequiredArgsConstructor
@Tag(name = "Module API", description = "모듈 API 문서")
public class ModuleController {

  private final ModuleService moduleService;

  @GetMapping("/list")
  public List<ModuleDto> getModuleList() {
    return moduleService.getModuleList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<ModuleDto> getModule(@PathVariable("id") Long id) {
    return ResponseEntity.ok(moduleService.getModuleById(id));
  }

  @PostMapping("/create")
  public ResponseEntity<ModuleDto> createModule(@RequestBody ModuleDto moduleDto) {
    return ResponseEntity.status(201).body(moduleService.createModule(moduleDto));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ModuleDto> updateModule(
      @PathVariable("id") Long id, @RequestBody ModuleDto moduleDto) {
    return ResponseEntity.ok(moduleService.updateModule(id, moduleDto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteModule(@PathVariable("id") Long id) {
    moduleService.deleteModule(id);
    return ResponseEntity.noContent().build();
  }
}
