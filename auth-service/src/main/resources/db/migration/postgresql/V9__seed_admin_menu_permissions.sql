INSERT INTO permissions (module_id, code, resource, action, description)
VALUES (1, 'SYSTEM_MANAGEMENT_MENU_VIEW', 'SYSTEM_MANAGEMENT', 'MENU_VIEW', 'View system management menus')
ON CONFLICT (code) DO UPDATE
SET module_id = EXCLUDED.module_id,
    resource = EXCLUDED.resource,
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
WHERE m.menu_code IN ('S1', 'S11', 'S12')
ON CONFLICT (menu_code, permission_id) DO NOTHING;