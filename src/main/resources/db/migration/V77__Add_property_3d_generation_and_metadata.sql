ALTER TABLE property_medias ADD COLUMN IF NOT EXISTS metadata JSONB;

CREATE TABLE property_3d_generations (
    id UUID PRIMARY KEY,
    property_id UUID NOT NULL,
    uploader_id UUID NOT NULL,
    operation_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_property_3d_generations_property FOREIGN KEY (property_id) REFERENCES properties (property_id),
    CONSTRAINT fk_property_3d_generations_user FOREIGN KEY (uploader_id) REFERENCES users (user_id),
    CONSTRAINT uk_property_3d_generations_operation_id UNIQUE (operation_id)
);

CREATE INDEX idx_property_3d_gen_property ON property_3d_generations(property_id);
CREATE INDEX idx_property_3d_gen_status ON property_3d_generations(status);
