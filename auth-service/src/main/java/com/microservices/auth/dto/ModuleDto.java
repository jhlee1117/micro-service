package com.microservices.auth.dto;

import com.microservices.auth.domain.entity.Module;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDto {

  private Long id;
  private String name;
  private String url;
  private String description;

  public static ModuleDto fromEntity(Module module) {
    if (module == null) {
      return null;
    }
    return new ModuleDto(
        module.getId(), module.getName(), module.getUrl(), module.getDescription());
  }
}
