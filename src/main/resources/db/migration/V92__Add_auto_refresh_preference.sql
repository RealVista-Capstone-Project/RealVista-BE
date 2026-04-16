ALTER TABLE setting_preferences
ADD COLUMN auto_refresh_enabled BOOLEAN DEFAULT TRUE;

UPDATE setting_preferences SET auto_refresh_enabled = TRUE WHERE auto_refresh_enabled IS NULL;
