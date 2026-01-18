-- Create templates table for template management service
CREATE TABLE templates (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    template_id VARCHAR(255) UNIQUE NOT NULL,
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
    
    CONSTRAINT chk_category CHECK (category IN ('COMMON', 'EMAIL', 'SMS', 'DOCUMENT'))
);

CREATE INDEX idx_templates_uuid ON templates(uuid) WHERE is_deleted = FALSE;
CREATE INDEX idx_templates_template_id ON templates(template_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_templates_category ON templates(category) WHERE is_deleted = FALSE;
CREATE INDEX idx_templates_created_at ON templates(created_at DESC);
CREATE INDEX idx_templates_name ON templates(name) WHERE is_deleted = FALSE;
