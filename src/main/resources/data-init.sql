-- ====================================================================
-- RESQEATS FOOD RESCUE PLATFORM - INITIAL DATA (MySQL)
-- ====================================================================
-- IMPORTANT:
-- - All primary keys are BIGINT AUTO_INCREMENT (JPA BaseEntity Long).
-- - Do NOT insert explicit IDs.
-- - Relationships are wired via SELECT lookups into MySQL user variables.
-- - Foreign keys are added idempotently (safe for repeated runs).
-- ====================================================================

SET @now := NOW();

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
-- 2. USERS
-- ====================================================================
-- Passwords:
-- - Admin@123! (BCrypt: $2a$10$N9qo8uLOickgx2ZMRZoMye.WXcSzA.IjqRzPOCwzBbxJNMQGxZ0Aq)
-- - User@123!  (BCrypt: $2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O)

-- Admins
INSERT IGNORE INTO users (email, password_hash, first_name, last_name, phone, role, status, email_verified, phone_verified, oauth2_provider, created_at, updated_at) VALUES
    ('admin@resqeats.lk',
     '$2a$10$N9qo8uLOickgx2ZMRZoMye.WXcSzA.IjqRzPOCwzBbxJNMQGxZ0Aq',
     'Kasun', 'Perera', '+94771234500',
     'ADMIN', 'ACTIVE', true, true, 'LOCAL', @now, @now),
    ('superadmin@resqeats.lk',
     '$2a$10$N9qo8uLOickgx2ZMRZoMye.WXcSzA.IjqRzPOCwzBbxJNMQGxZ0Aq',
     'Super', 'Administrator', '+94770000000',
     'SUPER_ADMIN', 'ACTIVE', true, true, 'LOCAL', @now, @now);

-- Sample customers
INSERT IGNORE INTO users (email, password_hash, first_name, last_name, phone, role, status, email_verified, phone_verified, oauth2_provider, created_at, updated_at) VALUES
    ('nimal.silva@gmail.com',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Nimal', 'Silva', '+94771234567',
    'CUSTOMER_USER', 'ACTIVE', true, true, 'LOCAL', @now, @now),
    ('kumari.fernando@yahoo.com',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Kumari', 'Fernando', '+94777654321',
    'CUSTOMER_USER', 'ACTIVE', true, false, 'LOCAL', @now, @now),
    ('ashan.jayawardena@gmail.com',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Ashan', 'Jayawardena', '+94779876543',
    'CUSTOMER_USER', 'ACTIVE', true, true, 'GOOGLE', @now, @now),
    ('dilini.wickramasinghe@outlook.com',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Dilini', 'Wickramasinghe', '+94765432198',
    'CUSTOMER_USER', 'ACTIVE', true, true, 'LOCAL', @now, @now),
    ('ruwan.bandara@gmail.com',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Ruwan', 'Bandara', '+94712345678',
    'CUSTOMER_USER', 'ACTIVE', true, true, 'LOCAL', @now, @now);

-- Merchant owner users
INSERT IGNORE INTO users (email, password_hash, first_name, last_name, phone, role, status, email_verified, phone_verified, oauth2_provider, created_at, updated_at) VALUES
    ('dharshan@ministryofcrab.com',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Dharshan', 'Munidasa', '+94112320707',
    'MERCHANT_USER', 'ACTIVE', true, true, 'LOCAL', @now, @now),
    ('niranjan@kefiscolombo.lk',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Niranjan', 'David', '+94112055555',
    'MERCHANT_USER', 'ACTIVE', true, true, 'LOCAL', @now, @now),
    ('management@cinnamonhotels.com',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Manori', 'Unambuwe', '+94112491000',
    'MERCHANT_USER', 'ACTIVE', true, true, 'LOCAL', @now, @now),
    ('info@pereraandsons.lk',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Damitha', 'Perera', '+94112588855',
    'MERCHANT_USER', 'ACTIVE', true, true, 'LOCAL', @now, @now),
    ('contact@keells.lk',
     '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
     'Charith', 'Senanayake', '+94112303500',
    'MERCHANT_USER', 'ACTIVE', true, true, 'LOCAL', @now, @now);

-- Capture key user IDs
SELECT id INTO @u_admin FROM users WHERE email = 'admin@resqeats.lk' LIMIT 1;
SELECT id INTO @u_superadmin FROM users WHERE email = 'superadmin@resqeats.lk' LIMIT 1;
SELECT id INTO @u_nimal FROM users WHERE email = 'nimal.silva@gmail.com' LIMIT 1;
SELECT id INTO @u_kumari FROM users WHERE email = 'kumari.fernando@yahoo.com' LIMIT 1;
SELECT id INTO @u_ashan FROM users WHERE email = 'ashan.jayawardena@gmail.com' LIMIT 1;
SELECT id INTO @u_dilini FROM users WHERE email = 'dilini.wickramasinghe@outlook.com' LIMIT 1;
SELECT id INTO @u_ruwan FROM users WHERE email = 'ruwan.bandara@gmail.com' LIMIT 1;
SELECT id INTO @u_dharshan FROM users WHERE email = 'dharshan@ministryofcrab.com' LIMIT 1;
SELECT id INTO @u_niranjan FROM users WHERE email = 'niranjan@kefiscolombo.lk' LIMIT 1;
SELECT id INTO @u_manori FROM users WHERE email = 'management@cinnamonhotels.com' LIMIT 1;
SELECT id INTO @u_damitha FROM users WHERE email = 'info@pereraandsons.lk' LIMIT 1;
SELECT id INTO @u_charith FROM users WHERE email = 'contact@keells.lk' LIMIT 1;

