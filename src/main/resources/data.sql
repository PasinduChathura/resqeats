-- =====================================================
-- ResQeats Application - Initial Database Seed Data
-- Generated: 2026-01-03
-- =====================================================

-- =====================================================
-- ROLES
-- =====================================================
-- Table: roles (id, created_at, created_by, updated_at, updated_by, name, is_deprecated, description, label, metadata, type)
INSERT IGNORE INTO resqeats.roles (id, created_at, created_by, updated_at, updated_by, name, is_deprecated, description, label, metadata, type) VALUES 
('1', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'SuperAdmin', '0', 'Super Admin Role', 'SuperAdmin', null, 'SUPER_ADMIN'),
('2', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'Admin', '0', 'Admin Role', 'Admin', null, 'ADMIN'),
('3', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'ShopOwner', '0', 'Shop Owner Role', 'ShopOwner', null, 'SHOP_OWNER'),
('4', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'User', '0', 'User Role', 'User', null, 'USER');
INSERT IGNORE INTO resqeats.roles_seq VALUES (4);

-- =====================================================
-- PRIVILEGES  
-- =====================================================
-- Table: privilege (id, created_at, created_by, updated_at, updated_by, name, is_deprecated, description, label, metadata, type)
INSERT IGNORE INTO resqeats.privilege (id, created_at, created_by, updated_at, updated_by, name, is_deprecated, description, label, metadata, type) VALUES 
-- User Management Privileges
('1', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'USER_READ_PRIVILEGE', '0', 'Read user information', null, null, 'USER'),
('2', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'USER_WRITE_PRIVILEGE', '0', 'Create new users', null, null, 'USER'),
('3', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'USER_DELETE_PRIVILEGE', '0', 'Delete users', null, null, 'USER'),
('4', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'USER_UPDATE_PRIVILEGE', '0', 'Update user information', null, null, 'USER'),
('5', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'USER_ADMINISTRATION_PRIVILEGE', '0', 'Full user administration access', null, null, 'USER'),

-- Workflow Privileges
('6', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'WORKFLOW_READ_PRIVILEGE', '0', 'Read workflow information', null, null, 'WORKFLOW'),
('7', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'WORKFLOW_WRITE_PRIVILEGE', '0', 'Create workflows', null, null, 'WORKFLOW'),
('8', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'WORKFLOW_UPDATE_PRIVILEGE', '0', 'Update workflows', null, null, 'WORKFLOW'),
('9', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'WORKFLOW_DELETE_PRIVILEGE', '0', 'Delete workflows', null, null, 'WORKFLOW'),
('10', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'WORKFLOW_ADMINISTRATION_PRIVILEGE', '0', 'Full workflow administration', null, null, 'WORKFLOW'),

-- Role Privileges
('11', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'ROLE_READ_PRIVILEGE', '0', 'Read role information', null, null, 'ROLE'),
('12', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'ROLE_WRITE_PRIVILEGE', '0', 'Create roles', null, null, 'ROLE'),
('13', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'ROLE_UPDATE_PRIVILEGE', '0', 'Update roles', null, null, 'ROLE'),
('14', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'ROLE_DELETE_PRIVILEGE', '0', 'Delete roles', null, null, 'ROLE'),
('15', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'ROLE_ADMINISTRATION_PRIVILEGE', '0', 'Full role administration', null, null, 'ROLE'),

-- Shop Management Privileges
('16', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'SHOP_READ_PRIVILEGE', '0', 'View shop information', null, null, 'SHOP'),
('17', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'SHOP_WRITE_PRIVILEGE', '0', 'Create new shops', null, null, 'SHOP'),
('18', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'SHOP_UPDATE_PRIVILEGE', '0', 'Update shop information', null, null, 'SHOP'),
('19', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'SHOP_DELETE_PRIVILEGE', '0', 'Delete shops', null, null, 'SHOP'),
('20', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'SHOP_APPROVE_PRIVILEGE', '0', 'Approve/reject shops', null, null, 'SHOP'),
('21', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'SHOP_ADMINISTRATION_PRIVILEGE', '0', 'Full shop administration', null, null, 'SHOP'),

