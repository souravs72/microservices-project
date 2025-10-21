-- Migration script to remove password column from users table
-- The User Service doesn't handle passwords - that's the Auth Service's responsibility

-- Remove the password column from users table
ALTER TABLE users DROP COLUMN IF EXISTS password;

-- Add comment for documentation
COMMENT ON TABLE users IS 'User profiles managed by User Service (passwords handled by Auth Service)';




