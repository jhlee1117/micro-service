package com.microservices.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
    
    private Long id;
    private String name;
    private String description;
    private boolean isSystemRole;
    
    // Role 엔티티에서 RoleDto로 변환하는 정적 메서드
    public static RoleDto fromEntity(com.microservices.auth_service.domain.entity.Role role) {
        if (role == null) {
            return null;
        }
        return new RoleDto(
            role.getId(),
            role.getName(),
            role.getDescription(),
            role.isSystemRole()
        );
    }
}
