INSERT INTO modules (name, url, description)
VALUES
('System Management', '/system/management', '전체 시스템 관리 기능'),
('HR Management', '/hr/management', '인사 관리 기능');

INSERT INTO tenant_modules (tenant_id, module_id, plan_type) VALUES
(1, 1, 'free');

INSERT INTO user_modules (user_id, module_id) VALUES
(1, 1),
(1, 2);