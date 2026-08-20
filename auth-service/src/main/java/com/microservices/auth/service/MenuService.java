package com.microservices.auth.service;

import com.common.exceptions.BusinessException;
import com.common.exceptions.code.MenuErrorCode;
import com.common.exceptions.code.ModuleErrorCode;
import com.common.exceptions.code.PermissionErrorCode;
import com.microservices.auth.domain.entity.Menu;
import com.microservices.auth.domain.entity.MenuPermission;
import com.microservices.auth.domain.entity.Module;
import com.microservices.auth.domain.entity.Permission;
import com.microservices.auth.domain.entity.QMenu;
import com.microservices.auth.domain.entity.QMenuPermission;
import com.microservices.auth.domain.entity.QPermission;
import com.microservices.auth.domain.entity.QRolePermission;
import com.microservices.auth.domain.entity.QTenantModule;
import com.microservices.auth.domain.entity.QUser;
import com.microservices.auth.domain.entity.QUserRole;
import com.microservices.auth.dto.MenuDto;
import com.microservices.auth.dto.MenuPermissionAssignmentDto;
import com.microservices.auth.dto.PermissionAssignmentRequest;
import com.microservices.auth.repository.MenuPermissionRepository;
import com.microservices.auth.repository.MenuRepository;
import com.microservices.auth.repository.ModuleRepository;
import com.microservices.auth.repository.PermissionRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuService {

  private final JPAQueryFactory jpaQueryFactory;
  private final MenuRepository menuRepository;
  private final ModuleRepository moduleRepository;
  private final PermissionRepository permissionRepository;
  private final MenuPermissionRepository menuPermissionRepository;

  // @Cacheable(value = "user-menus", key = "#userId")
  @Transactional(readOnly = true)
  public List<MenuDto> getMenuListByUserInfo(Long userId) {
    QUser qUser = QUser.user;
    QUserRole qUserRole = QUserRole.userRole;
    QRolePermission qRolePermission = QRolePermission.rolePermission;
    QPermission qPermission = QPermission.permission;
    QMenuPermission qMenuPermission = QMenuPermission.menuPermission;
    QTenantModule qTenantModule = QTenantModule.tenantModule;
    QMenu qMenu = QMenu.menu;

    return jpaQueryFactory
        .select(
            Projections.constructor(
                MenuDto.class,
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
        .join(qRolePermission)
        .on(qRolePermission.role.eq(qUserRole.role))
        .join(qRolePermission.permission, qPermission)
        .join(qMenuPermission)
        .on(qMenuPermission.permission.eq(qPermission))
        .join(qMenuPermission.menu, qMenu)
        .join(qTenantModule)
        .on(qTenantModule.tenant.eq(qUser.tenant), qTenantModule.module.eq(qMenu.module))
        .where(
            qUser.id.eq(userId),
            qUser.enabled.isTrue(),
            qTenantModule.enabled.isTrue(),
            qMenu.isActive.isTrue())
        .distinct()
        .fetch();
  }

  @Transactional(readOnly = true)
  public List<MenuDto> getMenuList() {
    return menuRepository.findAll().stream().map(MenuDto::fromEntity).toList();
  }

  @Transactional(readOnly = true)
  public MenuDto getMenuByCode(String menuCode) {
    return MenuDto.fromEntity(findMenu(menuCode));
  }

  @Transactional
  public MenuDto createMenu(MenuDto menuDto) {
    if (menuRepository.existsById(menuDto.getMenuCode())) {
      throw new BusinessException(MenuErrorCode.ALREADY_EXISTS_MENU);
    }

    Menu menu = buildMenu(menuDto, menuDto.getMenuCode());
    return MenuDto.fromEntity(menuRepository.save(menu));
  }

  @Transactional
  public MenuDto updateMenu(String menuCode, MenuDto menuDto) {
    findMenu(menuCode);
    Menu menu = buildMenu(menuDto, menuCode);
    return MenuDto.fromEntity(menuRepository.save(menu));
  }

  @Transactional
  public void deleteMenu(String menuCode) {
    Menu menu = findMenu(menuCode);
    menuRepository.delete(menu);
  }

  @Transactional(readOnly = true)
  public MenuPermissionAssignmentDto getMenuPermissions(String menuCode) {
    Menu menu = findMenu(menuCode);
    List<Long> permissionIds =
        menuPermissionRepository.findAllByMenuOrderByPermissionId(menu).stream()
            .map(menuPermission -> menuPermission.getPermission().getId())
            .toList();

    return new MenuPermissionAssignmentDto(menu.getMenuCode(), permissionIds);
  }

  @Transactional
  public MenuPermissionAssignmentDto updateMenuPermissions(
      String menuCode, PermissionAssignmentRequest request) {
    Menu menu = findMenu(menuCode);
    List<Permission> permissions = findPermissions(request.permissionIds());

    menuPermissionRepository.deleteByMenu(menu);
    menuPermissionRepository.flush();

    List<MenuPermission> menuPermissions =
        permissions.stream().map(permission -> createMenuPermission(menu, permission)).toList();
    menuPermissionRepository.saveAll(menuPermissions);

    return new MenuPermissionAssignmentDto(
        menu.getMenuCode(), permissions.stream().map(Permission::getId).toList());
  }

  private Menu buildMenu(MenuDto menuDto, String menuCode) {
    Module module =
        moduleRepository
            .findById(menuDto.getModuleId())
            .orElseThrow(() -> new BusinessException(ModuleErrorCode.MODULE_NOT_FOUND));

    Menu parentMenu = null;
    if (menuDto.getParentMenuCode() != null && !menuDto.getParentMenuCode().isBlank()) {
      parentMenu = findMenu(menuDto.getParentMenuCode());
    }

    return Menu.builder()
        .menuCode(menuCode)
        .parentMenu(parentMenu)
        .module(module)
        .path(menuDto.getPath())
        .apiPath(menuDto.getApiPath())
        .component(menuDto.getComponent())
        .description(menuDto.getDescription())
        .menuAlias(menuDto.getMenuAlias())
        .displayOrder(menuDto.getDisplayOrder() != null ? menuDto.getDisplayOrder() : 0)
        .isActive(menuDto.getIsActive() != null ? menuDto.getIsActive() : Boolean.TRUE)
        .icon(menuDto.getIcon())
        .build();
  }

  private Menu findMenu(String menuCode) {
    return menuRepository
        .findById(menuCode)
        .orElseThrow(() -> new BusinessException(MenuErrorCode.MENU_NOT_FOUND));
  }

  private MenuPermission createMenuPermission(Menu menu, Permission permission) {
    MenuPermission menuPermission = new MenuPermission();
    menuPermission.setMenu(menu);
    menuPermission.setPermission(permission);
    return menuPermission;
  }

  private List<Permission> findPermissions(List<Long> permissionIds) {
    Set<Long> uniquePermissionIds =
        permissionIds == null ? Set.of() : new LinkedHashSet<>(permissionIds);
    List<Permission> permissions = permissionRepository.findAllById(uniquePermissionIds);

    if (permissions.size() != uniquePermissionIds.size()) {
      throw new BusinessException(PermissionErrorCode.PERMISSION_NOT_FOUND);
    }

    return permissions;
  }
}
