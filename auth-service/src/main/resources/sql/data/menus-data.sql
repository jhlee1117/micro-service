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
('S1', NULL, 1, NULL, NULL, 'SystemManagement', 'System management', 'menu.system.systemManagement', 1, TRUE, 'system'),
('S11', 'S1', 1, '/system/management/user', '/api/system/management/user', 'UserManagement', 'User management', 'menu.system.userManagement', 1, TRUE, 'user'),
('S12', 'S1', 1, '/system/management/tenant', '/api/system/management/tenant', 'TenantManagement', 'Tenant management', 'menu.system.tenantManagement', 2, TRUE, 'company');
