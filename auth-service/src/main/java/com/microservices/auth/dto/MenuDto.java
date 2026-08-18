package com.microservices.auth.dto;

import com.microservices.auth.domain.entity.Menu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDto {

  private String menuCode;
  private String parentMenuCode;
  private Long moduleId;
  private String path;
  private String apiPath;
  private String component;
  private String description;
  private String menuAlias;
  private Integer displayOrder;
  private Boolean isActive;
  private String icon;

  public static MenuDto fromEntity(Menu menu) {
    if (menu == null) {
      return null;
    }
    return MenuDto.builder()
        .menuCode(menu.getMenuCode())
        .parentMenuCode(menu.getParentMenu() != null ? menu.getParentMenu().getMenuCode() : null)
        .moduleId(menu.getModule() != null ? menu.getModule().getId() : null)
        .path(menu.getPath())
        .apiPath(menu.getApiPath())
        .component(menu.getComponent())
        .description(menu.getDescription())
        .menuAlias(menu.getMenuAlias())
        .displayOrder(menu.getDisplayOrder())
        .isActive(menu.getIsActive())
        .icon(menu.getIcon())
        .build();
  }
}
