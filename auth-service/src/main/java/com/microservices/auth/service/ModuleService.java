package com.microservices.auth.service;

import com.common.exceptions.BusinessException;
import com.common.exceptions.code.ModuleErrorCode;
import com.microservices.auth.domain.entity.Module;
import com.microservices.auth.dto.ModuleDto;
import com.microservices.auth.repository.ModuleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModuleService {

  private final ModuleRepository moduleRepository;

  @Transactional(readOnly = true)
  public List<ModuleDto> getModuleList() {
    return moduleRepository.findAll().stream().map(ModuleDto::fromEntity).toList();
  }

  @Transactional(readOnly = true)
  public ModuleDto getModuleById(Long id) {
    return ModuleDto.fromEntity(findModule(id));
  }

  @Transactional
  public ModuleDto createModule(ModuleDto moduleDto) {
    validateModuleUniqueness(moduleDto.getName(), null);

    Module module =
        Module.builder()
            .name(moduleDto.getName())
            .url(moduleDto.getUrl())
            .description(moduleDto.getDescription())
            .build();

    return ModuleDto.fromEntity(moduleRepository.save(module));
  }

  @Transactional
  public ModuleDto updateModule(Long id, ModuleDto moduleDto) {
    Module existingModule = findModule(id);
    validateModuleUniqueness(moduleDto.getName(), existingModule.getId());

    Module updatedModule =
        Module.builder()
            .id(existingModule.getId())
            .name(moduleDto.getName())
            .url(moduleDto.getUrl())
            .description(moduleDto.getDescription())
            .build();

    return ModuleDto.fromEntity(moduleRepository.save(updatedModule));
  }

  @Transactional
  public void deleteModule(Long id) {
    Module module = findModule(id);
    moduleRepository.delete(module);
  }

  private Module findModule(Long id) {
    return moduleRepository
        .findById(id)
        .orElseThrow(() -> new BusinessException(ModuleErrorCode.MODULE_NOT_FOUND));
  }

  private void validateModuleUniqueness(String name, Long excludeModuleId) {
    moduleRepository
        .findByName(name)
        .filter(module -> excludeModuleId == null || !module.getId().equals(excludeModuleId))
        .ifPresent(
            module -> {
              throw new BusinessException(ModuleErrorCode.ALREADY_EXISTS_MODULE);
            });
  }
}
