--liquibase formatted sql
--changeset ksimokaitis:005
--validCheckSum: 9:1762679bf906f8ce01221017d80c6c33
--validCheckSum: 9:48d75e2d8fa53a0732ddd6f946870f9b

CREATE TABLE IF NOT EXISTS currency_exchange (
    id BIGSERIAL PRIMARY KEY,
    source_account_id BIGINT NOT NULL REFERENCES account(id),
    target_account_id BIGINT NOT NULL REFERENCES account(id),
    source_amount NUMERIC(19, 2) NOT NULL CHECK (source_amount > 0),
    source_currency CHAR(3) NOT NULL,
    target_amount NUMERIC(19, 2) NOT NULL CHECK (target_amount > 0),
    target_currency CHAR(3) NOT NULL,
    exchange_rate NUMERIC(19, 8) NOT NULL CHECK (exchange_rate > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
