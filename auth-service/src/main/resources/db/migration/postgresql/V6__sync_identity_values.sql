SELECT setval(pg_get_serial_sequence('tenant', 'id'), COALESCE((SELECT MAX(id) FROM tenant), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('appuser', 'id'), COALESCE((SELECT MAX(id) FROM appuser), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('oauth_account', 'id'), COALESCE((SELECT MAX(id) FROM oauth_account), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('roles', 'id'), COALESCE((SELECT MAX(id) FROM roles), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('appuser_roles', 'id'), COALESCE((SELECT MAX(id) FROM appuser_roles), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('modules', 'id'), COALESCE((SELECT MAX(id) FROM modules), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('tenant_modules', 'id'), COALESCE((SELECT MAX(id) FROM tenant_modules), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('user_modules', 'id'), COALESCE((SELECT MAX(id) FROM user_modules), 0) + 1, false);
