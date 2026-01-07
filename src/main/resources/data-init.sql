-- ====================================================================
-- RESQEATS FOOD RESCUE PLATFORM - INITIAL DATA MIGRATION (MySQL)
-- ====================================================================
-- This script sets up initial data for the Resqeats platform including:
-- - System configuration
-- - Admin users and merchants with proper roles (enum-based, not separate tables)
-- - Real merchants, outlets, and items in Sri Lanka
-- - Test data for development/testing
-- ====================================================================

-- ====================================================================
-- 1. CLEAR EXISTING DATA (for clean initialization) - DISABLED FOR SAFETY
-- ====================================================================
-- Note: Run this only in development/test environments if needed
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE notification_tokens;
-- TRUNCATE TABLE notifications;
-- TRUNCATE TABLE payments;
-- TRUNCATE TABLE order_items;
-- TRUNCATE TABLE orders;
-- TRUNCATE TABLE outlet_items;
-- TRUNCATE TABLE items;
-- TRUNCATE TABLE outlet_hours;
-- TRUNCATE TABLE outlets;
-- TRUNCATE TABLE merchants;
-- TRUNCATE TABLE refresh_tokens;
-- TRUNCATE TABLE users;
-- SET FOREIGN_KEY_CHECKS = 1;

-- ====================================================================
-- 2. ADMIN USERS
-- ====================================================================
-- Password: Admin@123! (BCrypt encoded)
INSERT IGNORE INTO users (id, email, password_hash, first_name, last_name, phone, role, status, email_verified, phone_verified, oauth2_provider, created_at, updated_at) VALUES
    ('f8a2b3c4-d5e6-4f7a-8b9c-0d1e2f3a4b5c',
     'admin@resqeats.lk',
     '$2a$10$N9qo8uLOickgx2ZMRZoMye.WXcSzA.IjqRzPOCwzBbxJNMQGxZ0Aq',
     'Kasun',
     'Perera',
     '+94771234500',
     'ADMIN',
     'ACTIVE',
     true,
     true,
     'LOCAL',
     NOW(),
     NOW()),
    ('a0a0a0a0-b1b1-c2c2-d3d3-e4e4e4e4e4e4',
     'superadmin@resqeats.lk',
     '$2a$10$N9qo8uLOickgx2ZMRZoMye.WXcSzA.IjqRzPOCwzBbxJNMQGxZ0Aq',
     'Super',
     'Administrator',
     '+94770000000',
     'SUPER_ADMIN',
     'ACTIVE',
     true,
     true,
     'LOCAL',
     NOW(),
     NOW());

-- ====================================================================
-- 4. SAMPLE CUSTOMERS
-- ====================================================================
-- Password: User@123! (BCrypt encoded)
INSERT IGNORE INTO users (id, email, password_hash, first_name, last_name, phone, role, status, email_verified, phone_verified, oauth2_provider, created_at, updated_at) VALUES
    ('1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d',
     'nimal.silva@gmail.com',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Nimal',
     'Silva',
     '+94771234567',
     'USER',
     'ACTIVE',
     true,
     true,
     'LOCAL',
     NOW(),
     NOW()),
    ('2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e',
     'kumari.fernando@yahoo.com',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Kumari',
     'Fernando',
     '+94777654321',
     'USER',
     'ACTIVE',
     true,
     false,
     'LOCAL',
     NOW(),
     NOW()),
    ('3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f',
     'ashan.jayawardena@gmail.com',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Ashan',
     'Jayawardena',
     '+94779876543',
     'USER',
     'ACTIVE',
     true,
     true,
     'GOOGLE',
     NOW(),
     NOW()),
    ('4d5e6f7a-8b9c-0d1e-2f3a-4b5c6d7e8f9a',
     'dilini.wickramasinghe@outlook.com',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Dilini',
     'Wickramasinghe',
     '+94765432198',
     'USER',
     'ACTIVE',
     true,
     true,
     'LOCAL',
     NOW(),
     NOW()),
    ('5e6f7a8b-9c0d-1e2f-3a4b-5c6d7e8f9a0b',
     'ruwan.bandara@gmail.com',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Ruwan',
     'Bandara',
     '+94712345678',
     'USER',
     'ACTIVE',
     true,
     true,
     'LOCAL',
     NOW(),
     NOW());

-- ====================================================================
-- 5. MERCHANT OWNER USERS
-- ====================================================================
INSERT IGNORE INTO users (id, email, password_hash, first_name, last_name, phone, role, status, email_verified, phone_verified, oauth2_provider, created_at, updated_at) VALUES
    ('6f7a8b9c-0d1e-2f3a-4b5c-6d7e8f9a0b1c',
     'dharshan@ministryofcrab.com',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Dharshan',
     'Munidasa',
     '+94112320707',
     'MERCHANT',
     'ACTIVE',
     true,
     true,
     'LOCAL',
     NOW(),
     NOW()),
    ('7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d',
     'niranjan@kefiscolombo.lk',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Niranjan',
     'David',
     '+94112055555',
     'MERCHANT',
     'ACTIVE',
     true,
     true,
     'LOCAL',
     NOW(),
     NOW()),
    ('8b9c0d1e-2f3a-4b5c-6d7e-8f9a0b1c2d3e',
     'management@cinnamonhotels.com',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Manori',
     'Unambuwe',
     '+94112491000',
     'MERCHANT',
     'ACTIVE',
     true,
     true,
     'LOCAL',
     NOW(),
     NOW()),
    ('9c0d1e2f-3a4b-5c6d-7e8f-9a0b1c2d3e4f',
     'info@pereraandsons.lk',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Damitha',
     'Perera',
     '+94112588855',
     'MERCHANT',
     'ACTIVE',
     true,
     true,
     'LOCAL',
     NOW(), 
     NOW()),
    ('0d1e2f3a-4b5c-6d7e-8f9a-0b1c2d3e4f5a',
     'contact@keells.lk',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Charith',
     'Senanayake',
     '+94112303500',
     'MERCHANT',
     'ACTIVE',
     true,
     true,
     'LOCAL',
     NOW(),
     NOW());

