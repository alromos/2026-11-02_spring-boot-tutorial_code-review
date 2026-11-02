-- Schema update: speeds up lookups by email.
CREATE INDEX idx_customers_email ON customers (email);
