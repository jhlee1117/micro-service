INSERT INTO modules (name, url, description)
VALUES
('System Management', '/system/management', '전체 시스템 관리 기능'),
('HR Management', '/hr/management', '인사 관리 기능')
ON CONFLICT (name) DO NOTHING;

INSERT INTO tenant_modules (tenant_id, module_id, plan_type) VALUES
(1, 1, 'free')
ON CONFLICT (tenant_id, module_id) DO NOTHING;

INSERT INTO user_modules (user_id, module_id) VALUES
(1, 1),
(1, 2)
ON CONFLICT (user_id, module_id) DO NOTHING;
