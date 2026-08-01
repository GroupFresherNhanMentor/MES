-- Migration: Create idempotency_keys table and unique index
DROP TABLE IF EXISTS idempotency_keys CASCADE;

CREATE TABLE idempotency_keys (
    id                 UUID PRIMARY KEY,
    key                VARCHAR(255) NOT NULL,
    user_id            UUID NULL,
    client_identifier  VARCHAR(255) NOT NULL,
    request_path       VARCHAR(255) NOT NULL,
    request_method     VARCHAR(10) NOT NULL,
    request_hash       VARCHAR(64) NOT NULL,
    status_code        INTEGER NULL,
    response_body      TEXT NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_idemp_unauthen
ON idempotency_keys(key, COALESCE(user_id::text, client_identifier));
