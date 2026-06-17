CREATE TABLE IF NOT EXISTS modules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    url VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS menus (
    menu_code VARCHAR(10) PRIMARY KEY,
    parent_menu_code VARCHAR(10) NULL,
    module_id BIGINT NOT NULL,
    path VARCHAR(100),
    api_path VARCHAR(200),
    component VARCHAR(100),
    description VARCHAR(255),
    menu_alias VARCHAR(100),
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    icon VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    FOREIGN KEY (parent_menu_code) REFERENCES menus(menu_code),
    FOREIGN KEY (module_id) REFERENCES modules(id)
);

CREATE TABLE IF NOT EXISTS tenant_modules (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    plan_type VARCHAR(50),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    UNIQUE (tenant_id, module_id),
    FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_modules (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    role VARCHAR(50),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    UNIQUE (user_id, module_id),
    FOREIGN KEY (user_id) REFERENCES appuser(id) ON DELETE CASCADE,
    FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE
);
