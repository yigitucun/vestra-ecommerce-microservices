CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(150) NOT NULL ,
    first_name VARCHAR(100) NOT NULL ,
    last_name VARCHAR(100) NOT NULL ,
    password VARCHAR NOT NULL ,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX users_email_index ON users(email)