-- ====================================================================
-- 6. MERCHANTS
-- ====================================================================
INSERT IGNORE INTO merchants (id, owner_user_id, name, registration_no, category, description, contact_email, contact_phone, website, logo_url, status, created_at, updated_at) VALUES
    ('a7b8c9d0-e1f2-3a4b-5c6d-7e8f9a0b1c2d',
     '6f7a8b9c-0d1e-2f3a-4b5c-6d7e8f9a0b1c',
     'Ministry of Crab',
     'PV00123456',
     'RESTAURANT',
     'Award-winning seafood restaurant by celebrity chef Dharshan Munidasa, offering premium crab dishes and reducing food waste through discounted end-of-day specials.',
     'info@ministryofcrab.com',
     '+94112320707',
     'https://www.ministryofcrab.com',
     'https://resqeats.lk/logos/ministry-of-crab.png',
     'APPROVED',
     NOW(),
     NOW()),
    ('b8c9d0e1-f2a3-4b5c-6d7e-8f9a0b1c2d3e',
     '7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d',
     'Kefi Colombo',
     'PV00234567',
     'RESTAURANT',
     'Mediterranean and Greek cuisine restaurant committed to sustainability and reducing food waste through quality surplus meal offerings.',
     'hello@keficolombo.lk',
     '+94112055555',
     'https://www.keficolombo.lk',
     'https://resqeats.lk/logos/kefi-colombo.png',
     'APPROVED',
     NOW(),
     NOW()),
    ('c9d0e1f2-a3b4-5c6d-7e8f-9a0b1c2d3e4f',
     '8b9c0d1e-2f3a-4b5c-6d7e-8f9a0b1c2d3e',
     'Cinnamon Grand Colombo',
     'PV00345678',
     'HOTEL',
     'Five-star luxury hotel offering surplus buffet items and gourmet meals at discounted prices to minimize food waste.',
     'reservations@cinnamonhotels.com',
     '+94112491000',
     'https://www.cinnamonhotels.com/cinnamongrandcolombo',
     'https://resqeats.lk/logos/cinnamon-grand.png',
     'APPROVED',
     NOW(),
     NOW()),
    ('d0e1f2a3-b4c5-6d7e-8f9a-0b1c2d3e4f5a',
     '9c0d1e2f-3a4b-5c6d-7e8f-9a0b1c2d3e4f',
     'Perera & Sons',
     'PV00456789',
     'BAKERY',
     'Iconic Sri Lankan bakery chain offering fresh bread, pastries, short eats, and cakes with end-of-day discounts to reduce waste.',
     'info@pereraandsons.lk',
     '+94112588855',
     'https://www.pereraandsons.lk',
     'https://resqeats.lk/logos/perera-sons.png',
     'APPROVED',
     NOW(),
     NOW()),
    ('e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b',
     '0d1e2f3a-4b5c-6d7e-8f9a-0b1c2d3e4f5a',
     'Keells Super',
     'PV00567890',
     'GROCERY',
     'Leading supermarket chain in Sri Lanka offering fresh produce, dairy, and prepared foods with daily discounts on items approaching best-before dates.',
     'customercare@keells.com',
     '+94112303500',
     'https://www.keellssuper.com',
     'https://resqeats.lk/logos/keells-super.png',
     'APPROVED',
     NOW(),
     NOW()),
    ('f2a3b4c5-d6e7-8f9a-0b1c-2d3e4f5a6b7c',
     '6f7a8b9c-0d1e-2f3a-4b5c-6d7e8f9a0b1c',
     'The Lagoon',
     'PV00678901',
     'RESTAURANT',
     'Seafood restaurant at Cinnamon Grand offering premium Sri Lankan and international seafood dishes with sustainable waste practices.',
     'thelagoon@cinnamonhotels.com',
     '+94112491000',
     'https://www.cinnamonhotels.com/cinnamongrandcolombo/dining/the-lagoon',
     'https://resqeats.lk/logos/the-lagoon.png',
     'PENDING',
     NOW(),
     NOW());

-- ====================================================================
-- 7. SAMPLE OUTLETS
-- ====================================================================
INSERT IGNORE INTO outlets (id, merchant_id, name, description, address, city, postal_code, latitude, longitude, phone, email, status, average_rating, total_ratings, created_at, updated_at) VALUES
    -- Ministry of Crab Outlets
    ('a1b2c3d4-1111-2222-3333-444455556666',
     'a7b8c9d0-e1f2-3a4b-5c6d-7e8f9a0b1c2d',
     'Ministry of Crab - Dutch Hospital',
     'Flagship location in the historic Dutch Hospital precinct offering award-winning crab dishes.',
     'Old Dutch Hospital, Hospital Street, Fort',
     'Colombo',
     '00100',
     6.9344,
     79.8428,
     '+94112320707',
     'dutchhospital@ministryofcrab.com',
     'ACTIVE',
     4.8,
     1256,
     NOW(),
     NOW()),
    
    -- Kefi Colombo Outlet
    ('b2c3d4e5-2222-3333-4444-555566667777',
     'b8c9d0e1-f2a3-4b5c-6d7e-8f9a0b1c2d3e',
     'Kefi - Park Street Mews',
     'Trendy Mediterranean restaurant in the heart of Colombo 2.',
     'No. 50/1, Park Street Mews, Park Street',
     'Colombo',
     '00200',
     6.9147,
     79.8536,
     '+94112055555',
     'parkstreet@keficolombo.lk',
     'ACTIVE',
     4.6,
     892,
     NOW(),
     NOW()),
    
    -- Cinnamon Grand Outlets
    ('c3d4e5f6-3333-4444-5555-666677778888',
     'c9d0e1f2-a3b4-5c6d-7e8f-9a0b1c2d3e4f',
     'Cinnamon Grand - Plates Restaurant',
     'All-day dining restaurant with international buffet and à la carte options.',
     '77 Galle Road, Colombo 03',
     'Colombo',
     '00300',
     6.9219,
     79.8489,
     '+94112491000',
     'plates@cinnamonhotels.com',
     'ACTIVE',
     4.5,
     2341,
     NOW(),
     NOW()),
    ('d4e5f6a7-4444-5555-6666-777788889999',
     'c9d0e1f2-a3b4-5c6d-7e8f-9a0b1c2d3e4f',
     'Cinnamon Grand - Noodles Restaurant',
     'Authentic Asian cuisine featuring Chinese, Japanese, and Thai dishes.',
     '77 Galle Road, Colombo 03',
     'Colombo',
     '00300',
     6.9219,
     79.8489,
     '+94112491000',
     'noodles@cinnamonhotels.com',
     'ACTIVE',
     4.4,
     876,
     NOW(),
     NOW()),
    
    -- Perera & Sons Outlets
    ('e5f6a7b8-5555-6666-7777-888899990000',
     'd0e1f2a3-b4c5-6d7e-8f9a-0b1c2d3e4f5a',
     'Perera & Sons - Colombo 07',
     'Popular bakery outlet in Cinnamon Gardens with fresh pastries and short eats.',
     '42 Horton Place, Colombo 07',
     'Colombo',
     '00700',
     6.9066,
     79.8612,
     '+94112694420',
     'colombo7@pereraandsons.lk',
     'ACTIVE',
     4.3,
     3456,
     NOW(),
     NOW()),
    ('f6a7b8c9-6666-7777-8888-999900001111',
     'd0e1f2a3-b4c5-6d7e-8f9a-0b1c2d3e4f5a',
     'Perera & Sons - Nugegoda',
     'Busy neighborhood bakery serving the Nugegoda community.',
     '125 High Level Road, Nugegoda',
     'Nugegoda',
     '10250',
     6.8729,
     79.8896,
     '+94112820425',
     'nugegoda@pereraandsons.lk',
     'ACTIVE',
     4.2,
     2187,
     NOW(),
     NOW()),
    ('a7b8c9d0-7777-8888-9999-000011112222',
     'd0e1f2a3-b4c5-6d7e-8f9a-0b1c2d3e4f5a',
     'Perera & Sons - Mount Lavinia',
     'Seaside bakery near the famous Mount Lavinia beach.',
     '23 Hotel Road, Mount Lavinia',
     'Mount Lavinia',
     '10370',
     6.8269,
     79.8656,
     '+94112716363',
     'mtlavinia@pereraandsons.lk',
     'ACTIVE',
     4.1,
     1543,
     NOW(),
     NOW()),
    
    -- Keells Super Outlets
    ('b8c9d0e1-8888-9999-0000-111122223333',
     'e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b',
     'Keells Super - Liberty Plaza',
     'Conveniently located supermarket in Liberty Plaza shopping complex.',
     'Liberty Plaza, R.A. De Mel Mawatha, Colombo 03',
     'Colombo',
     '00300',
     6.9167,
     79.8485,
     '+94112574747',
     'libertyplaza@keells.com',
     'ACTIVE',
     4.4,
     4521,
     NOW(),
     NOW()),
    ('c9d0e1f2-9999-0000-1111-222233334444',
     'e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b',
     'Keells Super - Crescat Boulevard',
     'Premium supermarket in Crescat Boulevard with wide selection.',
     'Crescat Boulevard, 89 Galle Road, Colombo 03',
     'Colombo',
     '00300',
     6.9194,
     79.8478,
     '+94112074747',
     'crescat@keells.com',
     'ACTIVE',
     4.5,
     3876,
     NOW(),
     NOW()),
    ('d0e1f2a3-0000-1111-2222-333344445555',
     'e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b',
     'Keells Super - Rajagiriya',
     'Neighborhood supermarket serving the Rajagiriya area.',
     '275 Kotte Road, Rajagiriya',
     'Rajagiriya',
     '10100',
     6.9066,
     79.8977,
     '+94112867676',
     'rajagiriya@keells.com',
     'INACTIVE',
     0.0,
     0,
     NOW(),
     NOW());