-- ====================================================================
-- 3. MERCHANTS
-- ====================================================================
INSERT IGNORE INTO merchants (owner_user_id, name, registration_no, category, description, contact_email, contact_phone, website, logo_url, status, created_at, updated_at) VALUES
    (@u_dharshan,
     'Ministry of Crab', 'PV00123456', 'RESTAURANT',
     'Award-winning seafood restaurant by celebrity chef Dharshan Munidasa, offering premium crab dishes and reducing food waste through discounted end-of-day specials.',
     'info@ministryofcrab.com', '+94112320707', 'https://www.ministryofcrab.com',
     'https://resqeats.lk/logos/ministry-of-crab.png', 'APPROVED', @now, @now),
    (@u_niranjan,
     'Kefi Colombo', 'PV00234567', 'RESTAURANT',
     'Mediterranean and Greek cuisine restaurant committed to sustainability and reducing food waste through quality surplus meal offerings.',
     'hello@keficolombo.lk', '+94112055555', 'https://www.keficolombo.lk',
     'https://resqeats.lk/logos/kefi-colombo.png', 'APPROVED', @now, @now),
    (@u_manori,
     'Cinnamon Grand Colombo', 'PV00345678', 'HOTEL',
     'Five-star luxury hotel offering surplus buffet items and gourmet meals at discounted prices to minimize food waste.',
     'reservations@cinnamonhotels.com', '+94112491000', 'https://www.cinnamonhotels.com/cinnamongrandcolombo',
     'https://resqeats.lk/logos/cinnamon-grand.png', 'APPROVED', @now, @now),
    (@u_damitha,
     'Perera & Sons', 'PV00456789', 'BAKERY',
     'Iconic Sri Lankan bakery chain offering fresh bread, pastries, short eats, and cakes with end-of-day discounts to reduce waste.',
     'info@pereraandsons.lk', '+94112588855', 'https://www.pereraandsons.lk',
     'https://resqeats.lk/logos/perera-sons.png', 'APPROVED', @now, @now),
    (@u_charith,
     'Keells Super', 'PV00567890', 'GROCERY',
     'Leading supermarket chain in Sri Lanka offering fresh produce, dairy, and prepared foods with daily discounts on items approaching best-before dates.',
     'customercare@keells.com', '+94112303500', 'https://www.keellssuper.com',
     'https://resqeats.lk/logos/keells-super.png', 'APPROVED', @now, @now),
    (@u_dharshan,
     'The Lagoon', 'PV00678901', 'RESTAURANT',
     'Seafood restaurant at Cinnamon Grand offering premium Sri Lankan and international seafood dishes with sustainable waste practices.',
     'thelagoon@cinnamonhotels.com', '+94112491000',
     'https://www.cinnamonhotels.com/cinnamongrandcolombo/dining/the-lagoon',
    'https://resqeats.lk/logos/the-lagoon.png', 'PENDING_APPROVAL', @now, @now);

-- Capture merchant IDs
SELECT id INTO @m_ministry FROM merchants WHERE registration_no = 'PV00123456' LIMIT 1;
SELECT id INTO @m_kefi FROM merchants WHERE registration_no = 'PV00234567' LIMIT 1;
SELECT id INTO @m_cinnamon FROM merchants WHERE registration_no = 'PV00345678' LIMIT 1;
SELECT id INTO @m_perera FROM merchants WHERE registration_no = 'PV00456789' LIMIT 1;
SELECT id INTO @m_keells FROM merchants WHERE registration_no = 'PV00567890' LIMIT 1;
SELECT id INTO @m_lagoon FROM merchants WHERE registration_no = 'PV00678901' LIMIT 1;

-- Back-fill merchant_id on MERCHANT users for tenant filtering
UPDATE users SET merchant_id = @m_ministry WHERE email = 'dharshan@ministryofcrab.com' AND (merchant_id IS NULL OR merchant_id <> @m_ministry);
UPDATE users SET merchant_id = @m_kefi WHERE email = 'niranjan@kefiscolombo.lk' AND (merchant_id IS NULL OR merchant_id <> @m_kefi);
UPDATE users SET merchant_id = @m_cinnamon WHERE email = 'management@cinnamonhotels.com' AND (merchant_id IS NULL OR merchant_id <> @m_cinnamon);
UPDATE users SET merchant_id = @m_perera WHERE email = 'info@pereraandsons.lk' AND (merchant_id IS NULL OR merchant_id <> @m_perera);
UPDATE users SET merchant_id = @m_keells WHERE email = 'contact@keells.lk' AND (merchant_id IS NULL OR merchant_id <> @m_keells);

-- ====================================================================
-- 4. OUTLETS
-- ====================================================================
INSERT IGNORE INTO outlets (merchant_id, name, description, address, city, postal_code, latitude, longitude, phone, email, status, availability_status, average_rating, total_ratings, created_at, updated_at) VALUES
    -- Ministry of Crab Outlets
    (@m_ministry,
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
    'OPEN',
     4.8,
     1256,
     @now,
     @now),
    
    -- Kefi Colombo Outlet
    (@m_kefi,
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
    'OPEN',
     4.6,
     892,
     @now,
     @now),
    
    -- Cinnamon Grand Outlets
    (@m_cinnamon,
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
    'OPEN',
     4.5,
     2341,
     @now,
     @now),
    (@m_cinnamon,
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
    'OPEN',
     4.4,
     876,
     @now,
     @now),
    
    -- Perera & Sons Outlets
    (@m_perera,
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
    'OPEN',
     4.3,
     3456,
     @now,
     @now),
    (@m_perera,
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
    'OPEN',
     4.2,
     2187,
     @now,
     @now),
    (@m_perera,
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
    'OPEN',
     4.1,
     1543,
     @now,
     @now),
    
    -- Keells Super Outlets
    (@m_keells,
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
    'OPEN',
     4.4,
     4521,
     @now,
     @now),
    (@m_keells,
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
    'OPEN',
     4.5,
     3876,
     @now,
     @now),
    (@m_keells,
     'Keells Super - Rajagiriya',
     'Neighborhood supermarket serving the Rajagiriya area.',
     '275 Kotte Road, Rajagiriya',
     'Rajagiriya',
     '10100',
     6.9066,
     79.8977,
     '+94112867676',
     'rajagiriya@keells.com',
    'DISABLED',
    'CLOSED',
     0.0,
     0,
     @now,
     @now);

-- Capture outlet IDs
SELECT id INTO @o_ministry_dutch FROM outlets WHERE merchant_id = @m_ministry AND name = 'Ministry of Crab - Dutch Hospital' LIMIT 1;
SELECT id INTO @o_kefi_park FROM outlets WHERE merchant_id = @m_kefi AND name = 'Kefi - Park Street Mews' LIMIT 1;
SELECT id INTO @o_cinnamon_plates FROM outlets WHERE merchant_id = @m_cinnamon AND name = 'Cinnamon Grand - Plates Restaurant' LIMIT 1;
SELECT id INTO @o_cinnamon_noodles FROM outlets WHERE merchant_id = @m_cinnamon AND name = 'Cinnamon Grand - Noodles Restaurant' LIMIT 1;
SELECT id INTO @o_perera_c7 FROM outlets WHERE merchant_id = @m_perera AND name = 'Perera & Sons - Colombo 07' LIMIT 1;
SELECT id INTO @o_perera_nugegoda FROM outlets WHERE merchant_id = @m_perera AND name = 'Perera & Sons - Nugegoda' LIMIT 1;
SELECT id INTO @o_perera_mount FROM outlets WHERE merchant_id = @m_perera AND name = 'Perera & Sons - Mount Lavinia' LIMIT 1;
SELECT id INTO @o_keells_liberty FROM outlets WHERE merchant_id = @m_keells AND name = 'Keells Super - Liberty Plaza' LIMIT 1;
SELECT id INTO @o_keells_crescat FROM outlets WHERE merchant_id = @m_keells AND name = 'Keells Super - Crescat Boulevard' LIMIT 1;
SELECT id INTO @o_keells_rajagiriya FROM outlets WHERE merchant_id = @m_keells AND name = 'Keells Super - Rajagiriya' LIMIT 1;

