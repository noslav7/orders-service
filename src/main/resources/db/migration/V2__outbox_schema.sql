CREATE TABLE IF NOT EXISTS orders_outbox(
    order_id BIGINT PRIMARY KEY,
    created_by TEXT NOT NULL,
    city TEXT NOT NULL,
    street TEXT NOT NULL,
    house INTEGER NOT NULL,
    apartment INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS heartbeat_table(
    id INTEGER PRIMARY KEY,
    updated_at TIMESTAMP NOT NULL
);

CREATE PUBLICATION orders_outbox_publication FOR TABLE orders_outbox, heartbeat_table;