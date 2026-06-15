CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_equipments_inventory_code ON equipments(inventory_code);
CREATE INDEX IF NOT EXISTS idx_assignments_status ON assignments(status);
CREATE INDEX IF NOT EXISTS idx_maintenances_status ON maintenances(status);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_read ON notifications(recipient_id, read);

ALTER TABLE IF EXISTS users DROP CONSTRAINT IF EXISTS users_status_check;
ALTER TABLE IF EXISTS users DROP CONSTRAINT IF EXISTS users_language_check;
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS system_account boolean DEFAULT false;
UPDATE users SET system_account = false WHERE system_account IS NULL;
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS email_verified boolean NOT NULL DEFAULT false;
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS enabled boolean NOT NULL DEFAULT true;
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS failed_login_attempts integer NOT NULL DEFAULT 0;