-- Food Item Privileges
('22', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'FOOD_READ_PRIVILEGE', '0', 'View food items', null, null, 'FOOD'),
('23', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'FOOD_WRITE_PRIVILEGE', '0', 'Create food items', null, null, 'FOOD'),
('24', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'FOOD_UPDATE_PRIVILEGE', '0', 'Update food items', null, null, 'FOOD'),
('25', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'FOOD_DELETE_PRIVILEGE', '0', 'Delete food items', null, null, 'FOOD'),
('26', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'FOOD_ADMINISTRATION_PRIVILEGE', '0', 'Full food item administration', null, null, 'FOOD'),

-- Order Privileges
('27', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'ORDER_READ_PRIVILEGE', '0', 'View orders', null, null, 'ORDER'),
('28', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'ORDER_WRITE_PRIVILEGE', '0', 'Create orders', null, null, 'ORDER'),
('29', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'ORDER_UPDATE_PRIVILEGE', '0', 'Update order status', null, null, 'ORDER'),
('30', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'ORDER_CANCEL_PRIVILEGE', '0', 'Cancel orders', null, null, 'ORDER'),
('31', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'ORDER_ADMINISTRATION_PRIVILEGE', '0', 'Full order administration', null, null, 'ORDER'),

-- Cart Privileges
('32', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'CART_READ_PRIVILEGE', '0', 'View cart', null, null, 'CART'),
('33', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'CART_WRITE_PRIVILEGE', '0', 'Add to cart', null, null, 'CART'),
('34', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'CART_UPDATE_PRIVILEGE', '0', 'Update cart items', null, null, 'CART'),
('35', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'CART_DELETE_PRIVILEGE', '0', 'Remove from cart', null, null, 'CART'),

-- Payment Privileges
('36', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'PAYMENT_READ_PRIVILEGE', '0', 'View payment information', null, null, 'PAYMENT'),
('37', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'PAYMENT_WRITE_PRIVILEGE', '0', 'Process payments', null, null, 'PAYMENT'),
('38', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'PAYMENT_ADMINISTRATION_PRIVILEGE', '0', 'Full payment administration', null, null, 'PAYMENT'),

-- Notification Privileges
('39', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'NOTIFICATION_READ_PRIVILEGE', '0', 'View notifications', null, null, 'NOTIFICATION'),
('40', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'NOTIFICATION_WRITE_PRIVILEGE', '0', 'Create notifications', null, null, 'NOTIFICATION'),
('41', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'NOTIFICATION_UPDATE_PRIVILEGE', '0', 'Update notifications', null, null, 'NOTIFICATION'),

-- Customer Privileges
('42', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'CUSTOMER_READ_PRIVILEGE', '0', 'View customer information', null, null, 'CUSTOMER'),
('43', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'CUSTOMER_WRITE_PRIVILEGE', '0', 'Create customers', null, null, 'CUSTOMER'),
('44', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'CUSTOMER_UPDATE_PRIVILEGE', '0', 'Update customer information', null, null, 'CUSTOMER'),
('45', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'CUSTOMER_DELETE_PRIVILEGE', '0', 'Delete customers', null, null, 'CUSTOMER'),
('46', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'CUSTOMER_ADMINISTRATION_PRIVILEGE', '0', 'Full customer administration', null, null, 'CUSTOMER');

INSERT IGNORE INTO resqeats.privilege_seq VALUES (46);

-- =====================================================
-- ROLE PRIVILEGES MAPPING
-- =====================================================
-- SUPER_ADMIN (ID: 1) - All privileges
INSERT IGNORE INTO resqeats.roles_privileges VALUES 
(1,1),(1,2),(1,3),(1,4),(1,5),     -- User management
(1,6),(1,7),(1,8),(1,9),(1,10),    -- Workflow
(1,11),(1,12),(1,13),(1,14),(1,15), -- Role management
(1,16),(1,17),(1,18),(1,19),(1,20),(1,21), -- Shop management
(1,22),(1,23),(1,24),(1,25),(1,26), -- Food items
(1,27),(1,28),(1,29),(1,30),(1,31), -- Orders
(1,32),(1,33),(1,34),(1,35),       -- Cart
(1,36),(1,37),(1,38),              -- Payment
(1,39),(1,40),(1,41),              -- Notifications
(1,42),(1,43),(1,44),(1,45),(1,46); -- Customer management

