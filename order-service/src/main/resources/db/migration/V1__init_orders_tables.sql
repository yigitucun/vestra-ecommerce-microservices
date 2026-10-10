CREATE TABLE orders
(
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number     VARCHAR(50)    NOT NULL UNIQUE,
    user_id          uuid           NOT NULL,
    status           VARCHAR(50)    NOT NULL DEFAULT 'PENDING',
    total_amount     DECIMAL(12, 2) NOT NULL,
    shipping_address TEXT           NOT NULL,
    version          BIGINT         NOT NULL DEFAULT 0,
    created_at       timestamptz    NOT NULL DEFAULT now(),
    updated_at       timestamptz    NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_status ON orders (status);

CREATE TABLE order_items
(
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id     uuid           NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    variant_id   uuid           NOT NULL,
    product_name VARCHAR(255)   NOT NULL,
    sku          VARCHAR(255)   NOT NULL,
    quantity     INT            NOT NULL CHECK (quantity > 0),
    unit_price   DECIMAL(10, 2) NOT NULL,
    subtotal     DECIMAL(12, 2) NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_variant_id ON order_items (variant_id);

CREATE TABLE outbox_events
(
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id   VARCHAR(255) NOT NULL,
    event_type     VARCHAR(255) NOT NULL,
    payload        jsonb        NOT NULL,
    created_at     timestamptz  NOT NULL DEFAULT now()
);
