package com.microservices.auth_service.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import com.microservices.auth_service.domain.entity.QRole;
import com.microservices.auth_service.domain.entity.QTenant;
import com.microservices.auth_service.domain.entity.QUser;
import com.microservices.auth_service.domain.entity.QUserRole;
import com.microservices.auth_service.domain.entity.User;
import com.microservices.auth_service.domain.entity.UserRole;
import com.microservices.auth_service.dto.UserDto;
import com.microservices.auth_service.dto.RoleDto;
import com.microservices.auth_service.dto.TenantDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<UserDto> getUserList() {
        QUser user = QUser.user;
        QTenant tenant = QTenant.tenant;
        QUserRole userRole = QUserRole.userRole;
        QRole role = QRole.role;

        // User 엔티티 전체를 조회 (연관 엔티티들과 함께)
        List<User> users = jpaQueryFactory.selectFrom(user)
                .leftJoin(user.tenant, tenant).fetchJoin()
                .leftJoin(user.userRoles, userRole).fetchJoin()
                .leftJoin(userRole.role, role).fetchJoin()
                .distinct()
                .fetch();

        List<UserDto> userDtoList = new ArrayList<>();

        for (User userEntity : users) {
            // User의 UserRole들에서 RoleDto들을 추출
            Set<RoleDto> roleDtos = new HashSet<>();
            if (userEntity.getUserRoles() != null) {
                for (UserRole ur : userEntity.getUserRoles()) {
                    if (ur.getRole() != null) {
                        RoleDto roleDto = RoleDto.fromEntity(ur.getRole());
                        if (roleDto != null) {
                            roleDtos.add(roleDto);
                        }
                    }
                }
            }

            // UserDto 생성
            TenantDto tenantDto = TenantDto.fromEntity(userEntity.getTenant());
            UserDto userDto = new UserDto(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                userEntity.getName(),
                null, // password는 응답에 포함하지 않음
                userEntity.isEnabled(),
                roleDtos, // UserRole에서 추출한 RoleDto들
                tenantDto // TenantDto 사용
            );

            userDtoList.add(userDto);
        }

        return userDtoList;
    }

}
