CREATE TABLE app_user (
    id            BIGINT GENERATED ALWAYS AS IDENTITY,
    email         VARCHAR(200) NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(32)  NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    version       BIGINT       NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    BIGINT,
    updated_by    BIGINT,
    CONSTRAINT pk_app_user PRIMARY KEY (id),
    CONSTRAINT ck_app_user_role CHECK (role IN ('ADMIN', 'STAFF')),
    CONSTRAINT ck_app_user_email_lower CHECK (email = lower(email))
);

-- Emails are stored lower-cased, so a plain unique index enforces case-insensitive uniqueness.
CREATE UNIQUE INDEX uq_app_user_email ON app_user (email);
