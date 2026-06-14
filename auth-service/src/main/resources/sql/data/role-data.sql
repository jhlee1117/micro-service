MERGE INTO roles (id, name, description, is_system_role) KEY(name)
VALUES
(1, 'ROLE_SUPER_ADMIN', 'Super administrator', TRUE),
(2, 'ROLE_ADMIN', 'Administrator', TRUE),
(3, 'ROLE_USER', 'User', FALSE),
(4, 'ROLE_VIEWER', 'Viewer', FALSE);
