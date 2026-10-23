CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE supplier (
    id         BIGINT GENERATED ALWAYS AS IDENTITY,
    name       VARCHAR(150) NOT NULL,
    email      VARCHAR(200),
    phone      VARCHAR(30),
    version    BIGINT       NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT pk_supplier PRIMARY KEY (id)
);

-- Supplier names are unique regardless of case.
CREATE UNIQUE INDEX uq_supplier_name ON supplier (lower(name));
-- Supports contains-search on name.
CREATE INDEX ix_supplier_name_trgm ON supplier USING gin (lower(name) gin_trgm_ops);