-- ADMIN (ID: 2) - High-level management privileges
INSERT IGNORE INTO resqeats.roles_privileges VALUES 
(2,1),(2,4),(2,5),                 -- User read/update/admin
(2,6),(2,8),(2,10),                -- Workflow read/update/admin
(2,11),(2,13),(2,15),              -- Role read/update/admin
(2,16),(2,18),(2,20),(2,21),       -- Shop read/update/approve/admin
(2,22),(2,24),(2,26),              -- Food read/update/admin
(2,27),(2,29),(2,31),              -- Order read/update/admin
(2,36),(2,38),                     -- Payment read/admin
(2,39),(2,41),                     -- Notification read/update
(2,42),(2,44),(2,46);              -- Customer read/update/admin

-- SHOP_OWNER (ID: 3) - Shop and food management
INSERT IGNORE INTO resqeats.roles_privileges VALUES 
(3,1),                             -- User read (basic profile)
(3,16),(3,17),(3,18),              -- Shop read/write/update (own shop)
(3,22),(3,23),(3,24),(3,25),       -- Food full CRUD
(3,27),(3,28),(3,29),              -- Order read/create/update
(3,32),                            -- Cart read
(3,36),                            -- Payment read
(3,39),(3,40),                     -- Notification read/write
(3,42);                            -- Customer read

-- USER (ID: 4) - Customer operations
INSERT IGNORE INTO resqeats.roles_privileges VALUES 
(4,1),                             -- User read (own profile)
(4,16),                            -- Shop read
(4,22),                            -- Food read
(4,27),(4,28),(4,30),              -- Order read/create/cancel
(4,32),(4,33),(4,34),(4,35),       -- Cart full operations
(4,36),(4,37),                     -- Payment read/write
(4,39),(4,40),                     -- Notification read/write
(4,42),(4,43),(4,44);              -- Customer read/write/update

-- =====================================================
-- USERS
-- =====================================================
-- Table: users (id, created_at, created_by, updated_at, updated_by, address, email, fax, first_name, last_name, password, phone, status, type, user_name, role_id)
-- Super Admin User (Password: Test@123)
INSERT IGNORE INTO resqeats.users (id, created_at, created_by, updated_at, updated_by, address, email, fax, first_name, last_name, password, phone, status, type, user_name, role_id) VALUES 
('1', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', null, 'chathura.pasindu@gmail.com', null, 'Super', 'Admin', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', null, 'ACTIVE', 'SUPER_ADMIN', 'superadmin', '1');

-- Admin User (Password: Test@123)
INSERT IGNORE INTO resqeats.users (id, created_at, created_by, updated_at, updated_by, address, email, fax, first_name, last_name, password, phone, status, type, user_name, role_id) VALUES 
('2', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', null, 'admin@resqeats.com', null, 'Admin', 'User', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', null, 'ACTIVE', 'ADMIN', 'admin', '2');

-- Shop Owner User (Password: Test@123)
INSERT IGNORE INTO resqeats.users (id, created_at, created_by, updated_at, updated_by, address, email, fax, first_name, last_name, password, phone, status, type, user_name, role_id) VALUES 
('3', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', null, 'owner@bakery.com', null, 'John', 'Bakery', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', null, 'ACTIVE', 'SHOP_OWNER', 'shopowner', '3');

-- Regular User 1 (Password: Test@123)
INSERT IGNORE INTO resqeats.users (id, created_at, created_by, updated_at, updated_by, address, email, fax, first_name, last_name, password, phone, status, type, user_name, role_id) VALUES 
('4', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', null, 'user1@example.com', null, 'Jane', 'Doe', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', null, 'ACTIVE', 'USER', 'user1', '4');

-- Regular User 2 (Password: Test@123)
INSERT IGNORE INTO resqeats.users (id, created_at, created_by, updated_at, updated_by, address, email, fax, first_name, last_name, password, phone, status, type, user_name, role_id) VALUES 
('5', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', null, 'user2@example.com', null, 'Bob', 'Smith', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', null, 'ACTIVE', 'USER', 'user2', '4');

