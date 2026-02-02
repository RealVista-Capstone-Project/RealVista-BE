-- V11__Insert_sample_users_with_roles.sql
-- Insert sample users with comprehensive role assignments for testing
-- Compatible with both PostgreSQL and H2 databases
-- Password for all users: 'Password123' (BCrypt encoded)

-- ============================================================================
-- BUSINESS RULES FOR USER ROLES
-- ============================================================================
-- 
-- 1. ADMIN Role:
--    - Single person with exclusive ADMIN role
--    - Cannot have other roles simultaneously
--    - Already exists in V7 (admin@realvista.com)
--    - System administrators with full access
--
-- 2. AGENT Role (40 users):
--    - Professional real estate agents
--    - Single role only (cannot have other roles)
--    - Can list properties for sale/rent
--    - Can manage client interactions
--    - Cannot be buyers or tenants
--
-- 3. VERIFIER Role (10 users):
--    - Property and document verification specialists
--    - Single role only (cannot have other roles)
--    - Can verify property information
--    - Can review agent listings
--    - Cannot be buyers or tenants
--
-- 4. Regular Users with BUYER + TENANT roles (20 users):
--    - Can purchase properties (BUYER role)
--    - Can rent properties (TENANT role)
--    - Exactly 2 roles, no more, no less
--    - Cannot manage properties as owner
--
-- 5. Owner Users with BUYER + TENANT + OWNER roles (40 users):
--    - Can sell properties they own (OWNER role)
--    - Can also buy other properties (BUYER role)
--    - Can rent properties to tenants (TENANT role)
--    - Exactly 3 roles - the most comprehensive role set
--    - Property management capabilities

-- ============================================================================
-- INSERT AGENTS (40 users) - Role: AGENT ONLY
-- ============================================================================

INSERT INTO users (user_id, first_name, last_name, business_name, password_hash, email, phone, status, created_at, updated_at, deleted)
VALUES 
('550e8400-e29b-41d4-a716-446655440101', 'Agent', 'One', 'Agent One Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent001@realvista.com', '+84901000101', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440102', 'Agent', 'Two', 'Agent Two Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent002@realvista.com', '+84901000102', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440103', 'Agent', 'Three', 'Agent Three Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent003@realvista.com', '+84901000103', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440104', 'Agent', 'Four', 'Agent Four Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent004@realvista.com', '+84901000104', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440105', 'Agent', 'Five', 'Agent Five Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent005@realvista.com', '+84901000105', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440106', 'Agent', 'Six', 'Agent Six Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent006@realvista.com', '+84901000106', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440107', 'Agent', 'Seven', 'Agent Seven Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent007@realvista.com', '+84901000107', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440108', 'Agent', 'Eight', 'Agent Eight Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent008@realvista.com', '+84901000108', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440109', 'Agent', 'Nine', 'Agent Nine Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent009@realvista.com', '+84901000109', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440110', 'Agent', 'Ten', 'Agent Ten Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent010@realvista.com', '+84901000110', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440111', 'Agent', 'Eleven', 'Agent Eleven Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent011@realvista.com', '+84901000111', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440112', 'Agent', 'Twelve', 'Agent Twelve Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent012@realvista.com', '+84901000112', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440113', 'Agent', 'Thirteen', 'Agent Thirteen Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent013@realvista.com', '+84901000113', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440114', 'Agent', 'Fourteen', 'Agent Fourteen Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent014@realvista.com', '+84901000114', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440115', 'Agent', 'Fifteen', 'Agent Fifteen Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent015@realvista.com', '+84901000115', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440116', 'Agent', 'Sixteen', 'Agent Sixteen Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent016@realvista.com', '+84901000116', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440117', 'Agent', 'Seventeen', 'Agent Seventeen Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent017@realvista.com', '+84901000117', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440118', 'Agent', 'Eighteen', 'Agent Eighteen Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent018@realvista.com', '+84901000118', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440119', 'Agent', 'Nineteen', 'Agent Nineteen Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent019@realvista.com', '+84901000119', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440120', 'Agent', 'Twenty', 'Agent Twenty Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent020@realvista.com', '+84901000120', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440121', 'Agent', 'TwentyOne', 'Agent TwentyOne Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent021@realvista.com', '+84901000121', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440122', 'Agent', 'TwentyTwo', 'Agent TwentyTwo Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent022@realvista.com', '+84901000122', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440123', 'Agent', 'TwentyThree', 'Agent TwentyThree Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent023@realvista.com', '+84901000123', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440124', 'Agent', 'TwentyFour', 'Agent TwentyFour Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent024@realvista.com', '+84901000124', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440125', 'Agent', 'TwentyFive', 'Agent TwentyFive Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent025@realvista.com', '+84901000125', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440126', 'Agent', 'TwentySix', 'Agent TwentySix Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent026@realvista.com', '+84901000126', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440127', 'Agent', 'TwentySeven', 'Agent TwentySeven Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent027@realvista.com', '+84901000127', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440128', 'Agent', 'TwentyEight', 'Agent TwentyEight Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent028@realvista.com', '+84901000128', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440129', 'Agent', 'TwentyNine', 'Agent TwentyNine Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent029@realvista.com', '+84901000129', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440130', 'Agent', 'Thirty', 'Agent Thirty Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent030@realvista.com', '+84901000130', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440131', 'Agent', 'ThirtyOne', 'Agent ThirtyOne Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent031@realvista.com', '+84901000131', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440132', 'Agent', 'ThirtyTwo', 'Agent ThirtyTwo Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent032@realvista.com', '+84901000132', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440133', 'Agent', 'ThirtyThree', 'Agent ThirtyThree Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent033@realvista.com', '+84901000133', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440134', 'Agent', 'ThirtyFour', 'Agent ThirtyFour Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent034@realvista.com', '+84901000134', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440135', 'Agent', 'ThirtyFive', 'Agent ThirtyFive Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent035@realvista.com', '+84901000135', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440136', 'Agent', 'ThirtySix', 'Agent ThirtySix Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent036@realvista.com', '+84901000136', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440137', 'Agent', 'ThirtySeven', 'Agent ThirtySeven Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent037@realvista.com', '+84901000137', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440138', 'Agent', 'ThirtyEight', 'Agent ThirtyEight Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent038@realvista.com', '+84901000138', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440139', 'Agent', 'ThirtyNine', 'Agent ThirtyNine Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent039@realvista.com', '+84901000139', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440140', 'Agent', 'Forty', 'Agent Forty Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'agent040@realvista.com', '+84901000140', 'ACTIVE', NOW(), NOW(), FALSE);

