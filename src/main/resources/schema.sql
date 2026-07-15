CREATE TABLE IF NOT EXISTS classroom (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS classroom_member (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    account_id VARCHAR(255) NOT NULL,
    classroom_id UUID NOT NULL,
    role VARCHAR(32),
    status VARCHAR(32) NOT NULL,
    -- Denormalized fields from account/identity service
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,

    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    UNIQUE(classroom_id, account_id)
);