INSERT IGNORE INTO resqeats.users_seq VALUES (6);

-- =====================================================
-- WORKFLOW SUB TYPES
-- =====================================================
-- Table: workflow_sub_type (id, created_at, created_by, updated_at, updated_by, name, is_deprecated, description, label, metadata, type)
INSERT IGNORE INTO resqeats.workflow_sub_type (id, created_at, created_by, updated_at, updated_by, name, is_deprecated, description, label, metadata, type) VALUES 
('1', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'Order Processing', '0', NULL, NULL, NULL, 'SERVICE_REQUEST'),
('2', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'Shop Approval', '0', NULL, NULL, NULL, 'SERVICE_REQUEST'),
('3', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'Payment Processing', '0', NULL, NULL, NULL, 'SERVICE_REQUEST');

-- =====================================================
-- SHOPS
-- =====================================================
-- Table: shops (id, created_at, created_by, updated_at, updated_by, address, average_rating, category, city, closing_time, description, email, image_url, is_open, latitude, longitude, name, opening_time, phone, pickup_end_time, pickup_start_time, postal_code, status, total_ratings, owner_id)
INSERT IGNORE INTO resqeats.shops (id, created_at, created_by, updated_at, updated_by, address, average_rating, category, city, closing_time, description, email, image_url, is_open, latitude, longitude, name, opening_time, phone, pickup_end_time, pickup_start_time, postal_code, status, total_ratings, owner_id) VALUES 
('1', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '123 Main Street', 4.5, 'BAKERY', 'Colombo', '20:00:00', 'Delicious freshly baked goods daily', 'info@freshbakery.com', 'https://example.com/bakery.jpg', 0, 6.92707080, 79.86124300, 'The Fresh Bakery', '07:00:00', '+94112345678', '19:30:00', '17:00:00', '00100', 'APPROVED', 120, 3);

INSERT IGNORE INTO resqeats.shops (id, created_at, created_by, updated_at, updated_by, address, average_rating, category, city, closing_time, description, email, image_url, is_open, latitude, longitude, name, opening_time, phone, pickup_end_time, pickup_start_time, postal_code, status, total_ratings, owner_id) VALUES 
('2', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '456 Park Avenue', 4.2, 'GROCERY', 'Colombo', '18:00:00', 'Fresh fruits and vegetables', 'info@greengrocers.com', 'https://example.com/grocery.jpg', 0, 6.91550700, 79.87370900, 'Green Grocers', '08:00:00', '+94112345679', '17:30:00', '16:00:00', '00300', 'APPROVED', 85, 3);

INSERT IGNORE INTO resqeats.shops (id, created_at, created_by, updated_at, updated_by, address, average_rating, category, city, closing_time, description, email, image_url, is_open, latitude, longitude, name, opening_time, phone, pickup_end_time, pickup_start_time, postal_code, status, total_ratings, owner_id) VALUES 
('3', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '789 Galle Road', 4.7, 'RESTAURANT', 'Colombo', '22:00:00', 'Authentic Sri Lankan cuisine', 'info@tastybites.com', 'https://example.com/restaurant.jpg', 0, 6.89568700, 79.85723400, 'Tasty Bites Restaurant', '10:00:00', '+94112345680', '21:30:00', '20:00:00', '00400', 'APPROVED', 200, 3);

INSERT IGNORE INTO resqeats.shops_seq VALUES (3);

