ALTER TABLE setting_preferences
ADD COLUMN preferred_language VARCHAR(10) DEFAULT 'vi';

COMMENT ON COLUMN setting_preferences.preferred_language IS 'User preferred language for system communications (e.g., vi, en)';
