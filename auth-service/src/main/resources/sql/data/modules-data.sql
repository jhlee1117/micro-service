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
