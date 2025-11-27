-- Add fcm_token field to users table for push notifications
ALTER TABLE users ADD COLUMN IF NOT EXISTS fcm_token VARCHAR(512);

-- Create index for faster FCM token lookups
CREATE INDEX IF NOT EXISTS idx_users_fcm_token ON users(fcm_token);