-- =====================================================
-- SHOP OPERATING DAYS
-- =====================================================
-- Table: shop_operating_days (id, created_at, created_by, updated_at, updated_by, closing_time, day_of_week, is_closed, opening_time, pickup_end_time, pickup_start_time, shop_id)
INSERT IGNORE INTO resqeats.shop_operating_days (id, created_at, created_by, updated_at, updated_by, closing_time, day_of_week, is_closed, opening_time, pickup_end_time, pickup_start_time, shop_id) VALUES 
('1', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '20:00:00', 'MONDAY', 0, '07:00:00', '19:30:00', '17:00:00', 1),
('2', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '20:00:00', 'TUESDAY', 0, '07:00:00', '19:30:00', '17:00:00', 1),
('3', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '20:00:00', 'WEDNESDAY', 0, '07:00:00', '19:30:00', '17:00:00', 1),
('4', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '20:00:00', 'THURSDAY', 0, '07:00:00', '19:30:00', '17:00:00', 1),
('5', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '20:00:00', 'FRIDAY', 0, '07:00:00', '19:30:00', '17:00:00', 1),
('6', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '20:00:00', 'SATURDAY', 0, '07:00:00', '19:30:00', '17:00:00', 1),
('7', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '18:00:00', 'MONDAY', 0, '08:00:00', '17:30:00', '16:00:00', 2),
('8', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '18:00:00', 'TUESDAY', 0, '08:00:00', '17:30:00', '16:00:00', 2),
('9', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '18:00:00', 'WEDNESDAY', 0, '08:00:00', '17:30:00', '16:00:00', 2),
('10', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '18:00:00', 'THURSDAY', 0, '08:00:00', '17:30:00', '16:00:00', 2),
('11', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '18:00:00', 'FRIDAY', 0, '08:00:00', '17:30:00', '16:00:00', 2),
('12', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '18:00:00', 'SATURDAY', 0, '08:00:00', '17:30:00', '16:00:00', 2),
('13', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '18:00:00', 'SUNDAY', 0, '08:00:00', '17:30:00', '16:00:00', 2),
('14', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '22:00:00', 'MONDAY', 0, '10:00:00', '21:30:00', '20:00:00', 3),
('15', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '22:00:00', 'TUESDAY', 0, '10:00:00', '21:30:00', '20:00:00', 3),
('16', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '22:00:00', 'WEDNESDAY', 0, '10:00:00', '21:30:00', '20:00:00', 3),
('17', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '22:00:00', 'THURSDAY', 0, '10:00:00', '21:30:00', '20:00:00', 3),
('18', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '22:00:00', 'FRIDAY', 0, '10:00:00', '21:30:00', '20:00:00', 3),
('19', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '22:00:00', 'SATURDAY', 0, '10:00:00', '21:30:00', '20:00:00', 3),
('20', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '22:00:00', 'SUNDAY', 0, '10:00:00', '21:30:00', '20:00:00', 3);

-- =====================================================
-- FOOD ITEMS
-- =====================================================
-- Table: food_items (id, created_at, created_by, updated_at, updated_by, allergens, category, description, dietary_info, expiry_date, image_url, is_active, is_gluten_free, is_vegan, is_vegetarian, name, original_price, shop_id)
INSERT IGNORE INTO resqeats.food_items (id, created_at, created_by, updated_at, updated_by, allergens, category, description, dietary_info, expiry_date, image_url, is_active, is_gluten_free, is_vegan, is_vegetarian, name, original_price, shop_id) VALUES 
('1', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'Wheat, Eggs, Milk', 'PASTRY', 'Buttery French pastry', 'Contains gluten', '2026-01-04 10:00:00', 'https://example.com/croissant.jpg', 1, 0, 0, 0, 'Croissant', 250.00, 1);

INSERT IGNORE INTO resqeats.food_items (id, created_at, created_by, updated_at, updated_by, allergens, category, description, dietary_info, expiry_date, image_url, is_active, is_gluten_free, is_vegan, is_vegetarian, name, original_price, shop_id) VALUES 
('2', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'Wheat', 'BREAD', 'Healthy whole wheat bread loaf', 'Contains gluten', '2026-01-04 10:00:00', 'https://example.com/bread.jpg', 1, 0, 1, 0, 'Whole Wheat Bread', 180.00, 1);

