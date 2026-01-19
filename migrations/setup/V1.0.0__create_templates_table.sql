-- Create template table for template management service
CREATE TABLE template (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    template_id VARCHAR(255) NOT NULL,
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    name VARCHAR(255) NOT NULL,
    description TEXT,
    content TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    param_schema JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT chk_category CHECK (category IN ('COMMON', 'EMAIL', 'SMS', 'DOCUMENT')),
    CONSTRAINT uq_template_id_language UNIQUE (template_id, language)
);

CREATE INDEX idx_template_uuid ON template(uuid) WHERE is_deleted = FALSE;
CREATE INDEX idx_template_template_id_language ON template(template_id, language) WHERE is_deleted = FALSE;
CREATE INDEX idx_template_category ON template(category) WHERE is_deleted = FALSE;
