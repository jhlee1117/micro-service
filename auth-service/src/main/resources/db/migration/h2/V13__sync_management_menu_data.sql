MERGE INTO menus (
    menu_code,
    parent_menu_code,
    module_id,
    path,
    api_path,
    component,
    description,
    menu_alias,
    display_order,
    is_active,
    icon
)
KEY(menu_code)
VALUES
('S13', 'S1', 1, '/system/management/roles', '/api/system/management/roles', 'RoleManagement', 'Role management', 'menu.system.roleManagement', 3, TRUE, 'role'),
('S14', 'S1', 1, '/system/management/module', '/api/system/management/module', 'ModuleManagement', 'Module management', 'menu.system.moduleManagement', 4, TRUE, 'module'),
('S15', 'S1', 1, '/system/management/menu', '/api/system/management/menu', 'MenuManagement', 'Menu management', 'menu.system.menuManagement', 5, TRUE, 'menu'),
('S16', 'S1', 1, '/system/management/permission', '/api/system/management/permission', 'PermissionManagement', 'Permission management', 'menu.system.permissionManagement', 6, TRUE, 'permission');

MERGE INTO permissions (code, resource, action, description) KEY(code)
VALUES ('SYSTEM_MANAGEMENT_MENU_VIEW', 'SYSTEM_MANAGEMENT', 'MENU_VIEW', 'View system management menus');

MERGE INTO role_permissions (role_id, permission_id) KEY(role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'SYSTEM_MANAGEMENT_MENU_VIEW'
WHERE r.name IN ('ROLE_SUPER_ADMIN', 'ROLE_ADMIN');

MERGE INTO menu_permissions (menu_code, permission_id) KEY(menu_code, permission_id)
SELECT m.menu_code, p.id
FROM menus m
JOIN permissions p ON p.code = 'SYSTEM_MANAGEMENT_MENU_VIEW'
WHERE m.menu_code IN ('S1', 'S11', 'S12', 'S13', 'S14', 'S15', 'S16');