-- V52__Add_is_property_standard_to_property_medias.sql
ALTER TABLE property_medias ADD COLUMN is_property_standard BOOLEAN DEFAULT TRUE;
CREATE INDEX idx_property_media_standard ON property_medias(is_property_standard);

-- Update existing records to be property-standard
UPDATE property_medias SET is_property_standard = TRUE WHERE is_property_standard IS NULL;