-- ====================================================================
-- 8. OUTLET HOURS
-- ====================================================================
-- Ministry of Crab - Dutch Hospital
INSERT IGNORE INTO outlet_hours (id, outlet_id, day_of_week, open_time, close_time, pickup_start, pickup_end, is_closed, created_at, updated_at) VALUES
    (UUID(), 'a1b2c3d4-1111-2222-3333-444455556666', 1, '12:00:00', '22:30:00', '12:00:00', '22:00:00', false, NOW(), NOW()),
    (UUID(), 'a1b2c3d4-1111-2222-3333-444455556666', 2, '12:00:00', '22:30:00', '12:00:00', '22:00:00', false, NOW(), NOW()),
    (UUID(), 'a1b2c3d4-1111-2222-3333-444455556666', 3, '12:00:00', '22:30:00', '12:00:00', '22:00:00', false, NOW(), NOW()),
    (UUID(), 'a1b2c3d4-1111-2222-3333-444455556666', 4, '12:00:00', '22:30:00', '12:00:00', '22:00:00', false, NOW(), NOW()),
    (UUID(), 'a1b2c3d4-1111-2222-3333-444455556666', 5, '12:00:00', '23:00:00', '12:00:00', '22:30:00', false, NOW(), NOW()),
    (UUID(), 'a1b2c3d4-1111-2222-3333-444455556666', 6, '12:00:00', '23:00:00', '12:00:00', '22:30:00', false, NOW(), NOW()),
    (UUID(), 'a1b2c3d4-1111-2222-3333-444455556666', 0, '12:00:00', '22:00:00', '12:00:00', '21:30:00', false, NOW(), NOW());

-- Kefi - Park Street Mews
INSERT IGNORE INTO outlet_hours (id, outlet_id, day_of_week, open_time, close_time, pickup_start, pickup_end, is_closed, created_at, updated_at) VALUES
    (UUID(), 'b2c3d4e5-2222-3333-4444-555566667777', 1, '11:30:00', '23:00:00', '11:30:00', '22:30:00', false, NOW(), NOW()),
    (UUID(), 'b2c3d4e5-2222-3333-4444-555566667777', 2, '11:30:00', '23:00:00', '11:30:00', '22:30:00', false, NOW(), NOW()),
    (UUID(), 'b2c3d4e5-2222-3333-4444-555566667777', 3, '11:30:00', '23:00:00', '11:30:00', '22:30:00', false, NOW(), NOW()),
    (UUID(), 'b2c3d4e5-2222-3333-4444-555566667777', 4, '11:30:00', '23:00:00', '11:30:00', '22:30:00', false, NOW(), NOW()),
    (UUID(), 'b2c3d4e5-2222-3333-4444-555566667777', 5, '11:30:00', '23:30:00', '11:30:00', '23:00:00', false, NOW(), NOW()),
    (UUID(), 'b2c3d4e5-2222-3333-4444-555566667777', 6, '11:30:00', '23:30:00', '11:30:00', '23:00:00', false, NOW(), NOW()),
    (UUID(), 'b2c3d4e5-2222-3333-4444-555566667777', 0, '11:30:00', '22:30:00', '11:30:00', '22:00:00', false, NOW(), NOW());

-- Cinnamon Grand - Plates Restaurant
INSERT IGNORE INTO outlet_hours (id, outlet_id, day_of_week, open_time, close_time, pickup_start, pickup_end, is_closed, created_at, updated_at) VALUES
    (UUID(), 'c3d4e5f6-3333-4444-5555-666677778888', 1, '06:30:00', '23:00:00', '06:30:00', '22:30:00', false, NOW(), NOW()),
    (UUID(), 'c3d4e5f6-3333-4444-5555-666677778888', 2, '06:30:00', '23:00:00', '06:30:00', '22:30:00', false, NOW(), NOW()),
    (UUID(), 'c3d4e5f6-3333-4444-5555-666677778888', 3, '06:30:00', '23:00:00', '06:30:00', '22:30:00', false, NOW(), NOW()),
    (UUID(), 'c3d4e5f6-3333-4444-5555-666677778888', 4, '06:30:00', '23:00:00', '06:30:00', '22:30:00', false, NOW(), NOW()),
    (UUID(), 'c3d4e5f6-3333-4444-5555-666677778888', 5, '06:30:00', '23:30:00', '06:30:00', '23:00:00', false, NOW(), NOW()),
    (UUID(), 'c3d4e5f6-3333-4444-5555-666677778888', 6, '06:30:00', '23:30:00', '06:30:00', '23:00:00', false, NOW(), NOW()),
    (UUID(), 'c3d4e5f6-3333-4444-5555-666677778888', 0, '06:30:00', '23:00:00', '06:30:00', '22:30:00', false, NOW(), NOW());

