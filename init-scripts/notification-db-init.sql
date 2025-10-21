-- Create notification database initialization script
-- This script runs when the PostgreSQL container starts for the first time

-- Create the notificationdb database (if it doesn't exist)
-- Note: This is handled by the POSTGRES_DB environment variable

-- Create the notificationuser user (if it doesn't exist)
-- Note: This is handled by the POSTGRES_USER environment variable

-- Grant necessary permissions
GRANT ALL PRIVILEGES ON DATABASE notificationdb TO notificationuser;

-- Connect to the notificationdb database
\c notificationdb;

-- Grant schema permissions
GRANT ALL ON SCHEMA public TO notificationuser;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO notificationuser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO notificationuser;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO notificationuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO notificationuser;
