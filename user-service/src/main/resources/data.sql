-- Data SQL for User Service
-- This automatically creates sample users on application startup

-- Insert sample users
INSERT INTO users (username, email, password, first_name, last_name, phone, active, created_at, updated_at)
VALUES
    ('john_doe', 'john.doe@example.com', 'password123', 'John', 'Doe', '+1-555-0101', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('jane_smith', 'jane.smith@example.com', 'password123', 'Jane', 'Smith', '+1-555-0102', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('bob_wilson', 'bob.wilson@example.com', 'password123', 'Bob', 'Wilson', '+1-555-0103', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('alice_johnson', 'alice.johnson@example.com', 'password123', 'Alice', 'Johnson', '+1-555-0104', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('charlie_brown', 'charlie.brown@example.com', 'password123', 'Charlie', 'Brown', '+1-555-0105', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('diana_prince', 'diana.prince@example.com', 'password123', 'Diana', 'Prince', '+1-555-0106', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('edward_stark', 'edward.stark@example.com', 'password123', 'Edward', 'Stark', '+1-555-0107', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('fiona_gallagher', 'fiona.gallagher@example.com', 'password123', 'Fiona', 'Gallagher', '+1-555-0108', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('george_martin', 'george.martin@example.com', 'password123', 'George', 'Martin', '+1-555-0109', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('hannah_montana', 'hannah.montana@example.com', 'password123', 'Hannah', 'Montana', '+1-555-0110', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Note: In production, passwords should be hashed using BCrypt or similar!
-- Example with BCrypt (if you implement password encoding):
-- INSERT INTO users (username, email, password, first_name, last_name, phone, active)
-- VALUES ('admin', 'admin@example.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Admin', 'User', '+1-555-0100', TRUE);