-- Perera & Sons - Colombo 07
INSERT IGNORE INTO outlet_hours (id, outlet_id, day_of_week, open_time, close_time, pickup_start, pickup_end, is_closed, created_at, updated_at) VALUES
    (UUID(), 'e5f6a7b8-5555-6666-7777-888899990000', 1, '06:00:00', '21:00:00', '06:00:00', '20:30:00', false, NOW(), NOW()),
    (UUID(), 'e5f6a7b8-5555-6666-7777-888899990000', 2, '06:00:00', '21:00:00', '06:00:00', '20:30:00', false, NOW(), NOW()),
    (UUID(), 'e5f6a7b8-5555-6666-7777-888899990000', 3, '06:00:00', '21:00:00', '06:00:00', '20:30:00', false, NOW(), NOW()),
    (UUID(), 'e5f6a7b8-5555-6666-7777-888899990000', 4, '06:00:00', '21:00:00', '06:00:00', '20:30:00', false, NOW(), NOW()),
    (UUID(), 'e5f6a7b8-5555-6666-7777-888899990000', 5, '06:00:00', '21:00:00', '06:00:00', '20:30:00', false, NOW(), NOW()),
    (UUID(), 'e5f6a7b8-5555-6666-7777-888899990000', 6, '06:00:00', '21:00:00', '06:00:00', '20:30:00', false, NOW(), NOW()),
    (UUID(), 'e5f6a7b8-5555-6666-7777-888899990000', 0, '06:30:00', '20:00:00', '06:30:00', '19:30:00', false, NOW(), NOW());

-- Keells Super - Liberty Plaza
INSERT IGNORE INTO outlet_hours (id, outlet_id, day_of_week, open_time, close_time, pickup_start, pickup_end, is_closed, created_at, updated_at) VALUES
    (UUID(), 'b8c9d0e1-8888-9999-0000-111122223333', 1, '08:00:00', '21:00:00', '08:00:00', '20:30:00', false, NOW(), NOW()),
    (UUID(), 'b8c9d0e1-8888-9999-0000-111122223333', 2, '08:00:00', '21:00:00', '08:00:00', '20:30:00', false, NOW(), NOW()),
    (UUID(), 'b8c9d0e1-8888-9999-0000-111122223333', 3, '08:00:00', '21:00:00', '08:00:00', '20:30:00', false, NOW(), NOW()),
    (UUID(), 'b8c9d0e1-8888-9999-0000-111122223333', 4, '08:00:00', '21:00:00', '08:00:00', '20:30:00', false, NOW(), NOW()),
    (UUID(), 'b8c9d0e1-8888-9999-0000-111122223333', 5, '08:00:00', '21:00:00', '08:00:00', '20:30:00', false, NOW(), NOW()),
    (UUID(), 'b8c9d0e1-8888-9999-0000-111122223333', 6, '08:00:00', '21:00:00', '08:00:00', '20:30:00', false, NOW(), NOW()),
    (UUID(), 'b8c9d0e1-8888-9999-0000-111122223333', 0, '09:00:00', '20:00:00', '09:00:00', '19:30:00', false, NOW(), NOW());

