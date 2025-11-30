-- Add telegram and habr fields to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS telegram VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS habr VARCHAR(255);
