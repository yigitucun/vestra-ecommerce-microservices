CREATE TABLE stock_movements
(
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id            uuid         NOT NULL REFERENCES items (id) ON DELETE CASCADE,
    variant_id         uuid         NOT NULL,
    change_amount      INT          NOT NULL,
    resulting_quantity INT          NOT NULL,
    movement_type      VARCHAR(50)  NOT NULL,
    reference_id       VARCHAR(255),
    created_at         timestamptz  NOT NULL DEFAULT now()
);

CREATE INDEX idx_stock_movements_variant_id ON stock_movements (variant_id);
