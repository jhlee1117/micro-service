package com.microservices.auth_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuDto {
    private String name;
    private String url;
    private String description;

    public MenuDto(String name, String url, String description) {
        this.name = name;
        this.url = url;
        this.description = description;
    }
}
