-- V6__Insert_default_roles.sql
-- Insert default system roles
-- Compatible with both PostgreSQL and H2 databases

INSERT INTO roles (role_code, description, is_system_role)
VALUES 
    ('ADMIN', 'Administrator with full system access', TRUE),
    ('VERIFIER', 'Verifies property documents and agent licenses', TRUE),
    ('AGENT', 'Real estate agent', FALSE),
    ('OWNER', 'Property owner', FALSE),
    ('BUYER', 'Property buyer', FALSE),
    ('TENANT', 'Property tenant', FALSE);