-- ====================================================================
-- 5. OUTLET HOURS
-- ====================================================================
-- Ministry of Crab - Dutch Hospital
INSERT IGNORE INTO outlet_hours (outlet_id, day_of_week, open_time, close_time, pickup_start, pickup_end, is_closed, created_at, updated_at) VALUES
    (@o_ministry_dutch, 1, '12:00:00', '22:30:00', '12:00:00', '22:00:00', false, @now, @now),
    (@o_ministry_dutch, 2, '12:00:00', '22:30:00', '12:00:00', '22:00:00', false, @now, @now),
    (@o_ministry_dutch, 3, '12:00:00', '22:30:00', '12:00:00', '22:00:00', false, @now, @now),
    (@o_ministry_dutch, 4, '12:00:00', '22:30:00', '12:00:00', '22:00:00', false, @now, @now),
    (@o_ministry_dutch, 5, '12:00:00', '23:00:00', '12:00:00', '22:30:00', false, @now, @now),
    (@o_ministry_dutch, 6, '12:00:00', '23:00:00', '12:00:00', '22:30:00', false, @now, @now),
    (@o_ministry_dutch, 0, '12:00:00', '22:00:00', '12:00:00', '21:30:00', false, @now, @now);

-- Kefi - Park Street Mews
INSERT IGNORE INTO outlet_hours (outlet_id, day_of_week, open_time, close_time, pickup_start, pickup_end, is_closed, created_at, updated_at) VALUES
    (@o_kefi_park, 1, '11:30:00', '23:00:00', '11:30:00', '22:30:00', false, @now, @now),
    (@o_kefi_park, 2, '11:30:00', '23:00:00', '11:30:00', '22:30:00', false, @now, @now),
    (@o_kefi_park, 3, '11:30:00', '23:00:00', '11:30:00', '22:30:00', false, @now, @now),
    (@o_kefi_park, 4, '11:30:00', '23:00:00', '11:30:00', '22:30:00', false, @now, @now),
    (@o_kefi_park, 5, '11:30:00', '23:30:00', '11:30:00', '23:00:00', false, @now, @now),
    (@o_kefi_park, 6, '11:30:00', '23:30:00', '11:30:00', '23:00:00', false, @now, @now),
    (@o_kefi_park, 0, '11:30:00', '22:30:00', '11:30:00', '22:00:00', false, @now, @now);

-- Cinnamon Grand - Plates Restaurant
INSERT IGNORE INTO outlet_hours (outlet_id, day_of_week, open_time, close_time, pickup_start, pickup_end, is_closed, created_at, updated_at) VALUES
    (@o_cinnamon_plates, 1, '06:30:00', '23:00:00', '06:30:00', '22:30:00', false, @now, @now),
    (@o_cinnamon_plates, 2, '06:30:00', '23:00:00', '06:30:00', '22:30:00', false, @now, @now),
    (@o_cinnamon_plates, 3, '06:30:00', '23:00:00', '06:30:00', '22:30:00', false, @now, @now),
    (@o_cinnamon_plates, 4, '06:30:00', '23:00:00', '06:30:00', '22:30:00', false, @now, @now),
    (@o_cinnamon_plates, 5, '06:30:00', '23:30:00', '06:30:00', '23:00:00', false, @now, @now),
    (@o_cinnamon_plates, 6, '06:30:00', '23:30:00', '06:30:00', '23:00:00', false, @now, @now),
    (@o_cinnamon_plates, 0, '06:30:00', '23:00:00', '06:30:00', '22:30:00', false, @now, @now);

-- Perera & Sons - Colombo 07
INSERT IGNORE INTO outlet_hours (outlet_id, day_of_week, open_time, close_time, pickup_start, pickup_end, is_closed, created_at, updated_at) VALUES
    (@o_perera_c7, 1, '06:00:00', '21:00:00', '06:00:00', '20:30:00', false, @now, @now),
    (@o_perera_c7, 2, '06:00:00', '21:00:00', '06:00:00', '20:30:00', false, @now, @now),
    (@o_perera_c7, 3, '06:00:00', '21:00:00', '06:00:00', '20:30:00', false, @now, @now),
    (@o_perera_c7, 4, '06:00:00', '21:00:00', '06:00:00', '20:30:00', false, @now, @now),
    (@o_perera_c7, 5, '06:00:00', '21:00:00', '06:00:00', '20:30:00', false, @now, @now),
    (@o_perera_c7, 6, '06:00:00', '21:00:00', '06:00:00', '20:30:00', false, @now, @now),
    (@o_perera_c7, 0, '06:30:00', '20:00:00', '06:30:00', '19:30:00', false, @now, @now);

-- Keells Super - Liberty Plaza
INSERT IGNORE INTO outlet_hours (outlet_id, day_of_week, open_time, close_time, pickup_start, pickup_end, is_closed, created_at, updated_at) VALUES
    (@o_keells_liberty, 1, '08:00:00', '21:00:00', '08:00:00', '20:30:00', false, @now, @now),
    (@o_keells_liberty, 2, '08:00:00', '21:00:00', '08:00:00', '20:30:00', false, @now, @now),
    (@o_keells_liberty, 3, '08:00:00', '21:00:00', '08:00:00', '20:30:00', false, @now, @now),
    (@o_keells_liberty, 4, '08:00:00', '21:00:00', '08:00:00', '20:30:00', false, @now, @now),
    (@o_keells_liberty, 5, '08:00:00', '21:00:00', '08:00:00', '20:30:00', false, @now, @now),
    (@o_keells_liberty, 6, '08:00:00', '21:00:00', '08:00:00', '20:30:00', false, @now, @now),
    (@o_keells_liberty, 0, '09:00:00', '20:00:00', '09:00:00', '19:30:00', false, @now, @now);

