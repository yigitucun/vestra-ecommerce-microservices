CREATE TABLE payments
(
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        uuid           NOT NULL,
    order_number    VARCHAR(50)    NOT NULL,
    user_id         uuid           NOT NULL,
    amount          DECIMAL(12, 2) NOT NULL,
    status          VARCHAR(50)    NOT NULL DEFAULT 'PENDING',
    payment_method  VARCHAR(50)    NOT NULL,
    transaction_id  VARCHAR(100),
    card_last_four  VARCHAR(4),
    failure_reason  TEXT,
    version         BIGINT         NOT NULL DEFAULT 0,
    created_at      timestamptz    NOT NULL DEFAULT now(),
    updated_at      timestamptz    NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_order_id ON payments (order_id);
CREATE INDEX idx_payments_user_id ON payments (user_id);
CREATE INDEX idx_payments_status ON payments (status);

CREATE TABLE outbox_events
(
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id   VARCHAR(255) NOT NULL,
    event_type     VARCHAR(255) NOT NULL,
    payload        jsonb        NOT NULL,
    created_at     timestamptz  NOT NULL DEFAULT now()
);