-- ====================================================================
-- 9. SAMPLE ITEMS (Master Items)
-- ====================================================================
INSERT IGNORE INTO items (id, merchant_id, name, description, item_type, category, base_price, sale_price, image_url, dietary_info, status, created_at, updated_at) VALUES
    -- Ministry of Crab Items
    ('a1111111-b222-c333-d444-e55555555555',
     'a7b8c9d0-e1f2-3a4b-5c6d-7e8f9a0b1c2d',
     'Crab Curry Mystery Box',
     'Chef''s selection of premium crab preparations including curry, garlic, and pepper crab with rice and accompaniments.',
     'MYSTERY_BOX',
     'SEAFOOD',
     8500.00,
     4250.00,
     'https://resqeats.lk/items/ministry-crab-box.jpg',
     '{"vegetarian": false, "vegan": false, "glutenFree": false, "halal": false, "allergens": ["SHELLFISH", "DAIRY"]}',
     'ACTIVE',
     NOW(), NOW()),
    ('b2222222-c333-d444-e555-f66666666666',
     'a7b8c9d0-e1f2-3a4b-5c6d-7e8f9a0b1c2d',
     'Seafood Platter for Two',
     'Assorted prawns, calamari, and fish with garlic butter sauce and sides.',
     'SINGLE_ITEM',
     'SEAFOOD',
     6500.00,
     3250.00,
     'https://resqeats.lk/items/seafood-platter.jpg',
     '{"vegetarian": false, "vegan": false, "glutenFree": false, "halal": false, "allergens": ["SHELLFISH", "DAIRY", "GLUTEN"]}',
     'ACTIVE',
     NOW(), NOW()),
    
    -- Kefi Colombo Items
    ('c3333333-d444-e555-f666-a77777777777',
     'b8c9d0e1-f2a3-4b5c-6d7e-8f9a0b1c2d3e',
     'Mediterranean Mezze Box',
     'Selection of hummus, falafel, tzatziki, grilled halloumi, pita bread, and Greek salad.',
     'MYSTERY_BOX',
     'MEDITERRANEAN',
     3800.00,
     1900.00,
     'https://resqeats.lk/items/kefi-mezze.jpg',
     '{"vegetarian": true, "vegan": false, "glutenFree": false, "halal": true, "allergens": ["DAIRY", "GLUTEN", "SESAME"]}',
     'ACTIVE',
     NOW(), NOW()),
    ('d4444444-e555-f666-a777-b88888888888',
     'b8c9d0e1-f2a3-4b5c-6d7e-8f9a0b1c2d3e',
     'Greek Lamb Souvlaki Plate',
     'Tender lamb skewers with tzatziki, Greek salad, and crispy pita.',
     'SINGLE_ITEM',
     'MEDITERRANEAN',
     2800.00,
     1400.00,
     'https://resqeats.lk/items/lamb-souvlaki.jpg',
     '{"vegetarian": false, "vegan": false, "glutenFree": false, "halal": true, "allergens": ["DAIRY", "GLUTEN"]}',
     'ACTIVE',
     NOW(), NOW()),
    
    -- Cinnamon Grand Items
    ('e5555555-f666-a777-b888-c99999999999',
     'c9d0e1f2-a3b4-5c6d-7e8f-9a0b1c2d3e4f',
     'Buffet Surplus Box - Large',
     'Chef''s selection of international buffet items including appetizers, mains, and desserts.',
     'MYSTERY_BOX',
     'MIXED',
     4500.00,
     2250.00,
     'https://resqeats.lk/items/cinnamon-buffet-large.jpg',
     '{"vegetarian": false, "vegan": false, "glutenFree": false, "halal": true, "allergens": ["DAIRY", "GLUTEN", "NUTS", "EGGS"]}',
     'ACTIVE',
     NOW(), NOW()),
    ('f6666666-a777-b888-c999-d00000000000',
     'c9d0e1f2-a3b4-5c6d-7e8f-9a0b1c2d3e4f',
     'Buffet Surplus Box - Medium',
     'Selection of quality surplus items from the evening buffet spread.',
     'MYSTERY_BOX',
     'MIXED',
     2800.00,
     1400.00,
     'https://resqeats.lk/items/cinnamon-buffet-medium.jpg',
     '{"vegetarian": false, "vegan": false, "glutenFree": false, "halal": true, "allergens": ["DAIRY", "GLUTEN", "NUTS"]}',
     'ACTIVE',
     NOW(), NOW()),
    ('a7777777-b888-c999-d000-e11111111111',
     'c9d0e1f2-a3b4-5c6d-7e8f-9a0b1c2d3e4f',
     'Dim Sum Platter',
     'Assorted steamed and fried dim sum including har gow, siu mai, and spring rolls.',
     'SINGLE_ITEM',
     'ASIAN',
     2200.00,
     1100.00,
     'https://resqeats.lk/items/dim-sum-platter.jpg',
     '{"vegetarian": false, "vegan": false, "glutenFree": false, "halal": false, "allergens": ["SHELLFISH", "GLUTEN", "EGGS", "SOY"]}',
     'ACTIVE',
     NOW(), NOW()),
    
    -- Perera & Sons Items
    ('b8888888-c999-d000-e111-f22222222222',
     'd0e1f2a3-b4c5-6d7e-8f9a-0b1c2d3e4f5a',
     'Bakery Surprise Box',
     'Assortment of fresh bread, buns, pastries, and short eats from the day''s baking.',
     'MYSTERY_BOX',
     'BAKERY',
     1200.00,
     600.00,
     'https://resqeats.lk/items/perera-bakery-box.jpg',
     '{"vegetarian": true, "vegan": false, "glutenFree": false, "halal": true, "allergens": ["GLUTEN", "DAIRY", "EGGS"]}',
     'ACTIVE',
     NOW(), NOW()),
    ('c9999999-d000-e111-f222-a33333333333',
     'd0e1f2a3-b4c5-6d7e-8f9a-0b1c2d3e4f5a',
     'Short Eats Combo (12 pieces)',
     'Popular Sri Lankan snacks including fish buns, vegetable patties, rolls, and cutlets.',
     'SINGLE_ITEM',
     'SNACKS',
     780.00,
     390.00,
     'https://resqeats.lk/items/short-eats.jpg',
     '{"vegetarian": false, "vegan": false, "glutenFree": false, "halal": false, "allergens": ["GLUTEN", "EGGS", "FISH"]}',
     'ACTIVE',
     NOW(), NOW()),
    ('d0000000-e111-f222-a333-b44444444444',
     'd0e1f2a3-b4c5-6d7e-8f9a-0b1c2d3e4f5a',
     'Cake Slice Selection (3 pieces)',
     'Three slices of assorted cakes including chocolate, vanilla, and fruit cake.',
     'SINGLE_ITEM',
     'DESSERTS',
     950.00,
     475.00,
     'https://resqeats.lk/items/cake-slices.jpg',
     '{"vegetarian": true, "vegan": false, "glutenFree": false, "halal": true, "allergens": ["GLUTEN", "DAIRY", "EGGS", "NUTS"]}',
     'ACTIVE',
     NOW(), NOW()),
    
    -- Keells Super Items
    ('e1111111-f222-a333-b444-c55555555555',
     'e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b',
     'Fresh Vegetable Box',
     '3kg assortment of fresh vegetables including carrots, beans, tomatoes, and leafy greens.',
     'SINGLE_ITEM',
     'PRODUCE',
     1500.00,
     750.00,
     'https://resqeats.lk/items/keells-veg-box.jpg',
     '{"vegetarian": true, "vegan": true, "glutenFree": true, "halal": true, "allergens": []}',
     'ACTIVE',
     NOW(), NOW()),
    ('f2222222-a333-b444-c555-d66666666666',
     'e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b',
     'Fruit Basket Surprise',
     'Seasonal fresh fruits including bananas, papayas, mangoes, and apples.',
     'MYSTERY_BOX',
     'PRODUCE',
     1800.00,
     900.00,
     'https://resqeats.lk/items/keells-fruit.jpg',
     '{"vegetarian": true, "vegan": true, "glutenFree": true, "halal": true, "allergens": []}',
     'ACTIVE',
     NOW(), NOW()),
    ('a3333333-b444-c555-d666-e77777777777',
     'e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b',
     'Dairy Essentials Pack',
     'Fresh milk, curd, cheese, and butter approaching best-before date.',
     'SINGLE_ITEM',
     'DAIRY',
     1650.00,
     825.00,
     'https://resqeats.lk/items/keells-dairy.jpg',
     '{"vegetarian": true, "vegan": false, "glutenFree": false, "halal": true, "allergens": ["DAIRY", "LACTOSE"]}',
     'ACTIVE',
     NOW(), NOW()),
    ('b4444444-c555-d666-e777-f88888888888',
     'e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b',
     'Ready-to-Eat Meals Pack',
     'Selection of prepared meals including rice and curry, fried rice, and kottu.',
     'MYSTERY_BOX',
     'MEALS',
     2200.00,
     1100.00,
     'https://resqeats.lk/items/keells-meals.jpg',
     '{"vegetarian": false, "vegan": false, "glutenFree": false, "halal": true, "allergens": ["GLUTEN", "EGGS", "SOY"]}',
     'ACTIVE',
     NOW(), NOW());