-- ====================================================================
-- 6. ITEMS
-- ====================================================================
INSERT IGNORE INTO items (merchant_id, name, description, item_type, category, base_price, sale_price, image_url, dietary_info, status, created_at, updated_at) VALUES
    -- Ministry of Crab Items
    (@m_ministry,
     'Crab Curry Mystery Box',
     'Chef''s selection of premium crab preparations including curry, garlic, and pepper crab with rice and accompaniments.',
     'MYSTERY_BOX',
     'SEAFOOD',
     8500.00,
     4250.00,
     'https://resqeats.lk/items/ministry-crab-box.jpg',
     '{"vegetarian": false, "vegan": false, "glutenFree": false, "halal": false, "allergens": ["SHELLFISH", "DAIRY"]}',
     'ACTIVE',
     @now, @now),
    (@m_ministry,
     'Seafood Platter for Two',
     'Assorted prawns, calamari, and fish with garlic butter sauce and sides.',
     'SINGLE_ITEM',
     'SEAFOOD',
     6500.00,
     3250.00,
     'https://resqeats.lk/items/seafood-platter.jpg',
     '{"vegetarian": false, "vegan": false, "glutenFree": false, "halal": false, "allergens": ["SHELLFISH", "DAIRY", "GLUTEN"]}',
     'ACTIVE',
     @now, @now),
    
    -- Kefi Colombo Items
    (@m_kefi,
     'Mediterranean Mezze Box',
     'Selection of hummus, falafel, tzatziki, grilled halloumi, pita bread, and Greek salad.',
     'MYSTERY_BOX',
     'MEDITERRANEAN',
     3800.00,
     1900.00,
     'https://resqeats.lk/items/kefi-mezze.jpg',
     '{"vegetarian": true, "vegan": false, "glutenFree": false, "halal": true, "allergens": ["DAIRY", "GLUTEN", "SESAME"]}',
     'ACTIVE',
     @now, @now),
    (@m_kefi,
     'Greek Lamb Souvlaki Plate',
     'Tender lamb skewers with tzatziki, Greek salad, and crispy pita.',
     'SINGLE_ITEM',
     'MEDITERRANEAN',
     2800.00,
     1400.00,
     'https://resqeats.lk/items/lamb-souvlaki.jpg',
     '{"vegetarian": false, "vegan": false, "glutenFree": false, "halal": true, "allergens": ["DAIRY", "GLUTEN"]}',
     'ACTIVE',
     @now, @now),
    
    -- Cinnamon Grand Items
    (@m_cinnamon,
     'Buffet Surplus Box - Large',
     'Chef''s selection of international buffet items including appetizers, mains, and desserts.',
     'MYSTERY_BOX',
     'MIXED',
     4500.00,
     2250.00,
     'https://resqeats.lk/items/cinnamon-buffet-large.jpg',
     '{"vegetarian": false, "vegan": false, "glutenFree": false, "halal": true, "allergens": ["DAIRY", "GLUTEN", "NUTS", "EGGS"]}',
     'ACTIVE',
     @now, @now),
    (@m_cinnamon,
     'Buffet Surplus Box - Medium',
     'Selection of quality surplus items from the evening buffet spread.',
     'MYSTERY_BOX',
     'MIXED',
     2800.00,
     1400.00,
     'https://resqeats.lk/items/cinnamon-buffet-medium.jpg',
     '{"vegetarian": false, "vegan": false, "glutenFree": false, "halal": true, "allergens": ["DAIRY", "GLUTEN", "NUTS"]}',
     'ACTIVE',
     @now, @now),
    (@m_cinnamon,
     'Dim Sum Platter',
     'Assorted steamed and fried dim sum including har gow, siu mai, and spring rolls.',
     'SINGLE_ITEM',
     'ASIAN',
     2200.00,
     1100.00,
     'https://resqeats.lk/items/dim-sum-platter.jpg',
     '{"vegetarian": false, "vegan": false, "glutenFree": false, "halal": false, "allergens": ["SHELLFISH", "GLUTEN", "EGGS", "SOY"]}',
     'ACTIVE',
     @now, @now),
    
    -- Perera & Sons Items
    (@m_perera,
     'Bakery Surprise Box',
     'Assortment of fresh bread, buns, pastries, and short eats from the day''s baking.',
     'MYSTERY_BOX',
     'BAKERY',
     1200.00,
     600.00,
     'https://resqeats.lk/items/perera-bakery-box.jpg',
     '{"vegetarian": true, "vegan": false, "glutenFree": false, "halal": true, "allergens": ["GLUTEN", "DAIRY", "EGGS"]}',
     'ACTIVE',
     @now, @now),
    (@m_perera,
     'Short Eats Combo (12 pieces)',
     'Popular Sri Lankan snacks including fish buns, vegetable patties, rolls, and cutlets.',
     'SINGLE_ITEM',
     'SNACKS',
     780.00,
     390.00,
     'https://resqeats.lk/items/short-eats.jpg',
     '{"vegetarian": false, "vegan": false, "glutenFree": false, "halal": false, "allergens": ["GLUTEN", "EGGS", "FISH"]}',
     'ACTIVE',
     @now, @now),
    (@m_perera,
     'Cake Slice Selection (3 pieces)',
     'Three slices of assorted cakes including chocolate, vanilla, and fruit cake.',
     'SINGLE_ITEM',
     'DESSERTS',
     950.00,
     475.00,
     'https://resqeats.lk/items/cake-slices.jpg',
     '{"vegetarian": true, "vegan": false, "glutenFree": false, "halal": true, "allergens": ["GLUTEN", "DAIRY", "EGGS", "NUTS"]}',
     'ACTIVE',
     @now, @now),
    
    -- Keells Super Items
    (@m_keells,
     'Fresh Vegetable Box',
     '3kg assortment of fresh vegetables including carrots, beans, tomatoes, and leafy greens.',
     'SINGLE_ITEM',
     'PRODUCE',
     1500.00,
     750.00,
     'https://resqeats.lk/items/keells-veg-box.jpg',
     '{"vegetarian": true, "vegan": true, "glutenFree": true, "halal": true, "allergens": []}',
     'ACTIVE',
     @now, @now),
    (@m_keells,
     'Fruit Basket Surprise',
     'Seasonal fresh fruits including bananas, papayas, mangoes, and apples.',
     'MYSTERY_BOX',
     'PRODUCE',
     1800.00,
     900.00,
     'https://resqeats.lk/items/keells-fruit.jpg',
     '{"vegetarian": true, "vegan": true, "glutenFree": true, "halal": true, "allergens": []}',
     'ACTIVE',
     @now, @now),
    (@m_keells,
     'Dairy Essentials Pack',
     'Fresh milk, curd, cheese, and butter approaching best-before date.',
     'SINGLE_ITEM',
     'DAIRY',
     1650.00,
     825.00,
     'https://resqeats.lk/items/keells-dairy.jpg',
     '{"vegetarian": true, "vegan": false, "glutenFree": false, "halal": true, "allergens": ["DAIRY", "LACTOSE"]}',
     'ACTIVE',
     @now, @now),
    (@m_keells,
     'Ready-to-Eat Meals Pack',
     'Selection of prepared meals including rice and curry, fried rice, and kottu.',
     'MYSTERY_BOX',
     'MEALS',
     2200.00,
     1100.00,
     'https://resqeats.lk/items/keells-meals.jpg',
     '{"vegetarian": false, "vegan": false, "glutenFree": false, "halal": true, "allergens": ["GLUTEN", "EGGS", "SOY"]}',
     'ACTIVE',
     @now, @now);

