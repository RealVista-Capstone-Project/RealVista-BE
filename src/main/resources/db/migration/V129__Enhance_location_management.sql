-- Add management metadata and integrity constraints for administrative locations.

ALTER TABLE locations ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE locations ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 0;
ALTER TABLE locations ADD COLUMN normalized_name VARCHAR(150);

UPDATE locations
SET normalized_name = translate(
    LOWER(name),
    'đáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ',
    'daaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyy'
)
WHERE normalized_name IS NULL;

ALTER TABLE locations ADD CONSTRAINT chk_location_status
    CHECK (status IN ('ACTIVE', 'ARCHIVED'));

ALTER TABLE locations ADD CONSTRAINT chk_location_parent_by_type
    CHECK (
        (type = 'CITY' AND parent_id IS NULL)
        OR (type IN ('DISTRICT', 'WARD') AND parent_id IS NOT NULL)
    );

CREATE UNIQUE INDEX uk_location_code_active
ON locations(code)
WHERE deleted = FALSE AND code IS NOT NULL;

CREATE UNIQUE INDEX uk_location_parent_name_active
ON locations(parent_id, name)
WHERE deleted = FALSE;

CREATE INDEX idx_location_status ON locations(status);
CREATE INDEX idx_location_sort ON locations(sort_order);
CREATE INDEX idx_location_normalized_name ON locations(normalized_name);
