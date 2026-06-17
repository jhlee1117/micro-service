INSERT INTO tenant (id, name, status, created_at, updated_at)
VALUES (1, 'Test', TRUE, NOW(), NOW())
ON CONFLICT (name) DO UPDATE
SET status = EXCLUDED.status,
    updated_at = EXCLUDED.updated_at;

INSERT INTO roles (id, name, description, is_system_role)
VALUES
(1, 'ROLE_SUPER_ADMIN', 'Super administrator', TRUE),
(2, 'ROLE_ADMIN', 'Administrator', TRUE),
(3, 'ROLE_USER', 'User', FALSE),
(4, 'ROLE_VIEWER', 'Viewer', FALSE)
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description,
    is_system_role = EXCLUDED.is_system_role;

INSERT INTO appuser (id, username, email, password, name, tenant_id, auth_type, signup_completed, is_active)
VALUES
(1, 'admin', 'admin@example.com', '$2a$10$5.ligZ5PF86vk.ylJ.g21Oz75MKq./fIfxLbGRPkr7Ux6W1oxoivO', 'Admin', 1, 'LOCAL', TRUE, TRUE),
(2, 'testuser', 'testuser@example.com', '$2a$10$ckBSkTA3sZ.8Zk7JU0nY8ePwkSUtuk/s0S7ZmB9tzzPSgApDEOemK', 'Test User', NULL, 'LOCAL', TRUE, TRUE)
ON CONFLICT (username) DO UPDATE
SET email = EXCLUDED.email,
    password = EXCLUDED.password,
    name = EXCLUDED.name,
    tenant_id = EXCLUDED.tenant_id,
    auth_type = EXCLUDED.auth_type,
    signup_completed = EXCLUDED.signup_completed,
    is_active = EXCLUDED.is_active;

INSERT INTO appuser_roles (user_id, role_id, granted_by)
VALUES (1, 1, NULL)
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO modules (id, name, url, description)
VALUES
(1, 'System Management', '/system/management', 'System management features'),
(2, 'HR Management', '/hr/management', 'HR management features')
ON CONFLICT (name) DO UPDATE
SET url = EXCLUDED.url,
    description = EXCLUDED.description;

INSERT INTO tenant_modules (tenant_id, module_id, plan_type)
VALUES (1, 1, 'free')
ON CONFLICT (tenant_id, module_id) DO UPDATE
SET plan_type = EXCLUDED.plan_type;

INSERT INTO user_modules (user_id, module_id)
VALUES
(1, 1),
(1, 2)
ON CONFLICT (user_id, module_id) DO NOTHING;

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
('S1', NULL, 1, NULL, NULL, 'SystemManagement', 'System management', 'menu.system.systemManagement', 1, TRUE, 'system'),
('S11', 'S1', 1, '/system/management/user', '/api/system/management/user', 'UserManagement', 'User management', 'menu.system.userManagement', 1, TRUE, 'user'),
('S12', 'S1', 1, '/system/management/tenant', '/api/system/management/tenant', 'TenantManagement', 'Tenant management', 'menu.system.tenantManagement', 2, TRUE, 'company')
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