-- Capture item IDs
SELECT id INTO @i_crab_box FROM items WHERE merchant_id = @m_ministry AND name = 'Crab Curry Mystery Box' LIMIT 1;
SELECT id INTO @i_seafood_platter FROM items WHERE merchant_id = @m_ministry AND name = 'Seafood Platter for Two' LIMIT 1;
SELECT id INTO @i_mezze_box FROM items WHERE merchant_id = @m_kefi AND name = 'Mediterranean Mezze Box' LIMIT 1;
SELECT id INTO @i_lamb_souvlaki FROM items WHERE merchant_id = @m_kefi AND name = 'Greek Lamb Souvlaki Plate' LIMIT 1;
SELECT id INTO @i_buffet_large FROM items WHERE merchant_id = @m_cinnamon AND name = 'Buffet Surplus Box - Large' LIMIT 1;
SELECT id INTO @i_buffet_medium FROM items WHERE merchant_id = @m_cinnamon AND name = 'Buffet Surplus Box - Medium' LIMIT 1;
SELECT id INTO @i_dim_sum FROM items WHERE merchant_id = @m_cinnamon AND name = 'Dim Sum Platter' LIMIT 1;
SELECT id INTO @i_bakery_box FROM items WHERE merchant_id = @m_perera AND name = 'Bakery Surprise Box' LIMIT 1;
SELECT id INTO @i_short_eats FROM items WHERE merchant_id = @m_perera AND name = 'Short Eats Combo (12 pieces)' LIMIT 1;
SELECT id INTO @i_cake_slices FROM items WHERE merchant_id = @m_perera AND name = 'Cake Slice Selection (3 pieces)' LIMIT 1;
SELECT id INTO @i_veg_box FROM items WHERE merchant_id = @m_keells AND name = 'Fresh Vegetable Box' LIMIT 1;
SELECT id INTO @i_fruit_basket FROM items WHERE merchant_id = @m_keells AND name = 'Fruit Basket Surprise' LIMIT 1;
SELECT id INTO @i_dairy_pack FROM items WHERE merchant_id = @m_keells AND name = 'Dairy Essentials Pack' LIMIT 1;
SELECT id INTO @i_meals_pack FROM items WHERE merchant_id = @m_keells AND name = 'Ready-to-Eat Meals Pack' LIMIT 1;

-- ====================================================================
-- 7. OUTLET ITEMS
-- ====================================================================
INSERT IGNORE INTO outlet_items (outlet_id, item_id, current_quantity, is_available, created_at, updated_at) VALUES
    -- Ministry of Crab - Dutch Hospital
    (@o_ministry_dutch, @i_crab_box, 8, true, @now, @now),
    (@o_ministry_dutch, @i_seafood_platter, 5, true, @now, @now),
    
    -- Kefi - Park Street Mews
    (@o_kefi_park, @i_mezze_box, 12, true, @now, @now),
    (@o_kefi_park, @i_lamb_souvlaki, 8, true, @now, @now),
    
    -- Cinnamon Grand - Plates Restaurant
    (@o_cinnamon_plates, @i_buffet_large, 15, true, @now, @now),
    (@o_cinnamon_plates, @i_buffet_medium, 20, true, @now, @now),
    
    -- Cinnamon Grand - Noodles Restaurant
    (@o_cinnamon_noodles, @i_dim_sum, 10, true, @now, @now),
    
    -- Perera & Sons - Colombo 07
    (@o_perera_c7, @i_bakery_box, 25, true, @now, @now),
    (@o_perera_c7, @i_short_eats, 40, true, @now, @now),
    (@o_perera_c7, @i_cake_slices, 15, true, @now, @now),
    
    -- Perera & Sons - Nugegoda
    (@o_perera_nugegoda, @i_bakery_box, 30, true, @now, @now),
    (@o_perera_nugegoda, @i_short_eats, 50, true, @now, @now),
    
    -- Keells Super - Liberty Plaza
    (@o_keells_liberty, @i_veg_box, 35, true, @now, @now),
    (@o_keells_liberty, @i_fruit_basket, 25, true, @now, @now),
    (@o_keells_liberty, @i_dairy_pack, 20, true, @now, @now),
    (@o_keells_liberty, @i_meals_pack, 18, true, @now, @now),
    
    -- Keells Super - Crescat Boulevard
    (@o_keells_crescat, @i_veg_box, 40, true, @now, @now),
    (@o_keells_crescat, @i_fruit_basket, 30, true, @now, @now),
    (@o_keells_crescat, @i_dairy_pack, 25, true, @now, @now);

-- ====================================================================
-- 8. ORDERS
-- ====================================================================
INSERT IGNORE INTO orders (user_id, outlet_id, order_number, status, subtotal, tax, total, pickup_code, pickup_by, notes, created_at, updated_at) VALUES
    (@u_nimal, @o_ministry_dutch, 'RQE-2025-000001', 'COMPLETED', 7500.00, 0.00, 6750.00, '847291', DATE_ADD(@now, INTERVAL 2 HOUR), 'Please pack carefully, will be travelling', DATE_SUB(@now, INTERVAL 3 DAY), DATE_SUB(@now, INTERVAL 3 DAY)),
    (@u_nimal, @o_perera_c7, 'RQE-2025-000002', 'READY', 1170.00, 0.00, 1053.00, '395847', DATE_ADD(@now, INTERVAL 1 HOUR), NULL, DATE_SUB(@now, INTERVAL 1 HOUR), DATE_SUB(@now, INTERVAL 30 MINUTE)),
    (@u_kumari, @o_keells_liberty, 'RQE-2025-000003', 'PENDING', 2475.00, 0.00, 2227.50, NULL, DATE_ADD(@now, INTERVAL 3 HOUR), 'Call on arrival - Gate B entrance', @now, @now),
    (@u_ashan, @o_kefi_park, 'RQE-2025-000004', 'CONFIRMED', 3300.00, 0.00, 2970.00, '629481', DATE_ADD(@now, INTERVAL 4 HOUR), 'Vegetarian preference', DATE_SUB(@now, INTERVAL 2 HOUR), DATE_SUB(@now, INTERVAL 1 HOUR)),
    (@u_dilini, @o_cinnamon_plates, 'RQE-2025-000005', 'CANCELLED', 4500.00, 0.00, 4050.00, NULL, DATE_ADD(@now, INTERVAL 5 HOUR), 'Cancelled - schedule conflict', DATE_SUB(@now, INTERVAL 1 DAY), DATE_SUB(@now, INTERVAL 12 HOUR));

