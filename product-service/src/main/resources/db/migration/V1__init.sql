CREATE TABLE categories
(
    id          uuid PRIMARY KEY      DEFAULT gen_random_uuid(),
    parent_id   uuid         REFERENCES categories (id) ON DELETE SET NULL,
    name        varchar(255) NOT NULL,
    slug        varchar(255) NOT NULL UNIQUE,
    description TEXT,
    created_at  timestamptz  NOT NULL DEFAULT NOW(),
    updated_at  timestamptz  NOT NULL DEFAULT NOW()
);

CREATE TABLE products
(
    id          uuid PRIMARY KEY      DEFAULT gen_random_uuid(),
    name        varchar(255) NOT NULL,
    description TEXT,
    image_url   varchar(255),
    category_id uuid         NOT NULL REFERENCES categories (id) ON DELETE set null ,
    created_at  timestamptz  NOT NULL DEFAULT NOW(),
    updated_at  timestamptz  NOT NULL DEFAULT NOW()
);

CREATE TABLE variants
(
    id         uuid PRIMARY KEY        DEFAULT gen_random_uuid(),

    product_id uuid           NOT NULL
        REFERENCES products (id) ON DELETE CASCADE,

    price      DECIMAL(10, 2) NOT NULL,
    sku        VARCHAR(255)   NOT NULL UNIQUE,

    created_at timestamptz    NOT NULL DEFAULT NOW(),
    updated_at timestamptz    NOT NULL DEFAULT NOW()
);

CREATE TABLE variant_attributes
(
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),

    variant_id      uuid         NOT NULL
        REFERENCES variants (id) ON DELETE CASCADE,

    attribute_name  VARCHAR(255) NOT NULL,
    attribute_value VARCHAR(255) NOT NULL,

    UNIQUE (variant_id, attribute_name)
);

CREATE INDEX idx_variants_product_id
    ON variants (product_id);

CREATE INDEX idx_variant_attributes_variant_id
    ON variant_attributes (variant_id);

CREATE TABLE reviews
(
    id         uuid PRIMARY KEY     DEFAULT gen_random_uuid(),
    product_id uuid        NOT NULL
        REFERENCES products (id) ON DELETE CASCADE,
    user_id    uuid        NOT NULL,
    rating     INT         NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    TEXT,
    created_at timestamptz NOT NULL DEFAULT NOW()
);

CREATE TABLE outbox_events
(
    id             uuid primary key      default gen_random_uuid(),
    aggregate_type varchar(255) not null,
    aggregate_id   varchar(255) not null,
    event_type     varchar(255) not null,
    payload        jsonb        not null,
    created_at     timestamptz  not null default now()
)


















