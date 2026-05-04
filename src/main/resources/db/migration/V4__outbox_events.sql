-- Transactional outbox table — guarantees at-least-once delivery to SQS even
-- if the application pod crashes between DB commit and SQS send.
--
-- Activation: set Spring profile eks-outbox in addition to eks. The bare eks
-- profile uses the lighter SqsTransactionEventListener fire-and-forget path
-- and does not write to this table.
--
-- payload column type: TEXT (not JSONB) for portability with H2 in tests. This
-- table is only ever appended to by the application and read in batches by
-- OutboxPublisher; the JSON payload is opaque to the database.

CREATE TABLE IF NOT EXISTS outbox_events (
    id            BIGSERIAL   PRIMARY KEY,
    aggregate_id  TEXT        NOT NULL,
    event_type    TEXT        NOT NULL,
    payload       TEXT        NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at  TIMESTAMPTZ,
    status        TEXT        NOT NULL DEFAULT 'PENDING'
);

-- Partial index makes the OutboxPublisher's pending-rows query O(log n) regardless
-- of the published-rows volume.
CREATE INDEX IF NOT EXISTS idx_outbox_pending
    ON outbox_events(status, created_at)
    WHERE status = 'PENDING';

-- pismo_app can read and write its own outbox rows (privileges already granted
-- in V2 ON ALL TABLES IN SCHEMA public).
