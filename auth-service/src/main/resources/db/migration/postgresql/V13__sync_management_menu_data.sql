INSERT INTO menus (
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
VALUES
('S13', 'S1', 1, '/system/management/roles', '/api/system/management/roles', 'RoleManagement', 'Role management', 'menu.system.roleManagement', 3, TRUE, 'role'),
('S14', 'S1', 1, '/system/management/module', '/api/system/management/module', 'ModuleManagement', 'Module management', 'menu.system.moduleManagement', 4, TRUE, 'module'),
('S15', 'S1', 1, '/system/management/menu', '/api/system/management/menu', 'MenuManagement', 'Menu management', 'menu.system.menuManagement', 5, TRUE, 'menu'),
('S16', 'S1', 1, '/system/management/permission', '/api/system/management/permission', 'PermissionManagement', 'Permission management', 'menu.system.permissionManagement', 6, TRUE, 'permission')
ON CONFLICT (menu_code) DO UPDATE
SET parent_menu_code = EXCLUDED.parent_menu_code,
    module_id = EXCLUDED.module_id,
    path = EXCLUDED.path,
    api_path = EXCLUDED.api_path,
    component = EXCLUDED.component,
    description = EXCLUDED.description,
    menu_alias = EXCLUDED.menu_alias,
    display_order = EXCLUDED.display_order,
    is_active = EXCLUDED.is_active,
    icon = EXCLUDED.icon;

INSERT INTO permissions (code, resource, action, description)
VALUES ('SYSTEM_MANAGEMENT_MENU_VIEW', 'SYSTEM_MANAGEMENT', 'MENU_VIEW', 'View system management menus')
ON CONFLICT (code) DO UPDATE
SET resource = EXCLUDED.resource,
    action = EXCLUDED.action,
    description = EXCLUDED.description;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'SYSTEM_MANAGEMENT_MENU_VIEW'
WHERE r.name IN ('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO menu_permissions (menu_code, permission_id)
SELECT m.menu_code, p.id
FROM menus m
JOIN permissions p ON p.code = 'SYSTEM_MANAGEMENT_MENU_VIEW'
WHERE m.menu_code IN ('S1', 'S11', 'S12', 'S13', 'S14', 'S15', 'S16')
ON CONFLICT (menu_code, permission_id) DO NOTHING;