-- Add indexes to ghg_emissions table
CREATE INDEX IF NOT EXISTS idx_ghg_company ON ghg_emissions(company_id);
CREATE INDEX IF NOT EXISTS idx_ghg_status ON ghg_emissions(status);
CREATE INDEX IF NOT EXISTS idx_ghg_scope ON ghg_emissions(scope);
CREATE INDEX IF NOT EXISTS idx_ghg_dates ON ghg_emissions(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_ghg_submitter ON ghg_emissions(submitted_by_id);

-- Add indexes to social_metrics table
CREATE INDEX IF NOT EXISTS idx_social_company ON social_metrics(company_id);
CREATE INDEX IF NOT EXISTS idx_social_status ON social_metrics(status);
CREATE INDEX IF NOT EXISTS idx_social_subtype ON social_metrics(subtype);
CREATE INDEX IF NOT EXISTS idx_social_category ON social_metrics(category);
CREATE INDEX IF NOT EXISTS idx_social_dates ON social_metrics(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_social_submitter ON social_metrics(submitted_by);

-- Add indexes to governance_metrics table
CREATE INDEX IF NOT EXISTS idx_gov_company ON governance_metrics(company_id);
CREATE INDEX IF NOT EXISTS idx_gov_status ON governance_metrics(status);
CREATE INDEX IF NOT EXISTS idx_gov_subtype ON governance_metrics(subtype);
CREATE INDEX IF NOT EXISTS idx_gov_category ON governance_metrics(category);
CREATE INDEX IF NOT EXISTS idx_gov_dates ON governance_metrics(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_gov_submitter ON governance_metrics(submitted_by);

-- Add indexes to notifications table
CREATE INDEX IF NOT EXISTS idx_notification_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notification_submission ON notifications(submission_id);
CREATE INDEX IF NOT EXISTS idx_notification_type ON notifications(type);
CREATE INDEX IF NOT EXISTS idx_notification_created ON notifications(created_at);

-- Create metric_categories table if it doesn't exist
CREATE TABLE IF NOT EXISTS metric_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    metric_type VARCHAR(50) NOT NULL,
    category_code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    display_order INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_metric_category UNIQUE (metric_type, category_code)
);

-- Add indexes to metric_categories table
CREATE INDEX IF NOT EXISTS idx_category_type ON metric_categories(metric_type);
CREATE INDEX IF NOT EXISTS idx_category_code ON metric_categories(category_code);

-- Insert default environment categories if they don't exist
INSERT INTO metric_categories (metric_type, category_code, name, description, display_order)
SELECT 'ENVIRONMENT', 'SCOPE_1', 'Scope 1', 'Direct emissions from owned or controlled sources', 1
WHERE NOT EXISTS (SELECT 1 FROM metric_categories WHERE metric_type = 'ENVIRONMENT' AND category_code = 'SCOPE_1');

INSERT INTO metric_categories (metric_type, category_code, name, description, display_order)
SELECT 'ENVIRONMENT', 'SCOPE_2', 'Scope 2', 'Indirect emissions from purchased electricity, steam, heating, and cooling', 2
WHERE NOT EXISTS (SELECT 1 FROM metric_categories WHERE metric_type = 'ENVIRONMENT' AND category_code = 'SCOPE_2');

INSERT INTO metric_categories (metric_type, category_code, name, description, display_order)
SELECT 'ENVIRONMENT', 'SCOPE_3', 'Scope 3', 'All other indirect emissions in a company''s value chain', 3
WHERE NOT EXISTS (SELECT 1 FROM metric_categories WHERE metric_type = 'ENVIRONMENT' AND category_code = 'SCOPE_3');

INSERT INTO metric_categories (metric_type, category_code, name, description, display_order)
SELECT 'ENVIRONMENT', 'SOLVENT', 'Solvent', 'Emissions from solvent use in industrial processes', 4
WHERE NOT EXISTS (SELECT 1 FROM metric_categories WHERE metric_type = 'ENVIRONMENT' AND category_code = 'SOLVENT');

INSERT INTO metric_categories (metric_type, category_code, name, description, display_order)
SELECT 'ENVIRONMENT', 'SINK', 'Sink', 'Carbon removal activities', 5
WHERE NOT EXISTS (SELECT 1 FROM metric_categories WHERE metric_type = 'ENVIRONMENT' AND category_code = 'SINK');

-- Insert default social categories if they don't exist
INSERT INTO metric_categories (metric_type, category_code, name, description, display_order)
SELECT 'SOCIAL', 'EMPLOYEE', 'Employee', 'Metrics related to employee welfare and development', 1
WHERE NOT EXISTS (SELECT 1 FROM metric_categories WHERE metric_type = 'SOCIAL' AND category_code = 'EMPLOYEE');

INSERT INTO metric_categories (metric_type, category_code, name, description, display_order)
SELECT 'SOCIAL', 'COMMUNITY', 'Community', 'Metrics related to community engagement and impact', 2
WHERE NOT EXISTS (SELECT 1 FROM metric_categories WHERE metric_type = 'SOCIAL' AND category_code = 'COMMUNITY');

INSERT INTO metric_categories (metric_type, category_code, name, description, display_order)
SELECT 'SOCIAL', 'SUPPLY_CHAIN', 'Supply Chain', 'Metrics related to supply chain management and ethics', 3
WHERE NOT EXISTS (SELECT 1 FROM metric_categories WHERE metric_type = 'SOCIAL' AND category_code = 'SUPPLY_CHAIN');

-- Insert default governance categories if they don't exist
INSERT INTO metric_categories (metric_type, category_code, name, description, display_order)
SELECT 'GOVERNANCE', 'CORPORATE', 'Corporate', 'Metrics related to corporate governance structure', 1
WHERE NOT EXISTS (SELECT 1 FROM metric_categories WHERE metric_type = 'GOVERNANCE' AND category_code = 'CORPORATE');

INSERT INTO metric_categories (metric_type, category_code, name, description, display_order)
SELECT 'GOVERNANCE', 'ETHICS', 'Ethics', 'Metrics related to business ethics and compliance', 2
WHERE NOT EXISTS (SELECT 1 FROM metric_categories WHERE metric_type = 'GOVERNANCE' AND category_code = 'ETHICS');

INSERT INTO metric_categories (metric_type, category_code, name, description, display_order)
SELECT 'GOVERNANCE', 'RISK', 'Risk', 'Metrics related to risk management', 3
WHERE NOT EXISTS (SELECT 1 FROM metric_categories WHERE metric_type = 'GOVERNANCE' AND category_code = 'RISK');
