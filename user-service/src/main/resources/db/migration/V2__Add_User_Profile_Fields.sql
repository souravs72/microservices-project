-- Migration script to add new fields to users table
-- Run this script on the user-service database

-- Add new columns to users table
ALTER TABLE users 
ADD COLUMN address VARCHAR(500),
ADD COLUMN bio VARCHAR(1000),
ADD COLUMN profile_picture_url VARCHAR(500),
ADD COLUMN role VARCHAR(50) DEFAULT 'USER',
ADD COLUMN member_since TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN created_by VARCHAR(255),
ADD COLUMN last_modified_by VARCHAR(255);

-- Update existing users to set member_since to their created_at date
UPDATE users SET member_since = created_at WHERE member_since IS NULL;

-- Create indexes for better performance
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_member_since ON users(member_since);
CREATE INDEX idx_users_created_by ON users(created_by);
CREATE INDEX idx_users_last_modified_by ON users(last_modified_by);

-- Add comments for documentation
COMMENT ON COLUMN users.address IS 'User address information';
COMMENT ON COLUMN users.bio IS 'User biography/description';
COMMENT ON COLUMN users.profile_picture_url IS 'URL to user profile picture';
COMMENT ON COLUMN users.role IS 'User role (USER, ADMIN, etc.)';
COMMENT ON COLUMN users.member_since IS 'Date when user became a member';
COMMENT ON COLUMN users.created_by IS 'User who created this record';
COMMENT ON COLUMN users.last_modified_by IS 'User who last modified this record';
