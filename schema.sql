DROP TABLE IF EXISTS notification_history;

CREATE TABLE notification_history (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      event_id VARCHAR(255) NOT NULL UNIQUE,
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

CREATE INDEX idx_event_id ON notification_history(event_id);
CREATE INDEX idx_recipient_email ON notification_history(recipient_email);
CREATE INDEX idx_status ON notification_history(status);
CREATE INDEX idx_created_at ON notification_history(created_at);