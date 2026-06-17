MERGE INTO tenant (id, name, status, created_at, updated_at) KEY(name)
VALUES (1, 'Test', TRUE, NOW(), NOW());

MERGE INTO roles (id, name, description, is_system_role) KEY(name)
VALUES
(1, 'ROLE_SUPER_ADMIN', 'Super administrator', TRUE),
(2, 'ROLE_ADMIN', 'Administrator', TRUE),
(3, 'ROLE_USER', 'User', FALSE),
(4, 'ROLE_VIEWER', 'Viewer', FALSE);

MERGE INTO appuser (id, username, email, password, name, tenant_id, auth_type, signup_completed, is_active) KEY(username)
VALUES
(1, 'admin', 'admin@example.com', '$2a$10$5.ligZ5PF86vk.ylJ.g21Oz75MKq./fIfxLbGRPkr7Ux6W1oxoivO', 'Admin', 1, 'LOCAL', TRUE, TRUE),
(2, 'testuser', 'testuser@example.com', '$2a$10$ckBSkTA3sZ.8Zk7JU0nY8ePwkSUtuk/s0S7ZmB9tzzPSgApDEOemK', 'Test User', NULL, 'LOCAL', TRUE, TRUE);

MERGE INTO appuser_roles (user_id, role_id, granted_by) KEY(user_id, role_id)
VALUES (1, 1, NULL);

MERGE INTO modules (id, name, url, description) KEY(name)
VALUES
(1, 'System Management', '/system/management', 'System management features'),
(2, 'HR Management', '/hr/management', 'HR management features');

MERGE INTO tenant_modules (tenant_id, module_id, plan_type) KEY(tenant_id, module_id)
VALUES (1, 1, 'free');

MERGE INTO user_modules (user_id, module_id) KEY(user_id, module_id)
VALUES
(1, 1),
(1, 2);

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
