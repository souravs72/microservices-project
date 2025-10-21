-- Migration to create outbox_events table for event-driven architecture
-- This table implements the Outbox Pattern for reliable event publishing

CREATE TABLE outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN NOT NULL DEFAULT false,
    processed_at TIMESTAMP,
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_retry_at TIMESTAMP,
    next_retry_at TIMESTAMP,
    failed BOOLEAN NOT NULL DEFAULT false,
    error_message TEXT,
    correlation_id VARCHAR(50)
);

-- Create indexes for better performance
CREATE INDEX idx_outbox_events_processed ON outbox_events(processed);
CREATE INDEX idx_outbox_events_event_type ON outbox_events(event_type);
CREATE INDEX idx_outbox_events_created_at ON outbox_events(created_at);
CREATE INDEX idx_outbox_events_next_retry_at ON outbox_events(next_retry_at);
CREATE INDEX idx_outbox_events_correlation_id ON outbox_events(correlation_id);

-- Add comments for documentation
COMMENT ON TABLE outbox_events IS 'Outbox events for reliable event publishing using Outbox Pattern';
COMMENT ON COLUMN outbox_events.aggregate_type IS 'Type of aggregate (USER, ORDER, etc.)';
COMMENT ON COLUMN outbox_events.aggregate_id IS 'ID of the aggregate';
COMMENT ON COLUMN outbox_events.event_type IS 'Type of event (USER_CREATED, USER_UPDATED, etc.)';
COMMENT ON COLUMN outbox_events.payload IS 'JSON payload of the event';
COMMENT ON COLUMN outbox_events.processed IS 'Whether the event has been processed';
COMMENT ON COLUMN outbox_events.retry_count IS 'Number of retry attempts';
COMMENT ON COLUMN outbox_events.failed IS 'Whether the event processing failed permanently';
COMMENT ON COLUMN outbox_events.correlation_id IS 'Correlation ID for tracing';




