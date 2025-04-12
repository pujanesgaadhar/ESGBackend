-- Companies table
CREATE TABLE companies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    company_id INTEGER REFERENCES companies(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ESG Submissions table
CREATE TABLE esg_submissions (
    id SERIAL PRIMARY KEY,
    company_id INTEGER REFERENCES companies(id),
    submitted_by INTEGER REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create default admin user
INSERT INTO users (name, email, password, role)
VALUES ('Admin User', 'admin@example.com', '$2a$10$1MYWILRzUvQDD8.y.cSoa.c9mD4ITpJyviTeF5myDxkJd3wk44o66', 'ADMIN');
CREATE TABLE esg_submissions (
    id SERIAL PRIMARY KEY,
    company_id INTEGER REFERENCES companies(id),
    submitted_by INTEGER REFERENCES users(id),
    status submission_status DEFAULT 'PENDING',
    reviewed_by INTEGER REFERENCES users(id),
    reviewed_at TIMESTAMP,
    environmental_metrics JSONB,
    social_metrics JSONB,
    governance_metrics JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notifications table
CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_users_company ON users(company_id);
CREATE INDEX idx_submissions_company ON esg_submissions(company_id);
CREATE INDEX idx_submissions_status ON esg_submissions(status);
CREATE INDEX idx_notifications_user ON notifications(user_id);

-- Insert default admin user
INSERT INTO users (name, email, password, role)
VALUES (
    'Admin',
    'admin@esgframework.com',
    '$2a$10$xn3LI/AjqicFYZFruSwve.268HP.EijMEemxLWrCUKmsFpXOT.2lu', -- password: admin123
    'ADMIN'
);
