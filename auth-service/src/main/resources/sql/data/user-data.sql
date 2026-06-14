MERGE INTO appuser (id, username, email, password, name, tenant_id) KEY(username)
VALUES
(1, 'admin', 'admin@example.com', '$2a$10$5.ligZ5PF86vk.ylJ.g21Oz75MKq./fIfxLbGRPkr7Ux6W1oxoivO', 'Admin', 1),
(2, 'testuser', 'testuser@example.com', '$2a$10$ckBSkTA3sZ.8Zk7JU0nY8ePwkSUtuk/s0S7ZmB9tzzPSgApDEOemK', 'Test User', NULL);

MERGE INTO appuser_roles (user_id, role_id, granted_by) KEY(user_id, role_id)
VALUES (1, 1, NULL);
