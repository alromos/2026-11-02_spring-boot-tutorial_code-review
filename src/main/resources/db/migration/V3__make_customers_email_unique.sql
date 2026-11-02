-- Emails must be unique. The unique index also serves lookups by email,
-- so the plain index from V2 is no longer needed.
-- Emails are stored lower-case by the application, so this is a case-insensitive rule in practice.
DROP INDEX idx_customers_email;
CREATE UNIQUE INDEX uq_customers_email ON customers (email);
