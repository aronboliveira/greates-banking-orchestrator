CREATE TABLE IF NOT EXISTS app_users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(40) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    display_name VARCHAR(80) NOT NULL,
    email VARCHAR(160) NOT NULL,
    role VARCHAR(30) NOT NULL,
    avatar_id VARCHAR(80) NOT NULL,
    notifications_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
