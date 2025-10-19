-- Auth Service Database Initialization Script
-- This script creates the necessary database and user for the Auth Service

-- Create the auth database
CREATE DATABASE auth_db;

-- Create a user for the auth service
CREATE USER auth_user WITH PASSWORD 'auth_password';

-- Grant privileges to the auth user
GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth_user;

-- Connect to the auth database
\c auth_db;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO auth_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO auth_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO auth_user;