-- Capture order IDs
SELECT id INTO @ord_1 FROM orders WHERE order_number = 'RQE-2025-000001' LIMIT 1;
SELECT id INTO @ord_2 FROM orders WHERE order_number = 'RQE-2025-000002' LIMIT 1;
SELECT id INTO @ord_3 FROM orders WHERE order_number = 'RQE-2025-000003' LIMIT 1;
SELECT id INTO @ord_4 FROM orders WHERE order_number = 'RQE-2025-000004' LIMIT 1;
SELECT id INTO @ord_5 FROM orders WHERE order_number = 'RQE-2025-000005' LIMIT 1;

-- ====================================================================
-- 9. ORDER ITEMS
-- ====================================================================
INSERT IGNORE INTO order_items (order_id, item_id, item_name, quantity, unit_price, total_price, created_at, updated_at) VALUES
    -- Order 1 items (Ministry of Crab)
    (@ord_1, @i_crab_box, 'Crab Curry Mystery Box', 1, 4250.00, 4250.00, DATE_SUB(@now, INTERVAL 3 DAY), DATE_SUB(@now, INTERVAL 3 DAY)),
    (@ord_1, @i_seafood_platter, 'Seafood Platter for Two', 1, 3250.00, 3250.00, DATE_SUB(@now, INTERVAL 3 DAY), DATE_SUB(@now, INTERVAL 3 DAY)),

    -- Order 2 items (Perera & Sons)
    (@ord_2, @i_bakery_box, 'Bakery Surprise Box', 1, 600.00, 600.00, DATE_SUB(@now, INTERVAL 1 HOUR), DATE_SUB(@now, INTERVAL 1 HOUR)),
    (@ord_2, @i_short_eats, 'Short Eats Combo (12 pieces)', 1, 390.00, 390.00, DATE_SUB(@now, INTERVAL 1 HOUR), DATE_SUB(@now, INTERVAL 1 HOUR)),
    (@ord_2, @i_cake_slices, 'Cake Slice Selection (3 pieces)', 1, 475.00, 475.00, DATE_SUB(@now, INTERVAL 1 HOUR), DATE_SUB(@now, INTERVAL 1 HOUR)),

    -- Order 3 items (Keells Super)
    (@ord_3, @i_veg_box, 'Fresh Vegetable Box', 1, 750.00, 750.00, @now, @now),
    (@ord_3, @i_fruit_basket, 'Fruit Basket Surprise', 1, 900.00, 900.00, @now, @now),
    (@ord_3, @i_dairy_pack, 'Dairy Essentials Pack', 1, 825.00, 825.00, @now, @now),

    -- Order 4 items (Kefi Colombo)
    (@ord_4, @i_mezze_box, 'Mediterranean Mezze Box', 1, 1900.00, 1900.00, DATE_SUB(@now, INTERVAL 2 HOUR), DATE_SUB(@now, INTERVAL 2 HOUR)),
    (@ord_4, @i_lamb_souvlaki, 'Greek Lamb Souvlaki Plate', 1, 1400.00, 1400.00, DATE_SUB(@now, INTERVAL 2 HOUR), DATE_SUB(@now, INTERVAL 2 HOUR)),

    -- Order 5 items (Cinnamon Grand - Cancelled)
    (@ord_5, @i_buffet_large, 'Buffet Surplus Box - Large', 2, 2250.00, 4500.00, DATE_SUB(@now, INTERVAL 1 DAY), DATE_SUB(@now, INTERVAL 1 DAY));

-- ====================================================================
-- 10. PAYMENTS
-- ====================================================================
INSERT IGNORE INTO payments (order_id, amount, status, payment_method_token, ipg_transaction_id, created_at, updated_at) VALUES
    (@ord_1, 6750.00, 'COMPLETED', NULL, 'VISA-4532-XXX-847291', DATE_SUB(@now, INTERVAL 3 DAY), DATE_SUB(@now, INTERVAL 3 DAY)),
    (@ord_2, 1053.00, 'COMPLETED', NULL, 'MASTER-5412-XXX-395847', DATE_SUB(@now, INTERVAL 1 HOUR), DATE_SUB(@now, INTERVAL 1 HOUR)),
    (@ord_3, 2227.50, 'PENDING', NULL, NULL, @now, @now),
    (@ord_4, 2970.00, 'COMPLETED', NULL, 'VISA-4716-XXX-629481', DATE_SUB(@now, INTERVAL 2 HOUR), DATE_SUB(@now, INTERVAL 2 HOUR)),
    (@ord_5, 4050.00, 'REFUNDED', NULL, 'AMEX-3742-XXX-REFUND', DATE_SUB(@now, INTERVAL 1 DAY), DATE_SUB(@now, INTERVAL 12 HOUR));

-- ====================================================================
-- 11. NOTIFICATIONS
-- ====================================================================
INSERT IGNORE INTO notifications (user_id, title, message, type, status, data, created_at, updated_at) VALUES
    (@u_nimal,
     'Order Ready for Pickup!',
     'Your order RQE-2025-000002 is ready for pickup at Perera & Sons - Colombo 07. Your pickup code is 395847.',
     'ORDER_UPDATE',
     'UNREAD',
     CONCAT('{"orderId": ', @ord_2, ', "outletId": ', @o_perera_c7, ', "pickupCode": "395847"}'),
     DATE_SUB(@now, INTERVAL 30 MINUTE),
     DATE_SUB(@now, INTERVAL 30 MINUTE)),
    (@u_nimal,
     'Order Completed - Thank You!',
     'Your order RQE-2025-000001 from Ministry of Crab has been completed. You helped save 2.5kg of food from waste!',
     'ORDER_UPDATE',
     'READ',
     CONCAT('{"orderId": ', @ord_1, ', "foodSaved": "2.5kg"}'),
     DATE_SUB(@now, INTERVAL 3 DAY),
     DATE_SUB(@now, INTERVAL 3 DAY)),
    (@u_kumari,
     'Order Placed Successfully',
     'Your order RQE-2025-000003 has been placed. We''ll notify you when it''s ready for pickup at Keells Super - Liberty Plaza.',
     'ORDER_UPDATE',
     'UNREAD',
     CONCAT('{"orderId": ', @ord_3, '}'),
     @now,
     @now),
    (@u_ashan,
     'Order Confirmed',
     'Great news! Kefi - Park Street Mews has confirmed your order RQE-2025-000004. Pickup code: 629481.',
     'ORDER_UPDATE',
     'UNREAD',
     CONCAT('{"orderId": ', @ord_4, ', "pickupCode": "629481"}'),
     DATE_SUB(@now, INTERVAL 1 HOUR),
     DATE_SUB(@now, INTERVAL 1 HOUR)),
    (@u_damitha,
     'New Order Received',
     'You have a new order RQE-2025-000002 at Perera & Sons - Colombo 07. Please prepare for pickup.',
     'ORDER_UPDATE',
     'UNREAD',
     CONCAT('{"orderId": ', @ord_2, ', "outletId": ', @o_perera_c7, '}'),
     DATE_SUB(@now, INTERVAL 1 HOUR),
     DATE_SUB(@now, INTERVAL 1 HOUR)),
    (@u_dilini,
     'Order Cancelled & Refunded',
     'Your order RQE-2025-000005 has been cancelled. A full refund of Rs. 4,050 has been processed to your card.',
     'ORDER_UPDATE',
     'READ',
     CONCAT('{"orderId": ', @ord_5, ', "refundAmount": 4050.00}'),
     DATE_SUB(@now, INTERVAL 12 HOUR),
     DATE_SUB(@now, INTERVAL 12 HOUR)),
    (@u_nimal,
     'Flash Sale Alert!',
     '50% off at Ministry of Crab tonight! Premium crab dishes available from 8:30 PM.',
     'PROMOTION',
     'UNREAD',
     CONCAT('{"merchantId": ', @m_ministry, ', "discount": "50%"}'),
     DATE_SUB(@now, INTERVAL 4 HOUR),
     DATE_SUB(@now, INTERVAL 4 HOUR));

