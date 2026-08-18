package com.microservices.auth.dto;

import com.microservices.auth.domain.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDto {

  private Long id;
  private String code;
  private String resource;
  private String action;
  private String description;

  public static PermissionDto fromEntity(Permission permission) {
    if (permission == null) {
      return null;
    }
    return new PermissionDto(
        permission.getId(),
        permission.getCode(),
        permission.getResource(),
        permission.getAction(),
        permission.getDescription());
  }
}
