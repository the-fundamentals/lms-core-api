CREATE TABLE IF NOT EXISTS account_profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cognito_sub VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    avatar_key VARCHAR(255),

    UNIQUE(cognito_sub)
);

CREATE TABLE IF NOT EXISTS classroom (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    banner_key VARCHAR(255),
    number_of_members INTEGER NOT NULL DEFAULT 0,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS classroom_member (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    account_id UUID NOT NULL,
    classroom_id UUID NOT NULL,
    role VARCHAR(32),
    status VARCHAR(32) NOT NULL,
    -- Denormalized fields from account/identity service
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,

    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    UNIQUE(classroom_id, account_id)
);

CREATE TABLE IF NOT EXISTS classroom_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    session_date TIMESTAMP NOT NULL,
    classroom_id UUID NOT NULL,
    name VARCHAR(255),
    description VARCHAR(2000),

    FOREIGN KEY (classroom_id) REFERENCES classroom(id)
);

CREATE TABLE IF NOT EXISTS classroom_attendance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    session_id UUID NOT NULL,
    classroom_member_id UUID NOT NULL,
    attendance_date TIMESTAMP,
    status VARCHAR(32) NOT NULL,

    FOREIGN KEY (session_id) REFERENCES classroom_session(id) ON DELETE CASCADE,
    FOREIGN KEY (classroom_member_id) REFERENCES classroom_member(id),
    UNIQUE(session_id, classroom_member_id)
);

CREATE TABLE IF NOT EXISTS storage_grants (
    id BIGSERIAL PRIMARY KEY,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    owner_id UUID,
    object_key VARCHAR(255) NOT NULL,
    object_bucket VARCHAR(255) NOT NULL,

    UNIQUE(owner_id, object_key),
    FOREIGN KEY (owner_id) REFERENCES account_profile(id)
)