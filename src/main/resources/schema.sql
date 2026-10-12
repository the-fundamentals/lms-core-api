CREATE TABLE IF NOT EXISTS account_profile
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_date       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    cognito_sub        VARCHAR(255) NOT NULL,
    email              VARCHAR(255) NOT NULL,
    first_name         VARCHAR(255) NOT NULL,
    last_name          VARCHAR(255) NOT NULL,
    avatar_key         VARCHAR(255),

    UNIQUE (cognito_sub)
);

CREATE TABLE IF NOT EXISTS classroom
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name               VARCHAR(255) NOT NULL,
    banner_key         VARCHAR(255),
    number_of_members  INTEGER      NOT NULL DEFAULT 0,
    created_date       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    status             VARCHAR(64)           DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS classroom_member
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_date       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    account_id         VARCHAR(64)  NOT NULL,
    classroom_id       UUID         NOT NULL,
    role               VARCHAR(32),
    status             VARCHAR(32)  NOT NULL,
    removed_date       TIMESTAMP,
    -- Denormalized fields from account/identity service
    email              VARCHAR(255) NOT NULL,
    name               VARCHAR(255) NOT NULL,
    avatar             VARCHAR(255),

    FOREIGN KEY (classroom_id) REFERENCES classroom (id),
    UNIQUE (classroom_id, account_id)
);

CREATE TABLE IF NOT EXISTS classroom_member_payment_plan
(
    id                  UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    created_date        TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    last_modified_date  TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    classroom_member_id UUID        NOT NULL,
    type                VARCHAR(32) NOT NULL DEFAULT 'PER_SESSION',
    amount              BIGINT      NOT NULL,
    currency            VARCHAR(32) NOT NULL DEFAULT 'VND',
    effective_from      TIMESTAMP   NOT NULL,
    effective_to        TIMESTAMP,

    FOREIGN KEY (classroom_member_id) REFERENCES classroom_member (id)
);

-- One open-ended (current) plan per member. No future-dated plans.
CREATE UNIQUE INDEX IF NOT EXISTS unique_current_payment_plan
    ON classroom_member_payment_plan (classroom_member_id)
    WHERE effective_to IS NULL;

CREATE TABLE IF NOT EXISTS classroom_schedule_recurrence
(
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_date          TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    last_modified_date    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    deleted_date          TIMESTAMP,
    frequency             VARCHAR(64) NOT NULL,
    by_day                VARCHAR(64) NOT NULL,
    recurrence_start_date DATE        NOT NULL,
    recur_until           DATE,
    start_time            TIME        NOT NULL,
    end_time              TIME        NOT NULL,
    name                  VARCHAR(255),
    description           VARCHAR(2000),
    classroom_id          UUID,

    FOREIGN KEY (classroom_id) REFERENCES classroom (id)
);

CREATE TABLE IF NOT EXISTS classroom_session
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_date       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    session_date       DATE NOT NULL,
    start_time         TIME NOT NULL,
    end_time           TIME NOT NULL,
    classroom_id       UUID NOT NULL,
    name               VARCHAR(255),
    description        VARCHAR(2000),
    status             VARCHAR(64)      DEFAULT 'OPEN',
    type               VARCHAR(64)      DEFAULT 'SCHEDULE',
    generated_by       UUID,
    FOREIGN KEY (classroom_id) REFERENCES classroom (id),
    FOREIGN KEY (generated_by) REFERENCES classroom_schedule_recurrence (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS unique_generated_session_idx
    ON classroom_session (classroom_id, generated_by, session_date)
    WHERE generated_by IS NOT NULL;

CREATE TABLE IF NOT EXISTS classroom_attendance
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_date        TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    last_modified_date  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    session_id          UUID        NOT NULL,
    classroom_member_id UUID        NOT NULL,
    attendance_date     TIMESTAMP,
    status              VARCHAR(32) NOT NULL,

    FOREIGN KEY (session_id) REFERENCES classroom_session (id) ON DELETE CASCADE,
    FOREIGN KEY (classroom_member_id) REFERENCES classroom_member (id),
    UNIQUE (session_id, classroom_member_id)
);

CREATE TABLE IF NOT EXISTS storage_grants
(
    id                 BIGSERIAL PRIMARY KEY,
    created_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    owner_id           UUID,
    object_key         VARCHAR(255) NOT NULL,
    object_bucket      VARCHAR(255) NOT NULL,

    UNIQUE (owner_id, object_key),
    FOREIGN KEY (owner_id) REFERENCES account_profile (id)
);

CREATE TABLE IF NOT EXISTS classroom_invoice
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_date       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    classroom_id       UUID        NOT NULL,
    member_id          UUID        NOT NULL,
    status             VARCHAR(64) NOT NULL,        -- DRAFT, APPROVED, COMPLETED
    type               VARCHAR(64) NOT NULL,        -- GENERATED (ADHOC will be added later)

    period_start       DATE,
    period_range       VARCHAR(64) DEFAULT 'MONTH', -- MONTH

    total_amount       BIGINT      NOT NULL CHECK (total_amount >= 0),
    note               TEXT,

    approved_at        TIMESTAMP,
    paid_at            TIMESTAMP,
    paid_amount        BIGINT,
    payment_method     VARCHAR(64),                 -- BANK, CASH, OTHER
    payment_reference  TEXT,
    payment_note       TEXT,

    FOREIGN KEY (classroom_id) REFERENCES classroom (id),
    FOREIGN KEY (member_id) REFERENCES classroom_member (id)
);
CREATE INDEX IF NOT EXISTS idx_classroom_invoice_classroom ON classroom_invoice (classroom_id);
CREATE INDEX IF NOT EXISTS idx_classroom_invoice_member ON classroom_invoice (member_id);
CREATE UNIQUE INDEX IF NOT EXISTS unique_generated_invoice_member_period
    ON classroom_invoice (member_id, period_range, period_start) NULLS NOT DISTINCT
WHERE type = 'GENERATED';

-- Aggregate child of classroom_invoice.
CREATE TABLE IF NOT EXISTS classroom_invoice_line
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    invoice_id         UUID        NOT NULL,
    kind               VARCHAR(64) NOT NULL, -- LESSON (or other types added here)
    description        TEXT        NOT NULL,
    unit_price         BIGINT      NOT NULL CHECK (unit_price >= 0),
    quantity           INT         NOT NULL CHECK (quantity >= 0),

    final_amount       BIGINT      NOT NULL,

    CONSTRAINT chk_invoice_final_amount CHECK (final_amount = unit_price * quantity),
    FOREIGN KEY (invoice_id) REFERENCES classroom_invoice (id)
);
CREATE INDEX IF NOT EXISTS idx_classroom_invoice_line_invoice ON classroom_invoice_line (invoice_id);