INSERT IGNORE INTO resqeats.food_items (id, created_at, created_by, updated_at, updated_by, allergens, category, description, dietary_info, expiry_date, image_url, is_active, is_gluten_free, is_vegan, is_vegetarian, name, original_price, shop_id) VALUES 
('3', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', null, 'FRUITS', 'Fresh organic apples (1kg)', 'Organic', '2026-01-05 10:00:00', 'https://example.com/apples.jpg', 1, 1, 1, 1, 'Organic Apples', 350.00, 2);

INSERT IGNORE INTO resqeats.food_items (id, created_at, created_by, updated_at, updated_by, allergens, category, description, dietary_info, expiry_date, image_url, is_active, is_gluten_free, is_vegan, is_vegetarian, name, original_price, shop_id) VALUES 
('4', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', null, 'PREPARED_MEAL', 'Traditional Sri Lankan rice and curry meal', 'Spicy', '2026-01-03 20:00:00', 'https://example.com/ricecurry.jpg', 1, 0, 0, 0, 'Rice & Curry', 450.00, 3);

INSERT IGNORE INTO resqeats.food_items (id, created_at, created_by, updated_at, updated_by, allergens, category, description, dietary_info, expiry_date, image_url, is_active, is_gluten_free, is_vegan, is_vegetarian, name, original_price, shop_id) VALUES 
('5', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', null, 'SALAD', 'Fresh mixed vegetable salad', 'Low calorie', '2026-01-04 10:00:00', 'https://example.com/salad.jpg', 1, 1, 1, 1, 'Vegetable Salad', 320.00, 3);

INSERT IGNORE INTO resqeats.food_items_seq VALUES (5);

-- =====================================================
-- SECRET BOXES
-- =====================================================
-- Table: secret_boxes (id, created_at, created_by, updated_at, updated_by, available_date, cutoff_time, description, discount_percentage, discounted_price, expiry_time, image_url, is_active, is_visible, may_contain, name, original_value, pickup_end_time, pickup_start_time, quantity_available, total_quantity, shop_id)
INSERT IGNORE INTO resqeats.secret_boxes (id, created_at, created_by, updated_at, updated_by, available_date, cutoff_time, description, discount_percentage, discounted_price, expiry_time, image_url, is_active, is_visible, may_contain, name, original_value, pickup_end_time, pickup_start_time, quantity_available, total_quantity, shop_id) VALUES 
('1', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', '16:00:00', 'Assorted pastries and bread items', 62.50, 300.00, '2026-01-03 19:30:00', 'https://example.com/surprisebox.jpg', 1, 1, 'Nuts, Gluten', 'Bakery Surprise Box', 800.00, '19:30:00', '17:00:00', 5, 10, 1);

INSERT IGNORE INTO resqeats.secret_boxes (id, created_at, created_by, updated_at, updated_by, available_date, cutoff_time, description, discount_percentage, discounted_price, expiry_time, image_url, is_active, is_visible, may_contain, name, original_value, pickup_end_time, pickup_start_time, quantity_available, total_quantity, shop_id) VALUES 
('2', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', '15:00:00', 'Mixed fresh fruits and vegetables', 58.33, 250.00, '2026-01-03 17:30:00', 'https://example.com/producebox.jpg', 1, 1, null, 'Fresh Produce Box', 600.00, '17:30:00', '16:00:00', 8, 15, 2);

INSERT IGNORE INTO resqeats.secret_boxes (id, created_at, created_by, updated_at, updated_by, available_date, cutoff_time, description, discount_percentage, discounted_price, expiry_time, image_url, is_active, is_visible, may_contain, name, original_value, pickup_end_time, pickup_start_time, quantity_available, total_quantity, shop_id) VALUES 
('3', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', '19:00:00', 'Chef special prepared meals', 62.50, 450.00, '2026-01-03 21:30:00', 'https://example.com/specialbox.jpg', 1, 1, 'Seafood, Dairy', 'Restaurant Special Box', 1200.00, '21:30:00', '20:00:00', 3, 8, 3);

INSERT IGNORE INTO resqeats.secret_boxes_seq VALUES (3);

-- =====================================================
-- SECRET BOX ITEMS
-- =====================================================
-- Table: secret_box_items (id, created_at, created_by, updated_at, updated_by, notes, quantity, food_item_id, secret_box_id)
INSERT IGNORE INTO resqeats.secret_box_items (id, created_at, created_by, updated_at, updated_by, notes, quantity, food_item_id, secret_box_id) VALUES 
('1', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', null, 3, 1, 1),
('2', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', null, 2, 2, 1),
('3', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', null, 5, 3, 2),
('4', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', null, 2, 4, 3),
('5', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', null, 1, 5, 3);

-- =====================================================
-- CUSTOMERS
-- =====================================================
-- Table: customer (id, created_at, created_by, updated_at, updated_by, address, district, email, fax, join_date, latitude, longitude, name, person, person_designation, phone, province, ref_id, status, type, parent_id)
INSERT IGNORE INTO resqeats.customer (id, created_at, created_by, updated_at, updated_by, address, district, email, fax, join_date, latitude, longitude, name, person, person_designation, phone, province, ref_id, status, type, parent_id) VALUES 
('1', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '123 Business Street', 'Colombo', 'abc@example.com', '0112234567', '2026-01-01 10:00:00', 6.92707080, 79.86124300, 'ABC Company', 'John Manager', 'General Manager', '0771234567', 'Western', 'CUST001', 'ACTIVE', 'PARENT', null);

INSERT IGNORE INTO resqeats.customer (id, created_at, created_by, updated_at, updated_by, address, district, email, fax, join_date, latitude, longitude, name, person, person_designation, phone, province, ref_id, status, type, parent_id) VALUES 
('2', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '456 Corporate Avenue', 'Kandy', 'xyz@example.com', '0812234567', '2026-01-01 10:00:00', 7.29062920, 80.63388160, 'XYZ Corporation', 'Jane Director', 'Director', '0772234567', 'Central', 'CUST002', 'ACTIVE', 'PARENT', null);

INSERT IGNORE INTO resqeats.customer_seq VALUES (2);

-- =====================================================
-- CARTS
-- =====================================================
-- Table: carts (id, created_at, created_by, updated_at, updated_by, expires_at, status, total_amount, total_items, user_id)
INSERT IGNORE INTO resqeats.carts (id, created_at, created_by, updated_at, updated_by, expires_at, status, total_amount, total_items, user_id) VALUES 
('1', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '2026-01-04 10:00:00', 'ACTIVE', 300.00, 1, 4);

INSERT IGNORE INTO resqeats.carts (id, created_at, created_by, updated_at, updated_by, expires_at, status, total_amount, total_items, user_id) VALUES 
('2', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '2026-01-04 10:00:00', 'ACTIVE', 450.00, 1, 5);

INSERT IGNORE INTO resqeats.carts_seq VALUES (2);

-- =====================================================
-- CART ITEMS
-- =====================================================
-- Table: cart_items (id, created_at, created_by, updated_at, updated_by, quantity, total_price, unit_price, cart_id, secret_box_id, shop_id)
INSERT IGNORE INTO resqeats.cart_items (id, created_at, created_by, updated_at, updated_by, quantity, total_price, unit_price, cart_id, secret_box_id, shop_id) VALUES 
('1', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 1, 300.00, 300.00, 1, 1, 1);

INSERT IGNORE INTO resqeats.cart_items (id, created_at, created_by, updated_at, updated_by, quantity, total_price, unit_price, cart_id, secret_box_id, shop_id) VALUES 
('2', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 1, 450.00, 450.00, 2, 3, 3);

INSERT IGNORE INTO resqeats.cart_items_seq VALUES (2);

-- =====================================================
-- ORDERS
-- =====================================================
-- Table: orders (id, created_at, created_by, updated_at, updated_by, accepted_at, cancelled_at, cancelled_reason, completed_at, estimated_ready_at, feedback_rating, feedback_text, latitude, longitude, order_number, pickup_code, pickup_deadline, pickup_end_time, pickup_start_time, ready_at, service_fee, status, subtotal, total_amount, shop_id, user_id)
INSERT IGNORE INTO resqeats.orders (id, created_at, created_by, updated_at, updated_by, accepted_at, cancelled_at, cancelled_reason, completed_at, estimated_ready_at, feedback_rating, feedback_text, latitude, longitude, order_number, pickup_code, pickup_deadline, pickup_end_time, pickup_start_time, ready_at, service_fee, status, subtotal, total_amount, shop_id, user_id) VALUES 
('1', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '2026-01-03 11:00:00', null, null, '2026-01-03 18:30:00', null, null, 'Thank you for saving food!', null, null, 'ORD-20260103-001', 'PICK123', '2026-01-03 19:30:00', '19:30:00', '17:00:00', '2026-01-03 18:00:00', 30.00, 'COMPLETED', 300.00, 330.00, 1, 4);

INSERT IGNORE INTO resqeats.orders_seq VALUES (1);

-- =====================================================
-- ORDER ITEMS
-- =====================================================
-- Table: order_items (id, created_at, created_by, updated_at, updated_by, expiry_time, pickup_time, quantity, total_price, unit_price, order_id, secret_box_id, shop_id)
INSERT IGNORE INTO resqeats.order_items (id, created_at, created_by, updated_at, updated_by, expiry_time, pickup_time, quantity, total_price, unit_price, order_id, secret_box_id, shop_id) VALUES 
('1', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '2026-01-03 19:30:00', null, 1, 300.00, 300.00, 1, 1, 1);

INSERT IGNORE INTO resqeats.order_items_seq VALUES (1);

-- =====================================================
-- PAYMENTS
-- =====================================================
-- Table: payments (id, created_at, created_by, updated_at, updated_by, amount, authorized_at, capture_transaction_id, captured_at, currency, gateway_response, payment_method, pre_auth_transaction_id, refund_transaction_id, refunded_at, released_at, status, order_id, payment_method_id)
INSERT IGNORE INTO resqeats.payments (id, created_at, created_by, updated_at, updated_by, amount, authorized_at, capture_transaction_id, captured_at, currency, gateway_response, payment_method, pre_auth_transaction_id, refund_transaction_id, refunded_at, released_at, status, order_id, payment_method_id) VALUES 
('1', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 330.00, '2026-01-03 10:30:00', 'CAPTURE-456', '2026-01-03 18:30:00', 'LKR', '{"status":"success","gateway":"stripe"}', 'CARD', 'PRE-AUTH-123', null, null, null, 'COMPLETED', 1, null);

INSERT IGNORE INTO resqeats.payments_seq VALUES (1);

-- =====================================================
-- USER PAYMENT METHODS
-- =====================================================
-- Table: user_payment_methods (id, created_at, created_by, updated_at, updated_by, display_name, expiry_month, expiry_year, is_default, last_four_digits, payment_method, token, type, user_id)
INSERT IGNORE INTO resqeats.user_payment_methods (id, created_at, created_by, updated_at, updated_by, display_name, expiry_month, expiry_year, is_default, last_four_digits, payment_method, token, type, user_id) VALUES 
('1', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', 'Visa ending in 1234', '12', '2028', 1, '1234', null, 'tok_visa1234', 'CARD', 4);

INSERT IGNORE INTO resqeats.user_payment_methods_seq VALUES (1);

-- =====================================================
-- NOTIFICATIONS
-- =====================================================
-- Table: notifications (id, created_at, created_by, updated_at, updated_by, data, is_read, message, read_at, reference_id, reference_type, title, type, user_id)
INSERT IGNORE INTO resqeats.notifications (id, created_at, created_by, updated_at, updated_by, data, is_read, message, read_at, reference_id, reference_type, title, type, user_id) VALUES 
('1', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '{"orderId":1,"orderNumber":"ORD-20260103-001"}', 0, 'Your order ORD-20260103-001 has been confirmed', null, 1, 'ORDER', 'Order Confirmed', 'ORDER_CONFIRMED', 4);

INSERT IGNORE INTO resqeats.notifications (id, created_at, created_by, updated_at, updated_by, data, is_read, message, read_at, reference_id, reference_type, title, type, user_id) VALUES 
('2', '2026-01-03 10:00:00', 'system', '2026-01-03 10:00:00', 'system', '{"orderId":1,"pickupCode":"PICK123"}', 1, 'Your order is ready. Pickup code: PICK123', '2026-01-03 17:45:00', 1, 'ORDER', 'Order Ready for Pickup', 'ORDER_READY', 4);

INSERT IGNORE INTO resqeats.notifications_seq VALUES (2);

-- =====================================================
-- END OF DATA SEED FILE
-- =====================================================





