ALTER TABLE users
ADD COLUMN provider VARCHAR(20) DEFAULT 'local',
ADD COLUMN provider_id VARCHAR(100),
ADD COLUMN image_url VARCHAR(255),
ADD COLUMN email_verified BOOLEAN DEFAULT false;


-- Make email nullable for OAuth2 users (they might not have email initially)
ALTER TABLE users ALTER COLUMN email DROP NOT NULL;

-- Add unique constraint for provider + provider_id
CREATE UNIQUE INDEX uk_user_provider ON users(provider, provider_id)
WHERE provider_id IS NOT NULL;

-- Update existing users to have provider = 'local'
UPDATE users SET provider = 'local' WHERE provider IS NULL;