-- Initial schema for the greatest-banking-orchestrator-api on Amazon RDS Aurora PostgreSQL.
-- This migration replaces Hibernate's auto-DDL (which is only used in the
-- default profile / Docker Compose). In the eks profile, Hibernate runs with
-- ddl-auto=validate and Flyway is the source of truth.

CREATE TABLE IF NOT EXISTS operation_types (
    operation_type_id INTEGER PRIMARY KEY,
    description       VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts (
    account_id       BIGSERIAL PRIMARY KEY,
    -- 512 chars: holds either the original 11-digit document_number (default)
    -- OR the Base64 ciphertext produced by KmsFieldEncryptor in the eks profile.
    document_number  VARCHAR(512) NOT NULL,
    CONSTRAINT uk_accounts_document_number UNIQUE (document_number)
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id      BIGSERIAL PRIMARY KEY,
    account_id          BIGINT      NOT NULL REFERENCES accounts(account_id),
    operation_type_id   INTEGER     NOT NULL REFERENCES operation_types(operation_type_id),
    amount              NUMERIC(19,4) NOT NULL,
    event_date          TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_event_date  ON transactions(event_date);

-- Reference data — same values seeded by DataInitializer in default profile.
INSERT INTO operation_types (operation_type_id, description) VALUES
    (1, 'PURCHASE'),
    (2, 'INSTALLMENT PURCHASE'),
    (3, 'WITHDRAWAL'),
    (4, 'PAYMENT')
ON CONFLICT (operation_type_id) DO NOTHING;
