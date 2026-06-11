INSERT INTO tenant (name, status, created_at, updated_at)
VALUES ('Test', TRUE, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;