-- ============================================================================
-- INSERT VERIFIERS (10 users) - Role: VERIFIER ONLY
-- ============================================================================

INSERT INTO users (user_id, first_name, last_name, business_name, password_hash, email, phone, status, created_at, updated_at, deleted)
VALUES 
('550e8400-e29b-41d4-a716-446655440201', 'Verifier', 'One', 'Verifier One Team', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'verifier001@realvista.com', '+84901000201', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440202', 'Verifier', 'Two', 'Verifier Two Team', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'verifier002@realvista.com', '+84901000202', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440203', 'Verifier', 'Three', 'Verifier Three Team', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'verifier003@realvista.com', '+84901000203', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440204', 'Verifier', 'Four', 'Verifier Four Team', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'verifier004@realvista.com', '+84901000204', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440205', 'Verifier', 'Five', 'Verifier Five Team', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'verifier005@realvista.com', '+84901000205', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440206', 'Verifier', 'Six', 'Verifier Six Team', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'verifier006@realvista.com', '+84901000206', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440207', 'Verifier', 'Seven', 'Verifier Seven Team', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'verifier007@realvista.com', '+84901000207', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440208', 'Verifier', 'Eight', 'Verifier Eight Team', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'verifier008@realvista.com', '+84901000208', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440209', 'Verifier', 'Nine', 'Verifier Nine Team', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'verifier009@realvista.com', '+84901000209', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440210', 'Verifier', 'Ten', 'Verifier Ten Team', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'verifier010@realvista.com', '+84901000210', 'ACTIVE', NOW(), NOW(), FALSE);

-- ============================================================================
-- INSERT REGULAR USERS (20 users) - Roles: BUYER + TENANT (2 roles)
-- ============================================================================

INSERT INTO users (user_id, first_name, last_name, business_name, password_hash, email, phone, status, created_at, updated_at, deleted)
VALUES 
('550e8400-e29b-41d4-a716-446655440301', 'Buyer', 'Tenant', 'BT User One', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser001@realvista.com', '+84901000301', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440302', 'Buyer', 'Tenant', 'BT User Two', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser002@realvista.com', '+84901000302', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440303', 'Buyer', 'Tenant', 'BT User Three', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser003@realvista.com', '+84901000303', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440304', 'Buyer', 'Tenant', 'BT User Four', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser004@realvista.com', '+84901000304', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440305', 'Buyer', 'Tenant', 'BT User Five', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser005@realvista.com', '+84901000305', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440306', 'Buyer', 'Tenant', 'BT User Six', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser006@realvista.com', '+84901000306', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440307', 'Buyer', 'Tenant', 'BT User Seven', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser007@realvista.com', '+84901000307', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440308', 'Buyer', 'Tenant', 'BT User Eight', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser008@realvista.com', '+84901000308', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440309', 'Buyer', 'Tenant', 'BT User Nine', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser009@realvista.com', '+84901000309', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440310', 'Buyer', 'Tenant', 'BT User Ten', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser010@realvista.com', '+84901000310', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440311', 'Buyer', 'Tenant', 'BT User Eleven', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser011@realvista.com', '+84901000311', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440312', 'Buyer', 'Tenant', 'BT User Twelve', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser012@realvista.com', '+84901000312', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440313', 'Buyer', 'Tenant', 'BT User Thirteen', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser013@realvista.com', '+84901000313', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440314', 'Buyer', 'Tenant', 'BT User Fourteen', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser014@realvista.com', '+84901000314', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440315', 'Buyer', 'Tenant', 'BT User Fifteen', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser015@realvista.com', '+84901000315', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440316', 'Buyer', 'Tenant', 'BT User Sixteen', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser016@realvista.com', '+84901000316', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440317', 'Buyer', 'Tenant', 'BT User Seventeen', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser017@realvista.com', '+84901000317', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440318', 'Buyer', 'Tenant', 'BT User Eighteen', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser018@realvista.com', '+84901000318', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440319', 'Buyer', 'Tenant', 'BT User Nineteen', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser019@realvista.com', '+84901000319', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440320', 'Buyer', 'Tenant', 'BT User Twenty', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'buyertenantuser020@realvista.com', '+84901000320', 'ACTIVE', NOW(), NOW(), FALSE);

-- ============================================================================
-- INSERT OWNER USERS (40 users) - Roles: BUYER + TENANT + OWNER (3 roles)
-- ============================================================================

INSERT INTO users (user_id, first_name, last_name, business_name, password_hash, email, phone, status, created_at, updated_at, deleted)
VALUES 
('550e8400-e29b-41d4-a716-446655440401', 'Owner', 'User', 'Owner One Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner001@realvista.com', '+84901000401', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440402', 'Owner', 'User', 'Owner Two Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner002@realvista.com', '+84901000402', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440403', 'Owner', 'User', 'Owner Three Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner003@realvista.com', '+84901000403', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440404', 'Owner', 'User', 'Owner Four Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner004@realvista.com', '+84901000404', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440405', 'Owner', 'User', 'Owner Five Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner005@realvista.com', '+84901000405', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440406', 'Owner', 'User', 'Owner Six Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner006@realvista.com', '+84901000406', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440407', 'Owner', 'User', 'Owner Seven Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner007@realvista.com', '+84901000407', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440408', 'Owner', 'User', 'Owner Eight Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner008@realvista.com', '+84901000408', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440409', 'Owner', 'User', 'Owner Nine Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner009@realvista.com', '+84901000409', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440410', 'Owner', 'User', 'Owner Ten Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner010@realvista.com', '+84901000410', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440411', 'Owner', 'User', 'Owner Eleven Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner011@realvista.com', '+84901000411', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440412', 'Owner', 'User', 'Owner Twelve Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner012@realvista.com', '+84901000412', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440413', 'Owner', 'User', 'Owner Thirteen Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner013@realvista.com', '+84901000413', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440414', 'Owner', 'User', 'Owner Fourteen Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner014@realvista.com', '+84901000414', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440415', 'Owner', 'User', 'Owner Fifteen Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner015@realvista.com', '+84901000415', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440416', 'Owner', 'User', 'Owner Sixteen Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner016@realvista.com', '+84901000416', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440417', 'Owner', 'User', 'Owner Seventeen Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner017@realvista.com', '+84901000417', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440418', 'Owner', 'User', 'Owner Eighteen Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner018@realvista.com', '+84901000418', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440419', 'Owner', 'User', 'Owner Nineteen Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner019@realvista.com', '+84901000419', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440420', 'Owner', 'User', 'Owner Twenty Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner020@realvista.com', '+84901000420', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440421', 'Owner', 'User', 'Owner TwentyOne Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner021@realvista.com', '+84901000421', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440422', 'Owner', 'User', 'Owner TwentyTwo Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner022@realvista.com', '+84901000422', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440423', 'Owner', 'User', 'Owner TwentyThree Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner023@realvista.com', '+84901000423', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440424', 'Owner', 'User', 'Owner TwentyFour Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner024@realvista.com', '+84901000424', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440425', 'Owner', 'User', 'Owner TwentyFive Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner025@realvista.com', '+84901000425', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440426', 'Owner', 'User', 'Owner TwentySix Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner026@realvista.com', '+84901000426', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440427', 'Owner', 'User', 'Owner TwentySeven Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner027@realvista.com', '+84901000427', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440428', 'Owner', 'User', 'Owner TwentyEight Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner028@realvista.com', '+84901000428', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440429', 'Owner', 'User', 'Owner TwentyNine Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner029@realvista.com', '+84901000429', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440430', 'Owner', 'User', 'Owner Thirty Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner030@realvista.com', '+84901000430', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440431', 'Owner', 'User', 'Owner ThirtyOne Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner031@realvista.com', '+84901000431', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440432', 'Owner', 'User', 'Owner ThirtyTwo Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner032@realvista.com', '+84901000432', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440433', 'Owner', 'User', 'Owner ThirtyThree Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner033@realvista.com', '+84901000433', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440434', 'Owner', 'User', 'Owner ThirtyFour Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner034@realvista.com', '+84901000434', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440435', 'Owner', 'User', 'Owner ThirtyFive Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner035@realvista.com', '+84901000435', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440436', 'Owner', 'User', 'Owner ThirtySix Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner036@realvista.com', '+84901000436', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440437', 'Owner', 'User', 'Owner ThirtySeven Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner037@realvista.com', '+84901000437', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440438', 'Owner', 'User', 'Owner ThirtyEight Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner038@realvista.com', '+84901000438', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440439', 'Owner', 'User', 'Owner ThirtyNine Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner039@realvista.com', '+84901000439', 'ACTIVE', NOW(), NOW(), FALSE),
('550e8400-e29b-41d4-a716-446655440440', 'Owner', 'User', 'Owner Forty Properties', '$2a$12$X6MuTOu3YaJfl6m31ZY/4OuOxfKkrG1okySzhCq6Idk60hH6rGoK2', 'owner040@realvista.com', '+84901000440', 'ACTIVE', NOW(), NOW(), FALSE);

-- ============================================================================
-- ASSIGN ROLES TO AGENTS (40 users - AGENT role only)
-- ============================================================================

-- Using a CTE to assign AGENT role to all agent users
WITH agent_ids AS (
    SELECT user_id FROM users 
    WHERE email LIKE 'agent%@realvista.com'
)
INSERT INTO user_roles (user_id, role_id, assigned_at, created_at, updated_at, deleted)
SELECT 
    a.user_id,
    r.role_id,
    NOW(),
    NOW(),
    NOW(),
    FALSE
FROM agent_ids a, (SELECT role_id FROM roles WHERE role_code = 'AGENT') r;

-- ============================================================================
-- ASSIGN ROLES TO VERIFIERS (10 users - VERIFIER role only)
-- ============================================================================

WITH verifier_ids AS (
    SELECT user_id FROM users 
    WHERE email LIKE 'verifier%@realvista.com'
)
INSERT INTO user_roles (user_id, role_id, assigned_at, created_at, updated_at, deleted)
SELECT 
    v.user_id,
    r.role_id,
    NOW(),
    NOW(),
    NOW(),
    FALSE
FROM verifier_ids v, (SELECT role_id FROM roles WHERE role_code = 'VERIFIER') r;

-- ============================================================================
-- ASSIGN ROLES TO REGULAR USERS (20 users - BUYER + TENANT roles)
-- ============================================================================

WITH buyertenantuser_ids AS (
    SELECT user_id FROM users 
    WHERE email LIKE 'buyertenantuser%@realvista.com'
)
INSERT INTO user_roles (user_id, role_id, assigned_at, created_at, updated_at, deleted)
SELECT 
    b.user_id,
    r.role_id,
    NOW(),
    NOW(),
    NOW(),
    FALSE
FROM buyertenantuser_ids b, (SELECT role_id FROM roles WHERE role_code IN ('BUYER', 'TENANT')) r;

-- ============================================================================
-- ASSIGN ROLES TO OWNER USERS (40 users - BUYER + TENANT + OWNER roles)
-- ============================================================================

WITH owner_ids AS (
    SELECT user_id FROM users 
    WHERE email LIKE 'owner%@realvista.com'
)
INSERT INTO user_roles (user_id, role_id, assigned_at, created_at, updated_at, deleted)
SELECT 
    o.user_id,
    r.role_id,
    NOW(),
    NOW(),
    NOW(),
    FALSE
FROM owner_ids o, (SELECT role_id FROM roles WHERE role_code IN ('BUYER', 'TENANT', 'OWNER')) r;
