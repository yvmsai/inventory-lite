CREATE TABLE product (
    id            BIGINT GENERATED ALWAYS AS IDENTITY,
    sku           VARCHAR(64)    NOT NULL,
    name          VARCHAR(200)   NOT NULL,
    unit_price    NUMERIC(19, 2) NOT NULL,
    quantity      INTEGER        NOT NULL DEFAULT 0,
    reorder_level INTEGER        NOT NULL DEFAULT 10,
    supplier_id   BIGINT         NOT NULL,
    version       BIGINT         NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_by    BIGINT,
    updated_by    BIGINT,
    CONSTRAINT pk_product PRIMARY KEY (id),
    CONSTRAINT uq_product_sku UNIQUE (sku),
    CONSTRAINT fk_product_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (id),
    CONSTRAINT ck_product_sku_format CHECK (sku ~ '^[A-Z0-9-]+$'),
    CONSTRAINT ck_product_unit_price_non_negative CHECK (unit_price >= 0),
    CONSTRAINT ck_product_quantity_non_negative CHECK (quantity >= 0),
    CONSTRAINT ck_product_reorder_level_non_negative CHECK (reorder_level >= 0)
);

CREATE INDEX ix_product_supplier_id ON product (supplier_id);
CREATE INDEX ix_product_name ON product (name);
-- Contains-search on name and SKU.
CREATE INDEX ix_product_name_trgm ON product USING gin (lower(name) gin_trgm_ops);
CREATE INDEX ix_product_sku_trgm ON product USING gin (lower(sku) gin_trgm_ops);
-- Dashboard low-stock list.
CREATE INDEX ix_product_low_stock ON product (quantity) WHERE quantity <= reorder_level;
