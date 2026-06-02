--liquibase formatted sql

--changeset ksimokaitis:003
ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255) NOT NULL DEFAULT '{noop}changeme',
    ADD COLUMN IF NOT EXISTS role VARCHAR(50) NOT NULL DEFAULT 'USER',
    ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE;

INSERT INTO app_user (username, email, password_hash, role, enabled)
SELECT 'admin', 'admin@bank.local', '{noop}admin123', 'USER', TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM app_user
    WHERE username = 'admin'
);
