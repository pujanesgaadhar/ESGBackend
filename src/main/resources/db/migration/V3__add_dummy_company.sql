INSERT INTO companies (name, description, industry, status, created_at)
VALUES ('Demo Company', 'A demo company for testing purposes', 'Technology', 'active', CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;