-- ====================================================================
-- 12. OUTLET STAFF USERS (OUTLET_USER role)
-- ====================================================================
INSERT IGNORE INTO users (email, password_hash, first_name, last_name, phone, role, status, email_verified, phone_verified, oauth2_provider, created_at, updated_at) VALUES
        ('priya.fernando@ministryofcrab.com',
         '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
         'Priya', 'Fernando', '+94771111222',
         'OUTLET_USER', 'ACTIVE', true, true, 'LOCAL', @now, @now),
        ('saman.jayasuriya@pereraandsons.lk',
         '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
         'Saman', 'Jayasuriya', '+94772222333',
         'OUTLET_USER', 'ACTIVE', true, true, 'LOCAL', @now, @now),
        ('chamari.silva@keells.com',
         '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
         'Chamari', 'Silva', '+94773333444',
         'OUTLET_USER', 'ACTIVE', true, true, 'LOCAL', @now, @now),
        ('nuwan.bandara@cinnamonhotels.com',
         '$2a$10$hs.1qNMqFHBWjJW/B.bW4uEtq8zXNzJZj8F5hH5p.dPWqYB7vVL8O',
         'Nuwan', 'Bandara', '+94774444555',
         'OUTLET_USER', 'ACTIVE', true, true, 'LOCAL', @now, @now);

-- Back-fill outlet_id for outlet staff
UPDATE users SET outlet_id = @o_ministry_dutch WHERE email = 'priya.fernando@ministryofcrab.com' AND (outlet_id IS NULL OR outlet_id <> @o_ministry_dutch);
UPDATE users SET outlet_id = @o_perera_c7 WHERE email = 'saman.jayasuriya@pereraandsons.lk' AND (outlet_id IS NULL OR outlet_id <> @o_perera_c7);
UPDATE users SET outlet_id = @o_keells_liberty WHERE email = 'chamari.silva@keells.com' AND (outlet_id IS NULL OR outlet_id <> @o_keells_liberty);
UPDATE users SET outlet_id = @o_cinnamon_plates WHERE email = 'nuwan.bandara@cinnamonhotels.com' AND (outlet_id IS NULL OR outlet_id <> @o_cinnamon_plates);

-- Capture staff user IDs (optional)
SELECT id INTO @u_staff_priya FROM users WHERE email = 'priya.fernando@ministryofcrab.com' LIMIT 1;
SELECT id INTO @u_staff_saman FROM users WHERE email = 'saman.jayasuriya@pereraandsons.lk' LIMIT 1;
SELECT id INTO @u_staff_chamari FROM users WHERE email = 'chamari.silva@keells.com' LIMIT 1;
SELECT id INTO @u_staff_nuwan FROM users WHERE email = 'nuwan.bandara@cinnamonhotels.com' LIMIT 1;

-- ====================================================================
-- 13. FOREIGN KEYS (idempotent)
-- ====================================================================

-- Helper pattern: check referenced relationship exists; add FK only if missing.

