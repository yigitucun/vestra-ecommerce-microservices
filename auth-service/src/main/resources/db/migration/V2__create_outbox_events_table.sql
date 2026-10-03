CREATE TABLE outbox_events (
    id uuid primary key ,
    aggregate_type varchar(255) not null ,
    aggregate_id varchar(255) not null ,
    event_type varchar(255) not null ,
    payload jsonb not null,
    created_at timestamptz not null default now()
)