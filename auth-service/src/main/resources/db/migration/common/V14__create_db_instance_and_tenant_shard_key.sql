CREATE TABLE IF NOT EXISTS db_instance (
    id BIGSERIAL PRIMARY KEY,
    shard_key VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    capacity_weight INT NOT NULL DEFAULT 100,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);

INSERT INTO db_instance (shard_key, status, capacity_weight)
VALUES ('shard-1', 'ACTIVE', 100);

ALTER TABLE tenant ADD COLUMN IF NOT EXISTS shard_key VARCHAR(50) NOT NULL DEFAULT 'shard-1';
ALTER TABLE tenant ADD CONSTRAINT fk_tenant_shard_key FOREIGN KEY (shard_key) REFERENCES db_instance (shard_key);