-- ====================================================================
-- 10. OUTLET ITEMS (Available at specific outlets)
-- ====================================================================
INSERT IGNORE INTO outlet_items (id, outlet_id, item_id, current_quantity, is_available, created_at, updated_at) VALUES
    -- Ministry of Crab - Dutch Hospital
    ('oi-a1111111-1111-1111-1111-111111111111',
     'a1b2c3d4-1111-2222-3333-444455556666',
     'a1111111-b222-c333-d444-e55555555555',
     8,
     true,
     NOW(), NOW()),
    ('oi-a2222222-2222-2222-2222-222222222222',
     'a1b2c3d4-1111-2222-3333-444455556666',
     'b2222222-c333-d444-e555-f66666666666',
     5,
     true,
     NOW(), NOW()),
    
    -- Kefi - Park Street Mews
    ('oi-b1111111-1111-1111-1111-111111111111',
     'b2c3d4e5-2222-3333-4444-555566667777',
     'c3333333-d444-e555-f666-a77777777777',
     12,
     true,
     NOW(), NOW()),
    ('oi-b2222222-2222-2222-2222-222222222222',
     'b2c3d4e5-2222-3333-4444-555566667777',
     'd4444444-e555-f666-a777-b88888888888',
     8,
     true,
     NOW(), NOW()),
    
    -- Cinnamon Grand - Plates Restaurant
    ('oi-c1111111-1111-1111-1111-111111111111',
     'c3d4e5f6-3333-4444-5555-666677778888',
     'e5555555-f666-a777-b888-c99999999999',
     15,
     true,
     NOW(), NOW()),
    ('oi-c2222222-2222-2222-2222-222222222222',
     'c3d4e5f6-3333-4444-5555-666677778888',
     'f6666666-a777-b888-c999-d00000000000',
     20,
     true,
     NOW(), NOW()),
    
    -- Cinnamon Grand - Noodles Restaurant
    ('oi-d1111111-1111-1111-1111-111111111111',
     'd4e5f6a7-4444-5555-6666-777788889999',
     'a7777777-b888-c999-d000-e11111111111',
     10,
     true,
     NOW(), NOW()),
    
    -- Perera & Sons - Colombo 07
    ('oi-e1111111-1111-1111-1111-111111111111',
     'e5f6a7b8-5555-6666-7777-888899990000',
     'b8888888-c999-d000-e111-f22222222222',
     25,
     true,
     NOW(), NOW()),
    ('oi-e2222222-2222-2222-2222-222222222222',
     'e5f6a7b8-5555-6666-7777-888899990000',
     'c9999999-d000-e111-f222-a33333333333',
     40,
     true,
     NOW(), NOW()),
    ('oi-e3333333-3333-3333-3333-333333333333',
     'e5f6a7b8-5555-6666-7777-888899990000',
     'd0000000-e111-f222-a333-b44444444444',
     15,
     true,
     NOW(), NOW()),
    
    -- Perera & Sons - Nugegoda
    ('oi-f1111111-1111-1111-1111-111111111111',
     'f6a7b8c9-6666-7777-8888-999900001111',
     'b8888888-c999-d000-e111-f22222222222',
     30,
     true,
     NOW(), NOW()),
    ('oi-f2222222-2222-2222-2222-222222222222',
     'f6a7b8c9-6666-7777-8888-999900001111',
     'c9999999-d000-e111-f222-a33333333333',
     50,
     true,
     NOW(), NOW()),
    
    -- Keells Super - Liberty Plaza
    ('oi-g1111111-1111-1111-1111-111111111111',
     'b8c9d0e1-8888-9999-0000-111122223333',
     'e1111111-f222-a333-b444-c55555555555',
     35,
     true,
     NOW(), NOW()),
    ('oi-g2222222-2222-2222-2222-222222222222',
     'b8c9d0e1-8888-9999-0000-111122223333',
     'f2222222-a333-b444-c555-d66666666666',
     25,
     true,
     NOW(), NOW()),
    ('oi-g3333333-3333-3333-3333-333333333333',
     'b8c9d0e1-8888-9999-0000-111122223333',
     'a3333333-b444-c555-d666-e77777777777',
     20,
     true,
     NOW(), NOW()),
    ('oi-g4444444-4444-4444-4444-444444444444',
     'b8c9d0e1-8888-9999-0000-111122223333',
     'b4444444-c555-d666-e777-f88888888888',
     18,
     true,
     NOW(), NOW()),
    
    -- Keells Super - Crescat Boulevard
    ('oi-h1111111-1111-1111-1111-111111111111',
     'c9d0e1f2-9999-0000-1111-222233334444',
     'e1111111-f222-a333-b444-c55555555555',
     40,
     true,
     NOW(), NOW()),
    ('oi-h2222222-2222-2222-2222-222222222222',
     'c9d0e1f2-9999-0000-1111-222233334444',
     'f2222222-a333-b444-c555-d66666666666',
     30,
     true,
     NOW(), NOW()),
    ('oi-h3333333-3333-3333-3333-333333333333',
     'c9d0e1f2-9999-0000-1111-222233334444',
     'a3333333-b444-c555-d666-e77777777777',
     25,
     true,
     NOW(), NOW());

