CREATE TABLE items (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    variant_id  uuid NOT NULL UNIQUE,
    quantity    INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    reserved    INTEGER NOT NULL DEFAULT 0 CHECK (reserved >= 0),
    active      BOOLEAN NOT NULL DEFAULT true,
    updated_at  timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_items_variant_id ON items (variant_id);