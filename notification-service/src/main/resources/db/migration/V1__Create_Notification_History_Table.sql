-- Create notification_history table
CREATE TABLE IF NOT EXISTS notification_history (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(255) UNIQUE NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(255),
    subject VARCHAR(500),
    content TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    error_message TEXT,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    failed_at TIMESTAMP,
    next_retry_at TIMESTAMP,
    template_name VARCHAR(100),
    template_variables TEXT,
    channel VARCHAR(100) DEFAULT 'EMAIL',
    priority VARCHAR(100) DEFAULT 'NORMAL',
    scheduled_at TIMESTAMP,
    expires_at TIMESTAMP,
    user_id BIGINT,
    external_reference VARCHAR(255),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_from_ip VARCHAR(45),
    updated_from_ip VARCHAR(45),
    version BIGINT DEFAULT 0
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_notification_history_event_id ON notification_history(event_id);
CREATE INDEX IF NOT EXISTS idx_notification_history_event_type ON notification_history(event_type);
CREATE INDEX IF NOT EXISTS idx_notification_history_recipient_email ON notification_history(recipient_email);
CREATE INDEX IF NOT EXISTS idx_notification_history_status ON notification_history(status);
CREATE INDEX IF NOT EXISTS idx_notification_history_created_at ON notification_history(created_at);
CREATE INDEX IF NOT EXISTS idx_notification_history_sent_at ON notification_history(sent_at);
CREATE INDEX IF NOT EXISTS idx_notification_history_channel ON notification_history(channel);
CREATE INDEX IF NOT EXISTS idx_notification_history_user_id ON notification_history(user_id);

-- Create trigger to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_notification_history_updated_at 
    BEFORE UPDATE ON notification_history 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
