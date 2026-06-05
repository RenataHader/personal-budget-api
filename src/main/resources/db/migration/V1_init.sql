CREATE TABLE accounts(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0
);

CREATE TABLE transactions(
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(19, 2) NOT NULL,
    type VARCHAR(10) NOT NULL,
    category VARCHAR(255) NOT NULL,
    description TEXT,
    transaction_date DATE NOT NULL,
    account_id BIGINT NOT NULL,

    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id),

    CONSTRAINT chk_transaction_amount_positive
        CHECK (amount > 0),

    CONSTRAINT chk_transaction_type
        CHECK (type IN ('INCOME', 'EXPENSE'))
);

CREATE INDEX idx_transactions_account_id
    ON transactions(account_id);

CREATE INDEX idx_transactions_transaction_date
    ON transactions(transaction_date);

CREATE INDEX idx_transactions_category
    ON transactions(category);