MERGE INTO tenant (id, name, status, created_at, updated_at) KEY(name)
VALUES (1, 'Test', TRUE, NOW(), NOW());
