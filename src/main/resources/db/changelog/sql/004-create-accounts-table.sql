--liquibase formatted sql

--changeset ksimokaitis:004
CREATE TABLE IF NOT EXISTS account (
    id BIGSERIAL PRIMARY KEY,
    iban VARCHAR(34) NOT NULL UNIQUE,
    owner_name VARCHAR(150) NOT NULL,
    balance NUMERIC(19, 2) NOT NULL,
    currency CHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_account_balance_non_negative CHECK (balance >= 0)
);
