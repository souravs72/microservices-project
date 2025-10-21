-- Auth Service Database Schema

-- Users table
CREATE TABLE IF NOT EXISTS auth_users (
                                          id BIGSERIAL PRIMARY KEY,
                                          username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    last_login_ip VARCHAR(45),
    last_login_at TIMESTAMP,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    last_failed_login_at TIMESTAMP,
    account_locked BOOLEAN NOT NULL DEFAULT false,
    account_locked_at TIMESTAMP
    );

-- Refresh tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id BIGSERIAL PRIMARY KEY,
                                              token VARCHAR(500) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT false,
    revoked_at TIMESTAMP,
    device_id VARCHAR(100),
    device_name VARCHAR(200),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    FOREIGN KEY (user_id) REFERENCES auth_users(id) ON DELETE CASCADE
    );

-- Password reset tokens table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
                                                     id BIGSERIAL PRIMARY KEY,
                                                     token VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used BOOLEAN NOT NULL DEFAULT false,
    used_at TIMESTAMP,
    request_ip VARCHAR(45),
    FOREIGN KEY (user_id) REFERENCES auth_users(id) ON DELETE CASCADE
    );

-- User sessions table
CREATE TABLE IF NOT EXISTS user_sessions (
                                             id BIGSERIAL PRIMARY KEY,
                                             user_id BIGINT NOT NULL,
                                             session_token VARCHAR(255) UNIQUE NOT NULL,
    device_id VARCHAR(100),
    device_name VARCHAR(200),
    device_type VARCHAR(100),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT true,
    logged_out_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES auth_users(id) ON DELETE CASCADE
    );

-- Outbox events table
CREATE TABLE IF NOT EXISTS outbox_events (
                                             id BIGSERIAL PRIMARY KEY,
                                             aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN NOT NULL DEFAULT false,
    processed_at TIMESTAMP,
    retry_count INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    correlation_id VARCHAR(50)
    );

-- Indexes
CREATE INDEX IF NOT EXISTS idx_username ON auth_users(username);
CREATE INDEX IF NOT EXISTS idx_email ON auth_users(email);
CREATE INDEX IF NOT EXISTS idx_account_locked ON auth_users(account_locked);
CREATE INDEX IF NOT EXISTS idx_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_device_id ON refresh_tokens(device_id);
CREATE INDEX IF NOT EXISTS idx_reset_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_reset_user_id ON password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_session_token ON user_sessions(session_token);
CREATE INDEX IF NOT EXISTS idx_session_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_session_device_id ON user_sessions(device_id);
CREATE INDEX IF NOT EXISTS idx_outbox_processed ON outbox_events(processed);
CREATE INDEX IF NOT EXISTS idx_outbox_event_type ON outbox_events(event_type);
CREATE INDEX IF NOT EXISTS idx_outbox_created_at ON outbox_events(created_at);

-- Create admin user (password: [Set in ADMIN_PASSWORD environment variable])
INSERT INTO auth_users (username, email, password, role, enabled)
VALUES ('admin', 'admin@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYC6FYT3aN6', 'ADMIN', true)
    ON CONFLICT (username) DO NOTHING;