-- ====================================================================
-- 11. SAMPLE ORDERS (For testing)
-- ====================================================================
INSERT IGNORE INTO orders (id, user_id, outlet_id, order_number, status, subtotal, tax, total, pickup_code, pickup_by, notes, created_at, updated_at) VALUES
    ('ord-11111111-1111-1111-1111-111111111111',
     '1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d',
     'a1b2c3d4-1111-2222-3333-444455556666',
     'RQE-2025-000001',
     'COMPLETED',
     7500.00,
     0.00,
     6750.00,
     '847291',
     DATE_ADD(NOW(), INTERVAL 2 HOUR),
     'Please pack carefully, will be travelling',
     DATE_SUB(NOW(), INTERVAL 3 DAY),
     DATE_SUB(NOW(), INTERVAL 3 DAY)),
    ('ord-22222222-2222-2222-2222-222222222222',
     '1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d',
     'e5f6a7b8-5555-6666-7777-888899990000',
     'RQE-2025-000002',
     'READY',
     1170.00,
     0.00,
     1053.00,
     '395847',
     DATE_ADD(NOW(), INTERVAL 1 HOUR),
     NULL,
     DATE_SUB(NOW(), INTERVAL 1 HOUR),
     DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
    ('ord-33333333-3333-3333-3333-333333333333',
     '2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e',
     'b8c9d0e1-8888-9999-0000-111122223333',
     'RQE-2025-000003',
     'PENDING',
     2475.00,
     0.00,
     2227.50,
     NULL,
     DATE_ADD(NOW(), INTERVAL 3 HOUR),
     'Call on arrival - Gate B entrance',
     NOW(),
     NOW()),
    ('ord-44444444-4444-4444-4444-444444444444',
     '3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f',
     'b2c3d4e5-2222-3333-4444-555566667777',
     'RQE-2025-000004',
     'CONFIRMED',
     3300.00,
     0.00,
     2970.00,
     '629481',
     DATE_ADD(NOW(), INTERVAL 4 HOUR),
     'Vegetarian preference',
     DATE_SUB(NOW(), INTERVAL 2 HOUR),
     DATE_SUB(NOW(), INTERVAL 1 HOUR)),
    ('ord-55555555-5555-5555-5555-555555555555',
     '4d5e6f7a-8b9c-0d1e-2f3a-4b5c6d7e8f9a',
     'c3d4e5f6-3333-4444-5555-666677778888',
     'RQE-2025-000005',
     'CANCELLED',
     4500.00,
     0.00,
     4050.00,
     NULL,
     DATE_ADD(NOW(), INTERVAL 5 HOUR),
     'Cancelled - schedule conflict',
     DATE_SUB(NOW(), INTERVAL 1 DAY),
     DATE_SUB(NOW(), INTERVAL 12 HOUR));

-- ====================================================================
-- 12. ORDER ITEMS
-- ====================================================================
INSERT IGNORE INTO order_items (id, order_id, item_id, item_name, quantity, unit_price, total_price, created_at) VALUES
    -- Order 1 items (Ministry of Crab)
    (UUID(), 'ord-11111111-1111-1111-1111-111111111111', 'a1111111-b222-c333-d444-e55555555555', 'Crab Curry Mystery Box', 1, 4250.00, 4250.00, DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (UUID(), 'ord-11111111-1111-1111-1111-111111111111', 'b2222222-c333-d444-e555-f66666666666', 'Seafood Platter for Two', 1, 3250.00, 3250.00, DATE_SUB(NOW(), INTERVAL 3 DAY)),
    
    -- Order 2 items (Perera & Sons)
    (UUID(), 'ord-22222222-2222-2222-2222-222222222222', 'b8888888-c999-d000-e111-f22222222222', 'Bakery Surprise Box', 1, 600.00, 600.00, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
    (UUID(), 'ord-22222222-2222-2222-2222-222222222222', 'c9999999-d000-e111-f222-a33333333333', 'Short Eats Combo (12 pieces)', 1, 390.00, 390.00, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
    (UUID(), 'ord-22222222-2222-2222-2222-222222222222', 'd0000000-e111-f222-a333-b44444444444', 'Cake Slice Selection (3 pieces)', 1, 475.00, 475.00, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
    
    -- Order 3 items (Keells Super)
    (UUID(), 'ord-33333333-3333-3333-3333-333333333333', 'e1111111-f222-a333-b444-c55555555555', 'Fresh Vegetable Box', 1, 750.00, 750.00, NOW()),
    (UUID(), 'ord-33333333-3333-3333-3333-333333333333', 'f2222222-a333-b444-c555-d66666666666', 'Fruit Basket Surprise', 1, 900.00, 900.00, NOW()),
    (UUID(), 'ord-33333333-3333-3333-3333-333333333333', 'a3333333-b444-c555-d666-e77777777777', 'Dairy Essentials Pack', 1, 825.00, 825.00, NOW()),
    
    -- Order 4 items (Kefi Colombo)
    (UUID(), 'ord-44444444-4444-4444-4444-444444444444', 'c3333333-d444-e555-f666-a77777777777', 'Mediterranean Mezze Box', 1, 1900.00, 1900.00, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
    (UUID(), 'ord-44444444-4444-4444-4444-444444444444', 'd4444444-e555-f666-a777-b88888888888', 'Greek Lamb Souvlaki Plate', 1, 1400.00, 1400.00, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
    
    -- Order 5 items (Cinnamon Grand - Cancelled)
    (UUID(), 'ord-55555555-5555-5555-5555-555555555555', 'e5555555-f666-a777-b888-c99999999999', 'Buffet Surplus Box - Large', 2, 2250.00, 4500.00, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ====================================================================
-- 13. SAMPLE PAYMENTS
-- ====================================================================
INSERT IGNORE INTO payments (id, order_id, amount, status, payment_method_token, ipg_transaction_id, created_at, updated_at) VALUES
    ('pay-11111111-1111-1111-1111-111111111111',
     'ord-11111111-1111-1111-1111-111111111111',
     6750.00,
     'COMPLETED',
     NULL,
     'VISA-4532-XXX-847291',
     DATE_SUB(NOW(), INTERVAL 3 DAY),
     DATE_SUB(NOW(), INTERVAL 3 DAY)),
    ('pay-22222222-2222-2222-2222-222222222222',
     'ord-22222222-2222-2222-2222-222222222222',
     1053.00,
     'COMPLETED',
     NULL,
     'MASTER-5412-XXX-395847',
     DATE_SUB(NOW(), INTERVAL 1 HOUR),
     DATE_SUB(NOW(), INTERVAL 1 HOUR)),
    ('pay-33333333-3333-3333-3333-333333333333',
     'ord-33333333-3333-3333-3333-333333333333',
     2227.50,
     'PENDING',
     NULL,
     NULL,
     NOW(),
     NOW()),
    ('pay-44444444-4444-4444-4444-444444444444',
     'ord-44444444-4444-4444-4444-444444444444',
     2970.00,
     'COMPLETED',
     NULL,
     'VISA-4716-XXX-629481',
     DATE_SUB(NOW(), INTERVAL 2 HOUR),
     DATE_SUB(NOW(), INTERVAL 2 HOUR)),
    ('pay-55555555-5555-5555-5555-555555555555',
     'ord-55555555-5555-5555-5555-555555555555',
     4050.00,
     'REFUNDED',
     NULL,
     'AMEX-3742-XXX-REFUND',
     DATE_SUB(NOW(), INTERVAL 1 DAY),
     DATE_SUB(NOW(), INTERVAL 12 HOUR));

-- ====================================================================
-- 14. SAMPLE NOTIFICATIONS
-- ====================================================================
INSERT IGNORE INTO notifications (id, user_id, title, message, type, status, data, created_at, updated_at) VALUES
    (UUID(),
     '1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d',
     'Order Ready for Pickup!',
     'Your order RQE-2025-000002 is ready for pickup at Perera & Sons - Colombo 07. Your pickup code is 395847.',
     'ORDER_UPDATE',
     'UNREAD',
     '{"orderId": "ord-22222222-2222-2222-2222-222222222222", "outletId": "e5f6a7b8-5555-6666-7777-888899990000", "pickupCode": "395847"}',
     DATE_SUB(NOW(), INTERVAL 30 MINUTE),
     DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
    (UUID(),
     '1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d',
     'Order Completed - Thank You!',
     'Your order RQE-2025-000001 from Ministry of Crab has been completed. You helped save 2.5kg of food from waste!',
     'ORDER_UPDATE',
     'READ',
     '{"orderId": "ord-11111111-1111-1111-1111-111111111111", "foodSaved": "2.5kg"}',
     DATE_SUB(NOW(), INTERVAL 3 DAY),
     DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (UUID(),
     '2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e',
     'Order Placed Successfully',
     'Your order RQE-2025-000003 has been placed. We''ll notify you when it''s ready for pickup at Keells Super - Liberty Plaza.',
     'ORDER_UPDATE',
     'UNREAD',
     '{"orderId": "ord-33333333-3333-3333-3333-333333333333"}',
     NOW(),
     NOW()),
    (UUID(),
     '3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f',
     'Order Confirmed',
     'Great news! Kefi - Park Street Mews has confirmed your order RQE-2025-000004. Pickup code: 629481.',
     'ORDER_UPDATE',
     'UNREAD',
     '{"orderId": "ord-44444444-4444-4444-4444-444444444444", "pickupCode": "629481"}',
     DATE_SUB(NOW(), INTERVAL 1 HOUR),
     DATE_SUB(NOW(), INTERVAL 1 HOUR)),
    (UUID(),
     '9c0d1e2f-3a4b-5c6d-7e8f-9a0b1c2d3e4f',
     'New Order Received',
     'You have a new order RQE-2025-000002 at Perera & Sons - Colombo 07. Please prepare for pickup.',
     'ORDER_UPDATE',
     'UNREAD',
     '{"orderId": "ord-22222222-2222-2222-2222-222222222222", "outletId": "e5f6a7b8-5555-6666-7777-888899990000"}',
     DATE_SUB(NOW(), INTERVAL 1 HOUR),
     DATE_SUB(NOW(), INTERVAL 1 HOUR)),
    (UUID(),
     '4d5e6f7a-8b9c-0d1e-2f3a-4b5c6d7e8f9a',
     'Order Cancelled & Refunded',
     'Your order RQE-2025-000005 has been cancelled. A full refund of Rs. 4,050 has been processed to your card.',
     'ORDER_UPDATE',
     'READ',
     '{"orderId": "ord-55555555-5555-5555-5555-555555555555", "refundAmount": 4050.00}',
     DATE_SUB(NOW(), INTERVAL 12 HOUR),
     DATE_SUB(NOW(), INTERVAL 12 HOUR)),
    (UUID(),
     '1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d',
     'Flash Sale Alert!',
     '50% off at Ministry of Crab tonight! Premium crab dishes available from 8:30 PM.',
     'PROMOTION',
     'UNREAD',
     '{"merchantId": "a7b8c9d0-e1f2-3a4b-5c6d-7e8f9a0b1c2d", "discount": "50%"}',
     DATE_SUB(NOW(), INTERVAL 4 HOUR),
     DATE_SUB(NOW(), INTERVAL 4 HOUR));

-- ====================================================================
-- 15. OUTLET STAFF USERS (OUTLET_USER role)
-- ====================================================================
INSERT IGNORE INTO users (id, email, password_hash, first_name, last_name, phone, role, status, email_verified, phone_verified, oauth2_provider, created_at, updated_at) VALUES
    ('staff-1111-2222-3333-444455556666',
     'priya.fernando@ministryofcrab.com',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Priya',
     'Fernando',
     '+94771111222',
     'OUTLET_USER',
     'ACTIVE',
     true,
     true,
     'LOCAL',
     NOW(),
     NOW()),
    ('staff-2222-3333-4444-555566667777',
     'saman.jayasuriya@pereraandsons.lk',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Saman',
     'Jayasuriya',
     '+94772222333',
     'OUTLET_USER',
     'ACTIVE',
     true,
     true,
     'LOCAL',
     NOW(),
     NOW()),
    ('staff-3333-4444-5555-666677778888',
     'chamari.silva@keells.com',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Chamari',
     'Silva',
     '+94773333444',
     'OUTLET_USER',
     'ACTIVE',
     true,
     true,
     'LOCAL',
     NOW(),
     NOW()),
    ('staff-4444-5555-6666-777788889999',
     'nuwan.bandara@cinnamonhotels.com',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Nuwan',
     'Bandara',
     '+94774444555',
     'OUTLET_USER',
     'ACTIVE',
     true,
     true,
     'LOCAL',
     NOW(),
     NOW());

-- ====================================================================
-- 16. INDEXES FOR PERFORMANCE (if not already created)
-- ====================================================================
-- Note: MySQL doesn't support GIST indexes, using standard indexes instead
-- These may already exist from schema file

-- Create indexes only if they don't already exist (safe for repeated runs)
-- idx_outlets_latitude
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outlets' AND INDEX_NAME = 'idx_outlets_latitude';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_outlets_latitude ON outlets (latitude);', 'SELECT "idx_outlets_latitude already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_outlets_longitude
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outlets' AND INDEX_NAME = 'idx_outlets_longitude';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_outlets_longitude ON outlets (longitude);', 'SELECT "idx_outlets_longitude already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_outlets_status
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outlets' AND INDEX_NAME = 'idx_outlets_status';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_outlets_status ON outlets (status);', 'SELECT "idx_outlets_status already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_outlets_merchant
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outlets' AND INDEX_NAME = 'idx_outlets_merchant';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_outlets_merchant ON outlets (merchant_id);', 'SELECT "idx_outlets_merchant already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_outlet_items_outlet
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outlet_items' AND INDEX_NAME = 'idx_outlet_items_outlet';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_outlet_items_outlet ON outlet_items (outlet_id);', 'SELECT "idx_outlet_items_outlet already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_outlet_items_availability
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outlet_items' AND INDEX_NAME = 'idx_outlet_items_availability';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_outlet_items_availability ON outlet_items (outlet_id, is_available);', 'SELECT "idx_outlet_items_availability already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_orders_user
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND INDEX_NAME = 'idx_orders_user';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_orders_user ON orders (user_id);', 'SELECT "idx_orders_user already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_orders_outlet
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND INDEX_NAME = 'idx_orders_outlet';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_orders_outlet ON orders (outlet_id);', 'SELECT "idx_orders_outlet already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_orders_status
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND INDEX_NAME = 'idx_orders_status';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_orders_status ON orders (status);', 'SELECT "idx_orders_status already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_notifications_user
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notifications' AND INDEX_NAME = 'idx_notifications_user';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_notifications_user ON notifications (user_id);', 'SELECT "idx_notifications_user already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_notifications_status
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notifications' AND INDEX_NAME = 'idx_notifications_status';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_notifications_status ON notifications (user_id, status);', 'SELECT "idx_notifications_status already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ====================================================================
-- SUMMARY
-- ====================================================================
-- This script creates:
-- - 4 Roles (ADMIN, USER, MERCHANT, OUTLET_USER)
-- - 1 Admin user (Kasun Perera)
-- - 5 Sample customers (Sri Lankan names)
-- - 5 Merchant owners + 6 Merchants (Real Sri Lankan businesses)
-- - 10 Outlets with operating hours (Real locations in Colombo area)
-- - 14 Items (Realistic food items with LKR pricing)
-- - 18 Outlet items (Item availability at outlets)
-- - 5 Sample orders with items
-- - 5 Sample payments
-- - 7 Sample notifications
-- - 4 Outlet staff members
-- ====================================================================

SELECT 'Resqeats initial data migration completed successfully!' AS result;