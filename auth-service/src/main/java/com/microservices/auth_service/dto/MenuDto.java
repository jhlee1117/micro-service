package com.microservices.auth_service.dto;

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

}