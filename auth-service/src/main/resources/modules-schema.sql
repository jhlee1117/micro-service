CREATE TABLE modules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- 메뉴 테이블
CREATE TABLE menus (
    id BIGSERIAL PRIMARY KEY,
    menu_code CHAR(20) NOT NULL UNIQUE,
    parent_menu_id BIGINT NULL,
    module_id BIGINT NOT NULL,
    path VARCHAR(100),
    api_path VARCHAR(200),
    component VARCHAR(100),
    description VARCHAR(255),
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    icon VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parent_menu_id) REFERENCES menus(id),
    FOREIGN KEY (module_id) REFERENCES modules(id)
);
-- 메뉴 다국어 지원 테이블
CREATE TABLE menu_translations (
    id BIGSERIAL PRIMARY KEY,
    menu_id BIGINT NOT NULL,
    lang_code CHAR(5) NOT NULL,   -- ex: 'en', 'ko', 'ja'
    name VARCHAR(100) NOT NULL,
    description TEXT,
    FOREIGN KEY (menu_id) REFERENCES menus(id),
    UNIQUE (menu_id, lang_code)
);
-- 테넌트가 어떤 모듈을 사용하는지
CREATE TABLE tenant_modules (
    tenant_id BIGINT,
    module_id BIGINT,
    plan_type VARCHAR(50), -- 예: free, standard, premium
    enabled BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (tenant_id, module_id),
    FOREIGN KEY (tenant_id) REFERENCES tenant(id),
    FOREIGN KEY (module_id) REFERENCES modules(id)
);
-- 사용자별 모듈 접근 권한
CREATE TABLE user_modules (
    user_id BIGINT,
    module_id BIGINT,
    role VARCHAR(50), -- 예: admin, user, viewer
    enabled BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (user_id, module_id),
    FOREIGN KEY (user_id) REFERENCES appuser(id),
    FOREIGN KEY (module_id) REFERENCES modules(id)
);