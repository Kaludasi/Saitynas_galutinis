--liquibase formatted sql
--changeset ksimokaitis:007

ALTER TABLE account
    ADD COLUMN IF NOT EXISTS app_user_id BIGINT;

UPDATE account
SET app_user_id = (
    SELECT id
    FROM app_user
    WHERE username = 'admin'
)
WHERE app_user_id IS NULL;

ALTER TABLE account
    ALTER COLUMN app_user_id SET NOT NULL;

ALTER TABLE account
    ADD CONSTRAINT fk_account_app_user
        FOREIGN KEY (app_user_id) REFERENCES app_user(id);
