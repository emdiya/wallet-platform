CREATE TABLE ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(80) NOT NULL UNIQUE,
    source_topic VARCHAR(80) NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    aggregate_type VARCHAR(40) NOT NULL,
    aggregate_key VARCHAR(80) NOT NULL,
    request_id VARCHAR(80),
    trace_id VARCHAR(80),
    reference_no VARCHAR(80),
    wallet_id BIGINT,
    user_id BIGINT,
    customer_id VARCHAR(40),
    account_number VARCHAR(20),
    from_account_number VARCHAR(20),
    to_account_number VARCHAR(20),
    currency VARCHAR(10),
    operation_id VARCHAR(80),
    hold_id VARCHAR(80),
    status VARCHAR(40),
    amount NUMERIC(19, 2),
    balance NUMERIC(19, 2),
    purpose VARCHAR(255),
    error_code VARCHAR(50),
    error_message VARCHAR(255),
    occurred_at TIMESTAMPTZ NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ledger_entries_request_id ON ledger_entries (request_id);
CREATE INDEX idx_ledger_entries_account_number ON ledger_entries (account_number);
CREATE INDEX idx_ledger_entries_from_account_number ON ledger_entries (from_account_number);
CREATE INDEX idx_ledger_entries_to_account_number ON ledger_entries (to_account_number);
CREATE INDEX idx_ledger_entries_hold_id ON ledger_entries (hold_id);
CREATE INDEX idx_ledger_entries_occurred_at ON ledger_entries (occurred_at DESC);