-- merchants.owner_user_id -> users.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'merchants' AND COLUMN_NAME = 'owner_user_id'
    AND REFERENCED_TABLE_NAME = 'users' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE merchants ADD CONSTRAINT fk_merchants_owner_user FOREIGN KEY (owner_user_id) REFERENCES users(id)',
    'SELECT "fk_merchants_owner_user exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- merchants.approved_by -> users.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'merchants' AND COLUMN_NAME = 'approved_by'
    AND REFERENCED_TABLE_NAME = 'users' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE merchants ADD CONSTRAINT fk_merchants_approved_by FOREIGN KEY (approved_by) REFERENCES users(id)',
    'SELECT "fk_merchants_approved_by exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- outlets.merchant_id -> merchants.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outlets' AND COLUMN_NAME = 'merchant_id'
    AND REFERENCED_TABLE_NAME = 'merchants' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE outlets ADD CONSTRAINT fk_outlets_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id)',
    'SELECT "fk_outlets_merchant exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- users.merchant_id -> merchants.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'merchant_id'
    AND REFERENCED_TABLE_NAME = 'merchants' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE users ADD CONSTRAINT fk_users_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id)',
    'SELECT "fk_users_merchant exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- users.outlet_id -> outlets.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'outlet_id'
    AND REFERENCED_TABLE_NAME = 'outlets' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE users ADD CONSTRAINT fk_users_outlet FOREIGN KEY (outlet_id) REFERENCES outlets(id)',
    'SELECT "fk_users_outlet exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- outlet_hours.outlet_id -> outlets.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outlet_hours' AND COLUMN_NAME = 'outlet_id'
    AND REFERENCED_TABLE_NAME = 'outlets' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE outlet_hours ADD CONSTRAINT fk_outlet_hours_outlet FOREIGN KEY (outlet_id) REFERENCES outlets(id)',
    'SELECT "fk_outlet_hours_outlet exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- items.merchant_id -> merchants.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'items' AND COLUMN_NAME = 'merchant_id'
    AND REFERENCED_TABLE_NAME = 'merchants' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE items ADD CONSTRAINT fk_items_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id)',
    'SELECT "fk_items_merchant exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- outlet_items.outlet_id -> outlets.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outlet_items' AND COLUMN_NAME = 'outlet_id'
    AND REFERENCED_TABLE_NAME = 'outlets' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE outlet_items ADD CONSTRAINT fk_outlet_items_outlet FOREIGN KEY (outlet_id) REFERENCES outlets(id)',
    'SELECT "fk_outlet_items_outlet exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- outlet_items.item_id -> items.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outlet_items' AND COLUMN_NAME = 'item_id'
    AND REFERENCED_TABLE_NAME = 'items' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE outlet_items ADD CONSTRAINT fk_outlet_items_item FOREIGN KEY (item_id) REFERENCES items(id)',
    'SELECT "fk_outlet_items_item exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- orders.user_id -> users.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'user_id'
    AND REFERENCED_TABLE_NAME = 'users' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE orders ADD CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)',
    'SELECT "fk_orders_user exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- orders.outlet_id -> outlets.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'outlet_id'
    AND REFERENCED_TABLE_NAME = 'outlets' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE orders ADD CONSTRAINT fk_orders_outlet FOREIGN KEY (outlet_id) REFERENCES outlets(id)',
    'SELECT "fk_orders_outlet exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- order_items.order_id -> orders.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_items' AND COLUMN_NAME = 'order_id'
    AND REFERENCED_TABLE_NAME = 'orders' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE order_items ADD CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id)',
    'SELECT "fk_order_items_order exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- order_items.item_id -> items.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_items' AND COLUMN_NAME = 'item_id'
    AND REFERENCED_TABLE_NAME = 'items' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE order_items ADD CONSTRAINT fk_order_items_item FOREIGN KEY (item_id) REFERENCES items(id)',
    'SELECT "fk_order_items_item exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- payments.order_id -> orders.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payments' AND COLUMN_NAME = 'order_id'
    AND REFERENCED_TABLE_NAME = 'orders' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE payments ADD CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id)',
    'SELECT "fk_payments_order exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- payments.payment_method_id -> payment_methods.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payments' AND COLUMN_NAME = 'payment_method_id'
    AND REFERENCED_TABLE_NAME = 'payment_methods' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE payments ADD CONSTRAINT fk_payments_payment_method FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)',
    'SELECT "fk_payments_payment_method exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- payment_methods.user_id -> users.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment_methods' AND COLUMN_NAME = 'user_id'
    AND REFERENCED_TABLE_NAME = 'users' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE payment_methods ADD CONSTRAINT fk_payment_methods_user FOREIGN KEY (user_id) REFERENCES users(id)',
    'SELECT "fk_payment_methods_user exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- notifications.user_id -> users.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notifications' AND COLUMN_NAME = 'user_id'
    AND REFERENCED_TABLE_NAME = 'users' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE notifications ADD CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id)',
    'SELECT "fk_notifications_user exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- notifications.order_id -> orders.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notifications' AND COLUMN_NAME = 'order_id'
    AND REFERENCED_TABLE_NAME = 'orders' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE notifications ADD CONSTRAINT fk_notifications_order FOREIGN KEY (order_id) REFERENCES orders(id)',
    'SELECT "fk_notifications_order exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- notifications.outlet_id -> outlets.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notifications' AND COLUMN_NAME = 'outlet_id'
    AND REFERENCED_TABLE_NAME = 'outlets' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE notifications ADD CONSTRAINT fk_notifications_outlet FOREIGN KEY (outlet_id) REFERENCES outlets(id)',
    'SELECT "fk_notifications_outlet exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- refresh_tokens.user_id -> users.id
SELECT COUNT(1) INTO @cnt
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'refresh_tokens' AND COLUMN_NAME = 'user_id'
    AND REFERENCED_TABLE_NAME = 'users' AND REFERENCED_COLUMN_NAME = 'id';
SET @sql = IF(@cnt = 0,
    'ALTER TABLE refresh_tokens ADD CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)',
    'SELECT "fk_refresh_tokens_user exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ====================================================================
-- 14. INDEXES (idempotent)
-- ====================================================================
-- Note: MySQL doesn't support GIST indexes, using standard indexes instead
-- These may already exist from schema file

-- Create indexes only if they don't already exist (safe for repeated runs)
-- idx_outlet_location (matches JPA @Index)
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outlets' AND INDEX_NAME = 'idx_outlet_location';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_outlet_location ON outlets (latitude, longitude);', 'SELECT "idx_outlet_location already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_outlet_status (matches JPA @Index)
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outlets' AND INDEX_NAME = 'idx_outlet_status';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_outlet_status ON outlets (status);', 'SELECT "idx_outlet_status already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_outlet_merchant (matches JPA @Index)
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outlets' AND INDEX_NAME = 'idx_outlet_merchant';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_outlet_merchant ON outlets (merchant_id);', 'SELECT "idx_outlet_merchant already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_outlet_item_outlet (matches JPA @Index)
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outlet_items' AND INDEX_NAME = 'idx_outlet_item_outlet';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_outlet_item_outlet ON outlet_items (outlet_id);', 'SELECT "idx_outlet_item_outlet already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_outlet_item_item (matches JPA @Index)
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outlet_items' AND INDEX_NAME = 'idx_outlet_item_item';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_outlet_item_item ON outlet_items (item_id);', 'SELECT "idx_outlet_item_item already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_order_user (matches JPA @Index)
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND INDEX_NAME = 'idx_order_user';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_order_user ON orders (user_id);', 'SELECT "idx_order_user already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_order_outlet (matches JPA @Index)
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND INDEX_NAME = 'idx_order_outlet';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_order_outlet ON orders (outlet_id);', 'SELECT "idx_order_outlet already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_order_status (matches JPA @Index)
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND INDEX_NAME = 'idx_order_status';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_order_status ON orders (status);', 'SELECT "idx_order_status already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_order_outlet_status (matches JPA @Index)
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND INDEX_NAME = 'idx_order_outlet_status';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_order_outlet_status ON orders (outlet_id, status);', 'SELECT "idx_order_outlet_status already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_notification_user (matches JPA @Index)
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notifications' AND INDEX_NAME = 'idx_notification_user';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_notification_user ON notifications (user_id);', 'SELECT "idx_notification_user already exists";');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- idx_notification_status (matches JPA @Index)
SELECT COUNT(1) INTO @cnt FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notifications' AND INDEX_NAME = 'idx_notification_status';
SET @sql = IF(@cnt = 0, 'CREATE INDEX idx_notification_status ON notifications (status);', 'SELECT "idx_notification_status already exists";');
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