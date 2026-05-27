package com.microservices.auth_service.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String username;
    private String email;
    private String name;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // 쓰기 전용 (클라이언트에서 받지만 응답에는 포함하지 않음)
    private String password;
    
    private boolean isActive;
    private Set<RoleDto> roles;
    private TenantDto tenant;

}