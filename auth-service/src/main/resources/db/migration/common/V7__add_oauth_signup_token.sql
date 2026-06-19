ALTER TABLE appuser
    ADD COLUMN IF NOT EXISTS signup_token_hash VARCHAR(100);

ALTER TABLE appuser
    ADD COLUMN IF NOT EXISTS signup_token_expires_at TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS idx_appuser_signup_token_hash
    ON appuser (signup_token_hash);
