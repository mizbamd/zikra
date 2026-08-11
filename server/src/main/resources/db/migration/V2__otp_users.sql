-- OTP-only accounts have no password. Legacy email+password rows keep their hash.
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;
