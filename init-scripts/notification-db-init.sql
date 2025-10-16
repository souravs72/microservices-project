-- Notification Service Database Schema

-- Notification history table
CREATE TABLE IF NOT EXISTS notification_history (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    event_id VARCHAR(255) UNIQUE NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(200),
    subject VARCHAR(500) NOT NULL,
    body TEXT,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    correlation_id VARCHAR(50)
    );

-- Indexes
CREATE INDEX IF NOT EXISTS idx_event_id ON notification_history(event_id);
CREATE INDEX IF NOT EXISTS idx_recipient_email ON notification_history(recipient_email);
CREATE INDEX IF NOT EXISTS idx_status ON notification_history(status);
CREATE INDEX IF NOT EXISTS idx_created_at ON notification_history(created_at);