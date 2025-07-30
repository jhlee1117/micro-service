-- 역할 정의 (테넌트 구분 없이 통합)
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    is_system_role BOOLEAN DEFAULT FALSE, -- 시스템 기본 역할 여부
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 사용자-역할 매핑
CREATE TABLE appuser_roles (
    user_id BIGINT,
    role_id BIGINT,
    granted_by BIGINT, -- 권한을 부여한 사용자
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES appuser(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (granted_by) REFERENCES appuser(id)
);

-- 기본 역할 데이터
INSERT INTO roles (name, description, is_system_role) VALUES 
('ROLE_SUPER_ADMIN', '슈퍼 관리자', TRUE),
('ROLE_ADMIN', '관리자', TRUE),
('ROLE_USER', '일반 사용자', TRUE),
('ROLE_VIEWER', '조회 전용', TRUE); 

-- INSERT INTO appuser_roles (user_id, role_id, granted_by, created_at) VALUES
-- ('1')