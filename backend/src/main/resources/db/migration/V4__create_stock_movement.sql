-- Immutable log: rows are only ever inserted.
CREATE TABLE stock_movement (
    id             BIGINT GENERATED ALWAYS AS IDENTITY,
    product_id     BIGINT       NOT NULL,
    type           VARCHAR(32)  NOT NULL,
    quantity       INTEGER      NOT NULL,
    quantity_after INTEGER      NOT NULL,
    note           VARCHAR(500),
    created_by     BIGINT       NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_stock_movement PRIMARY KEY (id),
    CONSTRAINT fk_stock_movement_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_stock_movement_created_by FOREIGN KEY (created_by) REFERENCES app_user (id),
    CONSTRAINT ck_stock_movement_type CHECK (type IN ('IN', 'OUT')),
    CONSTRAINT ck_stock_movement_quantity_positive CHECK (quantity > 0),
    CONSTRAINT ck_stock_movement_quantity_after_non_negative CHECK (quantity_after >= 0)
);

CREATE INDEX ix_stock_movement_product_id_created_at ON stock_movement (product_id, created_at DESC, id DESC);
CREATE INDEX ix_stock_movement_created_at ON stock_movement (created_at DESC, id DESC);
CREATE INDEX ix_stock_movement_created_by ON stock_movement (created_by);
