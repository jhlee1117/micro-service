package com.microservices.auth_service.service;

import org.springframework.stereotype.Service;

import com.microservices.auth_service.domain.entity.QMenu;
import com.microservices.auth_service.domain.entity.QModule;
import com.microservices.auth_service.domain.entity.QUserModule;
import com.microservices.auth_service.dto.MenuDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final JPAQueryFactory jpaQueryFactory;

    public List<MenuDto> getMenuListByUserInfo(Long userId) {
        QUserModule qUserModule = QUserModule.userModule;
        QModule qModule = QModule.module;
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
        .from(qUserModule)
        .join(qUserModule.module, qModule)
        .join(qModule.menus, qMenu)
        .where(qUserModule.user.id.eq(userId), qMenu.isActive.eq(true))
        .distinct()
        .fetch();
    }
}
