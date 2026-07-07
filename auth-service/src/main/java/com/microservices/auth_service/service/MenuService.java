package com.microservices.auth_service.service;

import com.microservices.auth_service.domain.entity.QMenu;
import com.microservices.auth_service.domain.entity.QMenuPermission;
import com.microservices.auth_service.domain.entity.QPermission;
import com.microservices.auth_service.domain.entity.QRolePermission;
import com.microservices.auth_service.domain.entity.QTenantModule;
import com.microservices.auth_service.domain.entity.QUser;
import com.microservices.auth_service.domain.entity.QUserRole;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.microservices.auth_service.dto.MenuDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final JPAQueryFactory jpaQueryFactory;

    @Cacheable(value = "user-menus", key = "#userId")
    public List<MenuDto> getMenuListByUserInfo(Long userId) {
        QUser qUser = QUser.user;
        QUserRole qUserRole = QUserRole.userRole;
        QRolePermission qRolePermission = QRolePermission.rolePermission;
        QPermission qPermission = QPermission.permission;
        QMenuPermission qMenuPermission = QMenuPermission.menuPermission;
        QTenantModule qTenantModule = QTenantModule.tenantModule;
        QMenu qMenu = QMenu.menu;

        return jpaQueryFactory.select(Projections.constructor(MenuDto.class,
        qMenu.menuCode,
        qMenu.parentMenu.menuCode,
        qMenu.module.id,
        qMenu.path,
        qMenu.apiPath,
        qMenu.component,
        qMenu.description,
        qMenu.menuAlias,
        qMenu.displayOrder,
        qMenu.isActive,
        qMenu.icon))
        .from(qUser)
        .join(qUser.userRoles, qUserRole)
        .join(qRolePermission).on(qRolePermission.role.eq(qUserRole.role))
        .join(qRolePermission.permission, qPermission)
        .join(qMenuPermission).on(qMenuPermission.permission.eq(qPermission))
        .join(qMenuPermission.menu, qMenu)
        .join(qTenantModule).on(
            qTenantModule.tenant.eq(qUser.tenant),
            qTenantModule.module.eq(qMenu.module)
        )
        .where(
            qUser.id.eq(userId),
            qUser.enabled.isTrue(),
            qTenantModule.enabled.isTrue(),
            qMenu.isActive.isTrue(),
            qPermission.module.eq(qMenu.module)
        )
        .distinct()
        .fetch();
    }
}
