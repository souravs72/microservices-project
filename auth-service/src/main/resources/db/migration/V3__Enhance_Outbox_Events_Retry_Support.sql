-- Migration to enhance outbox_events table with retry and failure tracking
-- This migration adds fields for robust retry mechanisms and circuit breaker support

-- Add new columns for enhanced retry handling
ALTER TABLE outbox_events 
ADD COLUMN last_retry_at TIMESTAMP,
ADD COLUMN next_retry_at TIMESTAMP,
ADD COLUMN failed BOOLEAN NOT NULL DEFAULT FALSE;

-- Add indexes for performance
CREATE INDEX idx_outbox_retry ON outbox_events (processed, next_retry_at);
CREATE INDEX idx_outbox_failed ON outbox_events (failed);
CREATE INDEX idx_outbox_retry_count ON outbox_events (retry_count);

-- Update existing records to have default values
UPDATE outbox_events SET failed = FALSE WHERE failed IS NULL;

-- Add comments for documentation
COMMENT ON COLUMN outbox_events.last_retry_at IS 'Timestamp of the last retry attempt';
COMMENT ON COLUMN outbox_events.next_retry_at IS 'Timestamp when the next retry should be attempted';
COMMENT ON COLUMN outbox_events.failed IS 'Indicates if the event has permanently failed after max retries';
COMMENT ON COLUMN outbox_events.retry_count IS 'Number of retry attempts made for this event';
COMMENT ON COLUMN outbox_events.error_message IS 'Error message from the last failed attempt';




