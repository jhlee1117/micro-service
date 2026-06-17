CREATE TABLE IF NOT EXISTS appuser (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) UNIQUE,
    password VARCHAR(1000),
    name VARCHAR(100),
    auth_type VARCHAR(20) DEFAULT 'LOCAL',
    signup_completed BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    FOREIGN KEY (tenant_id) REFERENCES tenant(id)
);

CREATE TABLE IF NOT EXISTS oauth_account (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(100),
    provider_name VARCHAR(100),
    profile_image_url VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT fk_oauth_account_user FOREIGN KEY (user_id) REFERENCES appuser(id),
    CONSTRAINT uk_oauth_provider_user UNIQUE (provider, provider_user_id)
);
