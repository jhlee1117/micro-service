INSERT INTO appuser (username, email, password, name, tenant_id) 
VALUES ('admin', 'admin@example.com', '$2a$10$5.ligZ5PF86vk.ylJ.g21Oz75MKq./fIfxLbGRPkr7Ux6W1oxoivO', 'Admin', 1);

INSERT INTO appuser (username, email, password, name) 
VALUES ('testuser', 'testuser@example.com', '$2a$10$ckBSkTA3sZ.8Zk7JU0nY8ePwkSUtuk/s0S7ZmB9tzzPSgApDEOemK', 'Test User');

INSERT INTO appuser_roles (user_id, role_id) VALUES (1, 1);