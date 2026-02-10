-- ============================================
-- Food Rescue Hub - Fake Data Initialization
-- ============================================
-- This file will be automatically executed by Spring Boot on startup
-- note that it will be populated inside ec2 server upon running
-- Note: Fields with @CreationTimestamp and @UpdateTimestamp are auto-generated

-- Reset tables for deterministic seeding (commented out - using ddl-auto=create instead)
-- NOTE: These TRUNCATE statements are commented out to prevent data loss after schema creation
-- SET FOREIGN_KEY_CHECKS=0;
-- TRUNCATE TABLE pickup_feedback;
-- TRUNCATE TABLE pickup_tokens;
-- TRUNCATE TABLE payments;
-- TRUNCATE TABLE commission_ledger;
-- TRUNCATE TABLE order_items;
-- TRUNCATE TABLE orders;
-- TRUNCATE TABLE listing_dietary_tags;
-- TRUNCATE TABLE listing_photos;
-- TRUNCATE TABLE inventory;
-- TRUNCATE TABLE listings;
-- TRUNCATE TABLE store_types;
-- TRUNCATE TABLE stores;
-- TRUNCATE TABLE payouts;
-- TRUNCATE TABLE wallet_transactions;
-- TRUNCATE TABLE wallets;
-- TRUNCATE TABLE supplier_profiles;
-- TRUNCATE TABLE consumer_profiles;
-- TRUNCATE TABLE dietary_tags;
-- -- Recommender System Tables
-- TRUNCATE TABLE search_logs;
-- TRUNCATE TABLE user_store_interactions;
-- TRUNCATE TABLE store_stats;
-- TRUNCATE TABLE consumer_stats;
-- TRUNCATE TABLE listing_stats;
-- TRUNCATE TABLE store_ratings;
-- TRUNCATE TABLE user_interactions;
-- SET FOREIGN_KEY_CHECKS=1;

INSERT INTO dietary_tags (tag_name) VALUES
('Vegetarian'),
('Vegan'),
('Gluten-Free'),
('Halal'),
('Dairy-Free'),
('Nut-Free'),
('Low-Sugar'),
('Organic');

-- ============================================
-- 2. FOOD_CATEGORIES (CO2 reference data)
-- ============================================
INSERT INTO food_categories (id, name, kg_co2_per_kg) VALUES
(1, 'Beef (beef herd)', 99.48),
(2, 'Lamb & Mutton', 39.72),
(3, 'Cheese', 23.88),
(4, 'Fish (farmed)', 13.63),
(5, 'Poultry Meat', 9.87),
(6, 'Eggs', 4.67),
(7, 'Rice', 4.45),
(8, 'Milk', 3.15),
(9, 'Wheat & Rye (Bread)', 1.57),
(10, 'Vegetables', 0.53)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
kg_co2_per_kg = VALUES(kg_co2_per_kg);

-- ============================================
-- 3. CONSUMER_PROFILES
-- ============================================
-- BCrypt password for seeded users: "password123"
INSERT INTO consumer_profiles (email, password, phone, display_name, status, role, default_lat, default_lng, preferences_json, created_at) VALUES
('alice.tan@email.com', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6591234567', 'Alice Tan', 'ACTIVE', 'CONSUMER', 1.3521, 103.8198, '{"dietary": ["vegetarian"], "radius": 5}', NOW()),
('bob.lim@email.com', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6591234568', 'Bob Lim', 'ACTIVE', 'CONSUMER', 1.2897, 103.8501, '{"dietary": ["halal"], "radius": 3}', NOW()),
('charlie.wong@email.com', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6591234569', 'Charlie Wong', 'ACTIVE', 'CONSUMER', 1.3048, 103.8318, '{"dietary": [], "radius": 10}', NOW()),
('diana.ng@email.com', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6591234570', 'Diana Ng', 'ACTIVE', 'CONSUMER', 1.3329, 103.7436, '{"dietary": ["vegan", "gluten-free"], "radius": 5}', NOW()),
('emily.chen@email.com', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6591234571', 'Emily Chen', 'ACTIVE', 'CONSUMER', 1.3644, 103.9915, '{"dietary": ["dairy-free"], "radius": 7}', NOW()),
('frank.koh@email.com', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6591234572', 'Frank Koh', 'ACTIVE', 'CONSUMER', 1.2800, 103.8400, '{"dietary": [], "radius": 5}', NOW()),
('grace.lee@email.com', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6591234573', 'Grace Lee', 'ACTIVE', 'CONSUMER', 1.3200, 103.8600, '{"dietary": ["halal", "nut-free"], "radius": 8}', NOW()),
('henry.teo@email.com', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6591234574', 'Henry Teo', 'ACTIVE', 'CONSUMER', 1.3100, 103.8500, '{"dietary": ["organic"], "radius": 6}', NOW());

-- ============================================
-- 4. SUPPLIER_PROFILES
-- ============================================
INSERT INTO supplier_profiles (email, password, phone, display_name, status, role, business_name, created_at) VALUES
('bakery@breadtalk.sg', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6562345678', 'BreadTalk Manager', 'ACTIVE', 'SUPPLIER', 'BreadTalk Singapore Pte Ltd', NOW()),
('cafe@toastbox.sg', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6562345679', 'Toast Box Manager', 'ACTIVE', 'SUPPLIER', 'Toast Box Pte Ltd', NOW()),
('restaurant@paradise.sg', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6562345680', 'Paradise Dynasty Manager', 'ACTIVE', 'SUPPLIER', 'Paradise Group (Singapore) Pte Ltd', NOW()),
('market@fairprice.sg', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6562345681', 'FairPrice Manager', 'ACTIVE', 'SUPPLIER', 'FairPrice Singapore', NOW()),
('cafe@starbucks.sg', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6562345682', 'Starbucks Manager', 'ACTIVE', 'SUPPLIER', 'Starbucks Coffee Singapore', NOW()),
-- New suppliers near Alice's location
('tongtong@bakehouse.sg', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6562345683', 'Tong Tong Manager', 'ACTIVE', 'SUPPLIER', 'Tong Tong Bakehouse Pte Ltd', NOW()),
('greenbowl@healthy.sg', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6562345684', 'Green Bowl Manager', 'ACTIVE', 'SUPPLIER', 'Green Bowl Cafe Pte Ltd', NOW()),
('pasta@paradise.sg', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6562345685', 'Pasta Paradise Manager', 'ACTIVE', 'SUPPLIER', 'Pasta Paradise Restaurant', NOW()),
('sunrise@coffee.sg', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6562345686', 'Sunrise Manager', 'ACTIVE', 'SUPPLIER', 'Sunrise Coffee House Pte Ltd', NOW()),
('freshbites@deli.sg', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6562345687', 'Fresh Bites Manager', 'ACTIVE', 'SUPPLIER', 'Fresh Bites Deli Pte Ltd', NOW()),
('sweetheaven@dessert.sg', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6562345688', 'Sweet Heaven Manager', 'ACTIVE', 'SUPPLIER', 'Sweet Heaven Desserts Pte Ltd', NOW()),
('laksa@delight.sg', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6562345689', 'Laksa Delight Manager', 'ACTIVE', 'SUPPLIER', 'Laksa Delight Restaurant', NOW()),
('urban@bites.sg', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6562345690', 'Urban Bites Manager', 'ACTIVE', 'SUPPLIER', 'Urban Bites Kitchen Pte Ltd', NOW()),
('morning@glory.sg', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6562345691', 'Morning Glory Manager', 'ACTIVE', 'SUPPLIER', 'Morning Glory Bakery Pte Ltd', NOW()),
('spice@garden.sg', '$2a$10$a/8ZzMc.wpC7vec7MEQSWeS2rgKSE0Tco/sqyb1YXX0IHsDYzDip6', '+6562345692', 'Spice Garden Manager', 'ACTIVE', 'SUPPLIER', 'Spice Garden Restaurant Pte Ltd', NOW());

-- ============================================
-- 5. WALLETS (Depends on consumer_profiles)
-- ============================================
INSERT INTO wallets (consumer_id, balance, currency, updated_at) VALUES
(1, 50.00, 'SGD', NOW()),
(2, 75.50, 'SGD', NOW()),
(3, 100.00, 'SGD', NOW()),
(4, 25.00, 'SGD', NOW()),
(5, 150.00, 'SGD', NOW()),
(6, 80.00, 'SGD', NOW()),
(7, 60.00, 'SGD', NOW()),
(8, 90.00, 'SGD', NOW());

-- ============================================
-- 6. STORES (Depends on supplier_profiles)
-- ============================================
INSERT INTO stores (supplier_id, store_name, description, address_line, postal_code, lat, lng, pickup_instructions, opening_hours, is_active, created_at) VALUES
(1, 'BreadTalk Orchard', 'Fresh bakery items daily', '313 Orchard Road, #B2-01', '238895', 1.3048, 103.8318, 'Pick up at counter, show QR code', 'Mon-Sun: 10:00-22:00', true, NOW()),
(1, 'BreadTalk Tampines', 'Bakery outlet at Tampines Mall', '4 Tampines Central 5, #01-23', '529510', 1.3525, 103.9447, 'Counter pickup available', 'Mon-Sun: 10:00-22:00', true, NOW()),
(2, 'Toast Box Marina Bay', 'Traditional kaya toast and coffee', '8 Marina Boulevard, #01-12', '018981', 1.2797, 103.8543, 'Show order confirmation at counter', 'Mon-Fri: 07:00-21:00, Sat-Sun: 08:00-20:00', true, NOW()),
(2, 'Toast Box Jurong Point', 'Cozy cafe at Jurong Point', '1 Jurong West Central 2, #B1-45', '648886', 1.3396, 103.7060, 'Pick up at designated counter', 'Mon-Sun: 08:00-22:00', true, NOW()),
(3, 'Paradise Dynasty ION', 'Premium Chinese cuisine', '2 Orchard Turn, #04-20 ION Orchard', '238801', 1.3041, 103.8332, 'Reception desk pickup', 'Mon-Sun: 11:30-22:00', true, NOW()),
(3, 'Paradise Dynasty Vivocity', 'Authentic dim sum and noodles', '1 HarbourFront Walk, #02-118', '098585', 1.2644, 103.8220, 'Show QR at entrance', 'Mon-Sun: 11:00-22:00', true, NOW()),
(4, 'FairPrice Finest Bukit Timah', 'Premium supermarket', '170 Upper Bukit Timah Road, #B1-01', '588179', 1.3431, 103.7764, 'Customer service counter', 'Mon-Sun: 08:00-23:00', true, NOW()),
(4, 'FairPrice Xtra Ang Mo Kio', 'Large format supermarket', '53 Ang Mo Kio Avenue 3, #01-01', '569933', 1.3691, 103.8489, 'Pickup point near entrance', 'Mon-Sun: 07:00-23:00', true, NOW()),
(5, 'Starbucks Raffles Place', 'Coffee and pastries', '6 Raffles Quay, #01-12', '048580', 1.2812, 103.8517, 'Show mobile order at counter', 'Mon-Fri: 07:00-20:00, Sat-Sun: 08:00-18:00', true, NOW()),
(5, 'Starbucks Changi Airport', 'Airport outlet', '60 Airport Boulevard, #02-234', '819643', 1.3644, 103.9915, 'Counter pickup', 'Mon-Sun: 06:00-23:00', true, NOW()),
-- New stores near Alice's location (1.3521, 103.8198)
(6, 'Tong Tong Bakehouse Somerset', 'Artisan breads and pastries made fresh daily', '111 Somerset Road, #01-15', '238164', 1.3503, 103.8384, 'Pick up at main counter', 'Mon-Sun: 08:00-21:00', true, NOW()),
(7, 'Green Bowl Cafe Dhoby Ghaut', 'Healthy bowls and fresh juices', '68 Orchard Road, Plaza Singapura #B1-20', '238839', 1.3506, 103.8455, 'Food court level B1, counter 20', 'Mon-Sun: 10:00-22:00', true, NOW()),
(8, 'Pasta Paradise Fort Canning', 'Authentic Italian cuisine with fresh ingredients', '5 Fort Canning Road, #01-01', '179493', 1.3450, 103.8442, 'Main entrance, first counter', 'Tue-Sun: 11:00-22:00', true, NOW()),
(9, 'Sunrise Coffee House Bras Basah', 'Premium coffee and breakfast items', '231 Bain Street, #01-23', '180231', 1.3542, 103.8500, 'Counter service available', 'Mon-Sun: 07:00-19:00', true, NOW()),
(10, 'Fresh Bites Deli Clarke Quay', 'Fresh sandwiches and salads daily', '3 River Valley Road, #01-05', '179024', 1.3430, 103.8420, 'Pickup at deli counter', 'Mon-Sat: 09:00-21:00', true, NOW()),
(11, 'Sweet Heaven Desserts Bugis', 'Cakes, cookies and sweet treats', '200 Victoria Street, Bugis Junction #B1-10', '188021', 1.3561, 103.8546, 'Basement level, dessert corner', 'Mon-Sun: 11:00-22:00', true, NOW()),
(12, 'Laksa Delight Chinatown', 'Traditional laksa and noodle dishes', '335 Smith Street, Chinatown Complex #02-112', '050335', 1.3420, 103.8388, 'Level 2, stall 112', 'Mon-Sun: 10:00-20:00', true, NOW()),
(13, 'Urban Bites Kitchen City Hall', 'Modern fusion cuisine and coffee', '3 Temasek Boulevard, Suntec City #01-645', '038983', 1.3540, 103.8588, 'Level 1, near fountain', 'Mon-Sun: 10:00-22:00', true, NOW()),
(14, 'Morning Glory Bakery Tanjong Pagar', 'Fresh bread and morning pastries', '7 Wallich Street, Guoco Tower #01-03', '078884', 1.3380, 103.8450, 'Ground floor lobby', 'Mon-Fri: 07:00-19:00', true, NOW()),
(15, 'Spice Garden Restaurant Little India', 'Indian cuisine with vegetarian options', '48 Serangoon Road, Little India Arcade #01-20', '217959', 1.3598, 103.8520, 'First floor, unit 20', 'Mon-Sun: 11:00-23:00', true, NOW());

-- ============================================
-- 7. STORE_TYPES (Depends on supplier_profiles - OneToOne)
-- ============================================
INSERT INTO store_types (supplier_id, type_name, description, is_active, created_at) VALUES
(1, 'Bakery', 'Fresh baked goods and pastries', true, NOW()),
(2, 'Cafe', 'Coffee and light meals', true, NOW()),
(3, 'Restaurant', 'Full-service dining', true, NOW()),
(4, 'Supermarket', 'Grocery and fresh produce', true, NOW()),
(5, 'Coffee Shop', 'Specialty coffee and snacks', true, NOW());

-- ============================================
-- 8. LISTINGS (OneToOne with stores)
-- ============================================
INSERT INTO listings (store_id, title, description, original_price, rescue_price, pickup_start, pickup_end, expiry_at, status, created_at) VALUES
(1, 'Assorted Bread Bundle', 'Mix of fresh breads from today', 15.00, 5.00, '2026-01-29 18:00:00', '2026-01-29 21:00:00', '2026-01-30 08:00:00', 'ACTIVE', NOW()),
(2, 'Pastry Surprise Box', 'Assorted pastries and cakes', 25.00, 8.00, '2026-01-29 19:00:00', '2026-01-29 22:00:00', '2026-01-30 08:00:00', 'ACTIVE', NOW()),
(3, 'Breakfast Combo', 'Kaya toast set with coffee', 12.00, 4.50, '2026-01-29 07:00:00', '2026-01-29 10:00:00', '2026-01-29 12:00:00', 'ACTIVE', NOW()),
(4, 'Local Delight Set', 'Traditional breakfast items', 14.00, 5.00, '2026-01-29 08:00:00', '2026-01-29 11:00:00', '2026-01-29 13:00:00', 'ACTIVE', NOW()),
(5, 'Dim Sum Platter', 'Assorted dim sum selection', 35.00, 12.00, '2026-01-29 14:00:00', '2026-01-29 16:00:00', '2026-01-29 20:00:00', 'ACTIVE', NOW()),
(6, 'Dumpling Feast', 'Mixed dumplings platter', 32.00, 11.00, '2026-01-29 20:00:00', '2026-01-29 21:30:00', '2026-01-30 10:00:00', 'ACTIVE', NOW()),
(7, 'Fresh Produce Box', 'Vegetables and fruits', 22.00, 8.00, '2026-01-29 20:00:00', '2026-01-29 22:00:00', '2026-01-30 12:00:00', 'ACTIVE', NOW()),
(8, 'Grocery Essentials', 'Mixed grocery items', 30.00, 10.00, '2026-01-29 21:00:00', '2026-01-29 22:30:00', '2026-01-30 14:00:00', 'ACTIVE', NOW()),
(9, 'Coffee & Pastry Set', 'Latte with pastries', 15.00, 5.50, '2026-01-29 17:00:00', '2026-01-29 19:00:00', '2026-01-29 22:00:00', 'ACTIVE', NOW()),
(10, 'Airport Snack Box', 'Travel-friendly snacks', 18.00, 6.50, '2026-01-29 18:00:00', '2026-01-29 21:00:00', '2026-01-30 08:00:00', 'ACTIVE', NOW());

-- ============================================
-- 9. INVENTORY (OneToOne with listings)
-- ============================================
INSERT INTO inventory (listing_id, qty_available, qty_reserved, last_updated) VALUES
(1, 10, 2, NOW()),
(2, 8, 1, NOW()),
(3, 20, 5, NOW()),
(4, 18, 4, NOW()),
(5, 8, 2, NOW()),
(6, 12, 3, NOW()),
(7, 20, 5, NOW()),
(8, 25, 8, NOW()),
(9, 30, 10, NOW()),
(10, 15, 3, NOW());

-- ============================================
-- 10. LISTING_PHOTOS (Many photos per listing)
-- ============================================
INSERT INTO listing_photos (listing_id, photo_url, sort_order, uploaded_at) VALUES
(1, 'https://images.unsplash.com/photo-1509440159596-0249088772ff', 1, NOW()),
(1, 'https://images.unsplash.com/photo-1555507036-ab1f4038808a', 2, NOW()),
(2, 'https://images.unsplash.com/photo-1486427944299-d1955d23e34d', 1, NOW()),
(2, 'https://images.unsplash.com/photo-1587241321921-91a834d82ffc', 2, NOW()),
(3, 'https://images.unsplash.com/photo-1525351484163-7529414344d8', 1, NOW()),
(3, 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085', 2, NOW()),
(4, 'https://images.unsplash.com/photo-1504754524776-8f4f37790ca0', 1, NOW()),
(4, 'https://images.unsplash.com/photo-1533777857889-4be7c70b33f7', 2, NOW()),
(5, 'https://images.unsplash.com/photo-1496116218417-1a781b1c416c', 1, NOW()),
(5, 'https://images.unsplash.com/photo-1563245372-f21724e3856d', 2, NOW()),
(6, 'https://images.unsplash.com/photo-1534422298391-e4f8c172dddb', 1, NOW()),
(6, 'https://images.unsplash.com/photo-1496116218417-1a781b1c416c', 2, NOW()),
(7, 'https://images.unsplash.com/photo-1540420773420-3366772f4999', 1, NOW()),
(7, 'https://images.unsplash.com/photo-1488459716781-31db52582fe9', 2, NOW()),
(8, 'https://images.unsplash.com/photo-1543168256-418811576931', 1, NOW()),
(8, 'https://images.unsplash.com/photo-1534723452862-4c874018d66d', 2, NOW()),
(9, 'https://images.unsplash.com/photo-1511920170033-f8396924c348', 1, NOW()),
(9, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93', 2, NOW()),
(10, 'https://images.unsplash.com/photo-1551024601-bec78aea704b', 1, NOW()),
(10, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93', 2, NOW());

-- ============================================
-- 11. LISTING_DIETARY_TAGS (Many-to-many links)
-- ============================================
INSERT INTO listing_dietary_tags (listing_id, tag_id) VALUES
(1, 1), -- Vegetarian breads
(2, 1), (2, 5), -- Pastry box vegetarian/dairy-free friendly
(3, 4), -- Breakfast halal friendly
(4, 1), (4, 2), -- Vegan-friendly local set
(5, 1), (5, 3), -- Dim sum with gluten-free options
(6, 4), -- Halal dumplings
(7, 8), -- Organic produce
(8, 6), -- Nut-free groceries
(9, 5), (9, 6), -- Dairy- and nut-free coffee set
(10, 7); -- Low-sugar snacks

-- ============================================
-- 12. ORDERS (Demo consumer/store orders)
-- ============================================
-- 增加更多历史订单,让推荐系统有丰富的数据
INSERT INTO orders (store_id, consumer_id, status, pickup_slot_start, pickup_slot_end, total_amount, currency, cancel_reason, created_at, updated_at) VALUES
-- 原有的3个订单
(1, 1, 'COMPLETED', '2026-01-29 18:00:00', '2026-01-29 19:30:00', 5.00, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 2, 'COMPLETED', '2026-01-29 07:30:00', '2026-01-29 09:00:00', 4.50, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
(5, 3, 'COMPLETED', '2026-01-29 14:30:00', '2026-01-29 16:00:00', 12.00, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),

-- Consumer 1 (Alice) - 喜欢Bakery (额外2个订单)
(1, 1, 'COMPLETED', '2026-01-22 18:00:00', '2026-01-22 19:30:00', 8.00, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY)),
(2, 1, 'COMPLETED', '2026-01-15 19:00:00', '2026-01-15 20:30:00', 8.00, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY)),

-- Consumer 2 (Bob) - 喜欢Cafe (额外2个订单)
(3, 2, 'COMPLETED', '2026-01-20 07:30:00', '2026-01-20 09:00:00', 4.50, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY)),
(4, 2, 'COMPLETED', '2026-01-13 08:00:00', '2026-01-13 10:00:00', 5.00, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 21 DAY), DATE_SUB(NOW(), INTERVAL 21 DAY)),

-- Consumer 3 (Charlie) - 喜欢Restaurant (额外2个订单)
(5, 3, 'COMPLETED', '2026-01-23 14:30:00', '2026-01-23 16:00:00', 12.00, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY)),
(6, 3, 'COMPLETED', '2026-01-16 20:00:00', '2026-01-16 21:30:00', 11.00, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY)),

-- Consumer 4 (Diana) - 喜欢Supermarket/Organic (2个订单)
(7, 4, 'COMPLETED', '2026-01-24 20:00:00', '2026-01-24 21:30:00', 8.00, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
(7, 4, 'COMPLETED', '2026-01-17 20:00:00', '2026-01-17 21:30:00', 8.00, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 17 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY)),

-- Consumer 5 (Emily) - 喜欢Coffee Shop (2个订单)
(9, 5, 'COMPLETED', '2026-01-25 17:00:00', '2026-01-25 18:30:00', 5.50, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY)),
(10, 5, 'COMPLETED', '2026-01-18 18:00:00', '2026-01-18 20:00:00', 6.50, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY)),

-- Consumer 6 (Frank) - 混合偏好 (2个订单)
(1, 6, 'COMPLETED', '2026-01-21 18:00:00', '2026-01-21 19:30:00', 5.00, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY)),
(8, 6, 'COMPLETED', '2026-01-14 21:00:00', '2026-01-14 22:00:00', 10.00, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY)),

-- Consumer 7 (Grace) - 喜欢Halal (2个订单)
(3, 7, 'COMPLETED', '2026-01-19 07:30:00', '2026-01-19 09:00:00', 4.50, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),
(6, 7, 'COMPLETED', '2026-01-12 20:00:00', '2026-01-12 21:30:00', 11.00, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY)),

-- Consumer 8 (Henry) - 喜欢Organic/Supermarket (2个订单)
(7, 8, 'COMPLETED', '2026-01-26 20:00:00', '2026-01-26 21:30:00', 8.00, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY)),
(8, 8, 'COMPLETED', '2026-01-11 21:00:00', '2026-01-11 22:00:00', 10.00, 'SGD', NULL, DATE_SUB(NOW(), INTERVAL 23 DAY), DATE_SUB(NOW(), INTERVAL 23 DAY));

-- ============================================
-- 13. ORDER_ITEMS (Line items per order)
-- ============================================
INSERT INTO order_items (order_id, listing_id, quantity, unit_price, line_total) VALUES
-- 原有3个订单的items
(1, 1, 1, 5.00, 5.00),
(2, 3, 1, 4.50, 4.50),
(3, 5, 1, 12.00, 12.00),

-- 新增订单的items
(4, 2, 1, 8.00, 8.00),   -- Alice - BreadTalk Tampines
(5, 2, 1, 8.00, 8.00),   -- Alice - BreadTalk Tampines

(6, 3, 1, 4.50, 4.50),   -- Bob - Toast Box Marina Bay
(7, 4, 1, 5.00, 5.00),   -- Bob - Toast Box Jurong Point

(8, 5, 1, 12.00, 12.00), -- Charlie - Paradise Dynasty ION
(9, 6, 1, 11.00, 11.00), -- Charlie - Paradise Dynasty Vivocity

(10, 7, 1, 8.00, 8.00),  -- Diana - FairPrice Finest
(11, 7, 1, 8.00, 8.00),  -- Diana - FairPrice Finest

(12, 9, 1, 5.50, 5.50),  -- Emily - Starbucks Raffles Place
(13, 10, 1, 6.50, 6.50), -- Emily - Starbucks Changi Airport

(14, 1, 1, 5.00, 5.00),  -- Frank - BreadTalk Orchard
(15, 8, 1, 10.00, 10.00),-- Frank - FairPrice Xtra

(16, 3, 1, 4.50, 4.50),  -- Grace - Toast Box Marina Bay
(17, 6, 1, 11.00, 11.00),-- Grace - Paradise Dynasty Vivocity

(18, 7, 1, 8.00, 8.00),  -- Henry - FairPrice Finest
(19, 8, 1, 10.00, 10.00);-- Henry - FairPrice Xtra

-- ============================================
-- 14. PAYMENTS (Payment attempts per order)
-- ============================================
INSERT INTO payments (order_id, provider, provider_ref, amount, status, created_at, captured_at, refunded_at) VALUES
(1, 'STRIPE', 'STRP-1001', 5.00, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), NULL),
(2, 'DBS_PAYLAH', 'DBS-2001', 4.50, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), NULL),
(3, 'STRIPE', 'STRP-1002', 12.00, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), NULL),
(4, 'STRIPE', 'STRP-1003', 8.00, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY), NULL),
(5, 'STRIPE', 'STRP-1004', 8.00, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY), NULL),
(6, 'DBS_PAYLAH', 'DBS-2002', 4.50, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY), NULL),
(7, 'DBS_PAYLAH', 'DBS-2003', 5.00, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 21 DAY), DATE_SUB(NOW(), INTERVAL 21 DAY), NULL),
(8, 'STRIPE', 'STRP-1005', 12.00, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY), NULL),
(9, 'STRIPE', 'STRP-1006', 11.00, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY), NULL),
(10, 'STRIPE', 'STRP-1007', 8.00, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), NULL),
(11, 'STRIPE', 'STRP-1008', 8.00, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 17 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY), NULL),
(12, 'STRIPE', 'STRP-1009', 5.50, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), NULL),
(13, 'STRIPE', 'STRP-1010', 6.50, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY), NULL),
(14, 'STRIPE', 'STRP-1011', 5.00, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY), NULL),
(15, 'DBS_PAYLAH', 'DBS-2004', 10.00, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY), NULL),
(16, 'DBS_PAYLAH', 'DBS-2005', 4.50, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), NULL),
(17, 'STRIPE', 'STRP-1012', 11.00, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY), NULL),
(18, 'STRIPE', 'STRP-1013', 8.00, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), NULL),
(19, 'DBS_PAYLAH', 'DBS-2006', 10.00, 'SUCCEEDED', DATE_SUB(NOW(), INTERVAL 23 DAY), DATE_SUB(NOW(), INTERVAL 23 DAY), NULL);

-- ============================================
-- 15. PICKUP_TOKENS (QR tokens for pickup)
-- ============================================
INSERT INTO pickup_tokens (order_id, qr_token_hash, issued_at, expires_at, used_at) VALUES
(1, 'hash-order-1', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), NOW()),
(2, 'hash-order-2', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), NOW()),
(3, 'hash-order-3', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), NULL),
(4, 'hash-order-4', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), NULL),
(5, 'hash-order-5', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), NULL),
(6, 'hash-order-6', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), NULL),
(7, 'hash-order-7', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), NULL),
(8, 'hash-order-8', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), NULL),
(9, 'hash-order-9', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), NULL);

-- ============================================
-- 16. PICKUP_FEEDBACK (Feedback per order)
-- ============================================
INSERT INTO pickup_feedback (order_id, feedback_review, on_time, expires_at, used_at) VALUES
(1, 'Great bread, smooth pickup', true, DATE_ADD(NOW(), INTERVAL 7 DAY), NOW()),
(2, 'Coffee was hot and on time', true, DATE_ADD(NOW(), INTERVAL 7 DAY), NOW()),
(3, 'Dim sum was tasty, slight wait', false, DATE_ADD(NOW(), INTERVAL 7 DAY), NULL);

-- ============================================
-- 17. COMMISSION_LEDGER (Platform commission per order)
-- ============================================
INSERT INTO commission_ledger (order_id, supplier_id, commission_amount, status, created_at) VALUES
(1, 1, 0.75, 'PAID', NOW()),
(2, 2, 0.68, 'PAID', NOW()),
(3, 3, 1.80, 'PAID', NOW());

-- ============================================
-- 18. WALLET_TRANSACTIONS (Balance movements)
-- ============================================
INSERT INTO wallet_transactions (wallet_id, txn_type, amount, reference_id, created_at) VALUES
(1, 'PAYMENT', -5.00, 'ORDER-1', NOW()),
(2, 'PAYMENT', -4.50, 'ORDER-2', NOW()),
(3, 'PAYMENT', -12.00, 'ORDER-3', NOW()),
(1, 'TOPUP', 20.00, 'TOPUP-001', NOW());

-- ============================================
-- 19. PAYOUTS (Settlement to suppliers)
-- ============================================
INSERT INTO payouts (supplier_id, period_start, period_end, amount_gross, commission_amount, amount_net, status, created_at) VALUES
(1, '2026-01-01 00:00:00', '2026-01-31 23:59:59', 5.00, 0.75, 4.25, 'PAID', NOW()),
(2, '2026-01-01 00:00:00', '2026-01-31 23:59:59', 4.50, 0.68, 3.82, 'PAID', NOW()),
(3, '2026-01-01 00:00:00', '2026-01-31 23:59:59', 12.00, 1.80, 10.20, 'PAID', NOW());

-- ============================================
-- RECOMMENDER SYSTEM TABLES
-- ============================================

-- ============================================
-- 20. USER_INTERACTIONS (User behavior tracking)
-- ============================================
-- Distribution: VIEW 50%, CLICK 30%, SEARCH 15%, ADD_TO_CART 5%
-- Total: ~180 records across 8 consumers and 10 listings

INSERT INTO user_interactions (consumer_id, listing_id, interaction_type, session_id, device_type, created_at) VALUES
-- Consumer 1 (Alice Tan) - Vegetarian preference
(1, 1, 'VIEW', 'sess-alice-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 1, 'CLICK', 'sess-alice-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 1, 'ADD_TO_CART', 'sess-alice-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 2, 'VIEW', 'sess-alice-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 2, 'CLICK', 'sess-alice-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 3, 'VIEW', 'sess-alice-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 4, 'VIEW', 'sess-alice-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 4, 'CLICK', 'sess-alice-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 5, 'VIEW', 'sess-alice-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 7, 'VIEW', 'sess-alice-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 7, 'CLICK', 'sess-alice-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 9, 'VIEW', 'sess-alice-004', 'MOBILE', DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- Consumer 2 (Bob Lim) - Halal preference
(2, 3, 'VIEW', 'sess-bob-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 3, 'CLICK', 'sess-bob-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 3, 'ADD_TO_CART', 'sess-bob-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 4, 'VIEW', 'sess-bob-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 4, 'CLICK', 'sess-bob-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 6, 'VIEW', 'sess-bob-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 6, 'CLICK', 'sess-bob-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 1, 'VIEW', 'sess-bob-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 2, 'VIEW', 'sess-bob-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 8, 'VIEW', 'sess-bob-004', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 8, 'CLICK', 'sess-bob-004', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 9, 'VIEW', 'sess-bob-004', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- Consumer 3 (Charlie Wong) - No dietary restrictions
(3, 5, 'VIEW', 'sess-charlie-001', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(3, 5, 'CLICK', 'sess-charlie-001', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(3, 5, 'ADD_TO_CART', 'sess-charlie-001', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(3, 6, 'VIEW', 'sess-charlie-001', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(3, 1, 'VIEW', 'sess-charlie-002', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3, 2, 'VIEW', 'sess-charlie-002', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3, 2, 'CLICK', 'sess-charlie-002', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3, 7, 'VIEW', 'sess-charlie-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 7, 'CLICK', 'sess-charlie-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 8, 'VIEW', 'sess-charlie-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 10, 'VIEW', 'sess-charlie-004', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 10, 'CLICK', 'sess-charlie-004', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 4 DAY)),

-- Consumer 4 (Diana Ng) - Vegan, gluten-free
(4, 4, 'VIEW', 'sess-diana-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(4, 4, 'CLICK', 'sess-diana-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(4, 5, 'VIEW', 'sess-diana-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(4, 5, 'CLICK', 'sess-diana-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(4, 7, 'VIEW', 'sess-diana-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 7, 'CLICK', 'sess-diana-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 7, 'ADD_TO_CART', 'sess-diana-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 1, 'VIEW', 'sess-diana-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 2, 'VIEW', 'sess-diana-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 8, 'VIEW', 'sess-diana-004', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 9, 'VIEW', 'sess-diana-004', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 9, 'CLICK', 'sess-diana-004', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- Consumer 5 (Emily Chen) - Dairy-free
(5, 9, 'VIEW', 'sess-emily-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(5, 9, 'CLICK', 'sess-emily-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(5, 9, 'ADD_TO_CART', 'sess-emily-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(5, 10, 'VIEW', 'sess-emily-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(5, 10, 'CLICK', 'sess-emily-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(5, 1, 'VIEW', 'sess-emily-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 2, 'VIEW', 'sess-emily-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 2, 'CLICK', 'sess-emily-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 3, 'VIEW', 'sess-emily-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 4, 'VIEW', 'sess-emily-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 7, 'VIEW', 'sess-emily-004', 'MOBILE', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 7, 'CLICK', 'sess-emily-004', 'MOBILE', DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- Consumer 6 (Frank Koh) - No dietary restrictions
(6, 1, 'VIEW', 'sess-frank-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(6, 1, 'CLICK', 'sess-frank-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(6, 2, 'VIEW', 'sess-frank-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(6, 3, 'VIEW', 'sess-frank-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(6, 3, 'CLICK', 'sess-frank-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(6, 4, 'VIEW', 'sess-frank-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(6, 5, 'VIEW', 'sess-frank-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(6, 5, 'CLICK', 'sess-frank-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(6, 6, 'VIEW', 'sess-frank-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(6, 8, 'VIEW', 'sess-frank-004', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 9, 'VIEW', 'sess-frank-004', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 10, 'VIEW', 'sess-frank-005', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- Consumer 7 (Grace Lee) - Halal, nut-free
(7, 3, 'VIEW', 'sess-grace-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(7, 3, 'CLICK', 'sess-grace-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(7, 4, 'VIEW', 'sess-grace-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(7, 4, 'CLICK', 'sess-grace-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(7, 6, 'VIEW', 'sess-grace-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(7, 6, 'CLICK', 'sess-grace-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(7, 8, 'VIEW', 'sess-grace-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 8, 'CLICK', 'sess-grace-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 8, 'ADD_TO_CART', 'sess-grace-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 1, 'VIEW', 'sess-grace-004', 'MOBILE', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(7, 2, 'VIEW', 'sess-grace-004', 'MOBILE', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(7, 9, 'VIEW', 'sess-grace-005', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- Consumer 8 (Henry Teo) - Organic preference
(8, 7, 'VIEW', 'sess-henry-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(8, 7, 'CLICK', 'sess-henry-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(8, 7, 'ADD_TO_CART', 'sess-henry-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(8, 8, 'VIEW', 'sess-henry-001', 'MOBILE', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(8, 1, 'VIEW', 'sess-henry-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(8, 2, 'VIEW', 'sess-henry-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(8, 2, 'CLICK', 'sess-henry-002', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(8, 3, 'VIEW', 'sess-henry-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(8, 4, 'VIEW', 'sess-henry-003', 'MOBILE', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(8, 5, 'VIEW', 'sess-henry-004', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(8, 6, 'VIEW', 'sess-henry-004', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(8, 10, 'VIEW', 'sess-henry-005', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- Additional VIEW interactions for more realistic distribution
(1, 6, 'VIEW', 'sess-alice-005', 'MOBILE', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 8, 'VIEW', 'sess-alice-005', 'MOBILE', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 10, 'VIEW', 'sess-alice-006', 'DESKTOP', NOW()),
(2, 5, 'VIEW', 'sess-bob-005', 'MOBILE', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 7, 'VIEW', 'sess-bob-005', 'MOBILE', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 10, 'VIEW', 'sess-bob-006', 'DESKTOP', NOW()),
(3, 3, 'VIEW', 'sess-charlie-005', 'MOBILE', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 4, 'VIEW', 'sess-charlie-005', 'MOBILE', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 9, 'VIEW', 'sess-charlie-006', 'DESKTOP', NOW()),
(4, 3, 'VIEW', 'sess-diana-005', 'MOBILE', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 6, 'VIEW', 'sess-diana-005', 'MOBILE', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 10, 'VIEW', 'sess-diana-006', 'DESKTOP', NOW()),
(5, 5, 'VIEW', 'sess-emily-005', 'MOBILE', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 6, 'VIEW', 'sess-emily-005', 'MOBILE', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 8, 'VIEW', 'sess-emily-006', 'DESKTOP', NOW()),
(6, 7, 'VIEW', 'sess-frank-006', 'MOBILE', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(7, 5, 'VIEW', 'sess-grace-006', 'MOBILE', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(7, 7, 'VIEW', 'sess-grace-007', 'DESKTOP', NOW()),
(7, 10, 'VIEW', 'sess-grace-008', 'MOBILE', NOW()),
(8, 9, 'VIEW', 'sess-henry-006', 'MOBILE', DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- Additional SEARCH interactions (~15% of total)
(1, 1, 'SEARCH', 'sess-alice-007', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 7, 'SEARCH', 'sess-alice-007', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 3, 'SEARCH', 'sess-bob-007', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 4, 'SEARCH', 'sess-bob-007', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 5, 'SEARCH', 'sess-charlie-007', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 7, 'SEARCH', 'sess-diana-007', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 9, 'SEARCH', 'sess-emily-007', 'MOBILE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 1, 'SEARCH', 'sess-frank-007', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(7, 3, 'SEARCH', 'sess-grace-009', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 6, 'SEARCH', 'sess-grace-009', 'MOBILE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(8, 7, 'SEARCH', 'sess-henry-007', 'DESKTOP', DATE_SUB(NOW(), INTERVAL 4 DAY));

-- ============================================
-- 21. STORE_RATINGS (Store reviews and ratings)
-- ============================================
-- Based on 3 completed orders plus additional non-order ratings
-- Rating distribution: 5★ 40%, 4-4.5★ 35%, 3-3.5★ 20%, 1-2★ 5%

INSERT INTO store_ratings (store_id, consumer_id, order_id, rating, comment, created_at) VALUES
-- Ratings from completed orders
(1, 1, 1, 4.50, 'Great bread, smooth pickup process. Fresh and tasty!', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 2, 2, 5.00, 'Coffee was hot and on time. Perfect breakfast combo!', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(5, 3, 3, 4.00, 'Dim sum was tasty, but had to wait a bit longer than expected.', DATE_SUB(NOW(), INTERVAL 7 DAY)),

-- Additional ratings without orders (consumers who visited before)
(1, 2, NULL, 4.50, 'Love their bread selection. Always fresh!', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(1, 6, NULL, 5.00, 'Best bakery deals in Orchard. Highly recommend!', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 1, NULL, 3.50, 'Pastries were okay, a bit dry. Service was good though.', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(3, 5, NULL, 5.00, 'Amazing coffee and kaya toast. Will come back!', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 7, NULL, 4.50, 'Great halal breakfast options. Convenient location.', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(4, 2, NULL, 4.00, 'Good local food at Toast Box. Reliable quality.', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(5, 4, NULL, 3.00, 'Dim sum quality varies. Sometimes great, sometimes average.', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(6, 3, NULL, 4.50, 'Love the dumplings! Great value for money.', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 4, NULL, 5.00, 'Organic produce is always fresh. Best grocery store!', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(7, 8, NULL, 5.00, 'Perfect for organic lovers. High quality items.', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(8, 5, NULL, 4.00, 'Good grocery selection. Convenient late hours.', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(8, 7, NULL, 3.50, 'Decent grocery store. Sometimes out of stock items.', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(9, 5, NULL, 5.00, 'Perfect coffee and pastry combo at Raffles Place!', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9, 6, NULL, 4.50, 'Great Starbucks outlet. Quick service.', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(10, 3, NULL, 4.00, 'Convenient airport location. Good snacks for travel.', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(10, 8, NULL, 3.00, 'A bit pricey for airport outlet but decent quality.', DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- One low rating for realism (5%)
(2, 8, NULL, 2.00, 'Pastries were stale and overpriced. Not worth it.', DATE_SUB(NOW(), INTERVAL 10 DAY));

-- ============================================
-- 22. LISTING_STATS (Listing performance metrics)
-- ============================================
-- Calculated from user_interactions and orders
-- All 10 listings have stats

INSERT INTO listing_stats (listing_id, view_count, click_count, add_to_cart_count, order_count, ctr, cvr, updated_at) VALUES
-- Listing 1: Assorted Bread Bundle (Store 1)
(1, 15, 5, 1, 1, 0.3333, 0.0667, NOW()),
-- Listing 2: Pastry Surprise Box (Store 2)
(2, 13, 6, 0, 0, 0.4615, 0.0000, NOW()),
-- Listing 3: Breakfast Combo (Store 3)
(3, 10, 6, 1, 1, 0.6000, 0.1000, NOW()),
-- Listing 4: Local Delight Set (Store 4)
(4, 12, 6, 0, 0, 0.5000, 0.0000, NOW()),
-- Listing 5: Dim Sum Platter (Store 5)
(5, 11, 4, 1, 1, 0.3636, 0.0909, NOW()),
-- Listing 6: Dumpling Feast (Store 6)
(6, 8, 3, 0, 0, 0.3750, 0.0000, NOW()),
-- Listing 7: Fresh Produce Box (Store 7)
(7, 12, 6, 2, 0, 0.5000, 0.0000, NOW()),
-- Listing 8: Grocery Essentials (Store 8)
(8, 9, 4, 1, 0, 0.4444, 0.0000, NOW()),
-- Listing 9: Coffee & Pastry Set (Store 9)
(9, 8, 2, 1, 0, 0.2500, 0.0000, NOW()),
-- Listing 10: Airport Snack Box (Store 10)
(10, 10, 2, 0, 0, 0.2000, 0.0000, NOW());

-- ============================================
-- 23. CONSUMER_STATS (User behavior statistics)
-- ============================================
-- 更新以反映新增的订单历史
INSERT INTO consumer_stats (consumer_id, total_orders, completed_orders, cancelled_orders, total_spend, avg_order_value, total_views, total_clicks, favorite_store_type, updated_at) VALUES
-- Consumer 1: Alice Tan - 3个订单,偏好Bakery
(1, 3, 3, 0, 21.00, 7.00, 12, 5, 'Bakery', NOW()),
-- Consumer 2: Bob Lim - 3个订单,偏好Cafe
(2, 3, 3, 0, 14.00, 4.67, 12, 6, 'Cafe', NOW()),
-- Consumer 3: Charlie Wong - 3个订单,偏好Restaurant
(3, 3, 3, 0, 35.00, 11.67, 12, 5, 'Restaurant', NOW()),
-- Consumer 4: Diana Ng - 2个订单,偏好Supermarket
(4, 2, 2, 0, 16.00, 8.00, 12, 5, 'Supermarket', NOW()),
-- Consumer 5: Emily Chen - 2个订单,偏好Coffee Shop
(5, 2, 2, 0, 12.00, 6.00, 12, 5, 'Coffee Shop', NOW()),
-- Consumer 6: Frank Koh - 2个订单,混合偏好
(6, 2, 2, 0, 15.00, 7.50, 12, 4, 'Bakery', NOW()),
-- Consumer 7: Grace Lee - 2个订单,偏好Cafe(Halal)
(7, 2, 2, 0, 15.50, 7.75, 12, 5, 'Cafe', NOW()),
-- Consumer 8: Henry Teo - 2个订单,偏好Supermarket(Organic)
(8, 2, 2, 0, 18.00, 9.00, 12, 3, 'Supermarket', NOW());

-- ============================================
-- 24. STORE_STATS (Store performance metrics)
-- ============================================
-- Calculated from store_ratings, orders, and user_interactions

INSERT INTO store_stats (store_id, avg_rating, total_ratings, total_orders, completed_orders, completion_rate, on_time_deliveries, on_time_rate, active_listings, total_views, updated_at) VALUES
-- Store 1: BreadTalk Orchard
(1, 4.50, 3, 1, 1, 1.0000, 1, 1.0000, 1, 15, NOW()),
-- Store 2: BreadTalk Tampines
(2, 3.00, 2, 0, 0, 0.0000, 0, 0.0000, 1, 13, NOW()),
-- Store 3: Toast Box Marina Bay
(3, 4.75, 3, 1, 1, 1.0000, 1, 1.0000, 1, 10, NOW()),
-- Store 4: Toast Box Jurong Point
(4, 4.00, 1, 0, 0, 0.0000, 0, 0.0000, 1, 12, NOW()),
-- Store 5: Paradise Dynasty ION
(5, 3.50, 2, 1, 1, 1.0000, 0, 0.0000, 1, 11, NOW()),
-- Store 6: Paradise Dynasty Vivocity
(6, 4.50, 1, 0, 0, 0.0000, 0, 0.0000, 1, 8, NOW()),
-- Store 7: FairPrice Finest Bukit Timah
(7, 5.00, 2, 0, 0, 0.0000, 0, 0.0000, 1, 12, NOW()),
-- Store 8: FairPrice Xtra Ang Mo Kio
(8, 3.75, 2, 0, 0, 0.0000, 0, 0.0000, 1, 9, NOW()),
-- Store 9: Starbucks Raffles Place
(9, 4.75, 2, 0, 0, 0.0000, 0, 0.0000, 1, 8, NOW()),
-- Store 10: Starbucks Changi Airport
(10, 3.50, 2, 0, 0, 0.0000, 0, 0.0000, 1, 10, NOW());

-- ============================================
-- 25. USER_STORE_INTERACTIONS (Consumer-Store relationships)
-- ============================================
-- Tracks user engagement per store

INSERT INTO user_store_interactions (consumer_id, store_id, view_count, click_count, order_count, total_spend, last_order_at, updated_at) VALUES
-- Consumer 1 interactions
(1, 1, 3, 2, 1, 5.00, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
(1, 2, 2, 1, 0, 0.00, NULL, NOW()),
(1, 3, 1, 0, 0, 0.00, NULL, NOW()),
(1, 4, 2, 1, 0, 0.00, NULL, NOW()),
(1, 5, 1, 0, 0, 0.00, NULL, NOW()),
(1, 7, 2, 1, 0, 0.00, NULL, NOW()),

-- Consumer 2 interactions
(2, 1, 1, 0, 0, 0.00, NULL, NOW()),
(2, 2, 1, 0, 0, 0.00, NULL, NOW()),
(2, 3, 3, 2, 1, 4.50, DATE_SUB(NOW(), INTERVAL 6 DAY), NOW()),
(2, 4, 2, 1, 0, 0.00, NULL, NOW()),
(2, 6, 2, 1, 0, 0.00, NULL, NOW()),
(2, 8, 2, 1, 0, 0.00, NULL, NOW()),

-- Consumer 3 interactions
(3, 1, 1, 0, 0, 0.00, NULL, NOW()),
(3, 2, 2, 1, 0, 0.00, NULL, NOW()),
(3, 3, 1, 0, 0, 0.00, NULL, NOW()),
(3, 5, 3, 2, 1, 12.00, DATE_SUB(NOW(), INTERVAL 7 DAY), NOW()),
(3, 6, 1, 0, 0, 0.00, NULL, NOW()),
(3, 7, 2, 1, 0, 0.00, NULL, NOW()),
(3, 10, 2, 1, 0, 0.00, NULL, NOW()),

-- Consumer 4 interactions
(4, 1, 1, 0, 0, 0.00, NULL, NOW()),
(4, 2, 1, 0, 0, 0.00, NULL, NOW()),
(4, 4, 2, 1, 0, 0.00, NULL, NOW()),
(4, 5, 2, 1, 0, 0.00, NULL, NOW()),
(4, 7, 3, 2, 0, 0.00, NULL, NOW()),
(4, 8, 1, 0, 0, 0.00, NULL, NOW()),

-- Consumer 5 interactions
(5, 1, 1, 0, 0, 0.00, NULL, NOW()),
(5, 2, 2, 1, 0, 0.00, NULL, NOW()),
(5, 3, 1, 0, 0, 0.00, NULL, NOW()),
(5, 4, 1, 0, 0, 0.00, NULL, NOW()),
(5, 7, 2, 1, 0, 0.00, NULL, NOW()),
(5, 9, 3, 2, 0, 0.00, NULL, NOW()),
(5, 10, 2, 1, 0, 0.00, NULL, NOW()),

-- Consumer 6 interactions
(6, 1, 2, 1, 0, 0.00, NULL, NOW()),
(6, 2, 1, 0, 0, 0.00, NULL, NOW()),
(6, 3, 2, 1, 0, 0.00, NULL, NOW()),
(6, 4, 1, 0, 0, 0.00, NULL, NOW()),
(6, 5, 2, 1, 0, 0.00, NULL, NOW()),
(6, 7, 1, 0, 0, 0.00, NULL, NOW()),

-- Consumer 7 interactions
(7, 1, 1, 0, 0, 0.00, NULL, NOW()),
(7, 2, 1, 0, 0, 0.00, NULL, NOW()),
(7, 3, 2, 1, 0, 0.00, NULL, NOW()),
(7, 4, 2, 1, 0, 0.00, NULL, NOW()),
(7, 5, 1, 0, 0, 0.00, NULL, NOW()),
(7, 6, 2, 1, 0, 0.00, NULL, NOW()),
(7, 8, 3, 2, 0, 0.00, NULL, NOW()),

-- Consumer 8 interactions
(8, 1, 1, 0, 0, 0.00, NULL, NOW()),
(8, 2, 2, 1, 0, 0.00, NULL, NOW()),
(8, 3, 1, 0, 0, 0.00, NULL, NOW()),
(8, 4, 1, 0, 0, 0.00, NULL, NOW()),
(8, 5, 1, 0, 0, 0.00, NULL, NOW()),
(8, 7, 3, 2, 0, 0.00, NULL, NOW()),
(8, 10, 1, 0, 0, 0.00, NULL, NOW());

-- ============================================
-- 26. SEARCH_LOGS (Search query tracking)
-- ============================================
-- Tracks user search behavior and click patterns

INSERT INTO search_logs (consumer_id, query_text, results_count, clicked_listing_id, click_position, session_id, created_at) VALUES
-- Consumer 1 searches
(1, 'bread', 2, 1, 1, 'sess-alice-007', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 'organic vegetables', 3, 7, 2, 'sess-alice-007', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 'vegetarian food', 5, NULL, NULL, 'sess-alice-008', DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- Consumer 2 searches
(2, 'halal breakfast', 2, 3, 1, 'sess-bob-007', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 'halal dumplings', 1, 4, 1, 'sess-bob-007', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 'kaya toast', 2, NULL, NULL, 'sess-bob-008', DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- Consumer 3 searches
(3, 'dim sum', 2, 5, 1, 'sess-charlie-007', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 'chinese food', 3, NULL, NULL, 'sess-charlie-008', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 'dumplings', 1, 6, 1, 'sess-charlie-009', DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- Consumer 4 searches
(4, 'vegan', 4, 7, 3, 'sess-diana-007', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 'gluten free', 3, NULL, NULL, 'sess-diana-008', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 'organic produce', 2, 7, 1, 'sess-diana-009', DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- Consumer 5 searches
(5, 'coffee pastry', 3, 9, 1, 'sess-emily-007', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 'dairy free snacks', 2, NULL, NULL, 'sess-emily-008', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 'starbucks', 2, 9, 1, 'sess-emily-009', DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- Consumer 6 searches
(6, 'bakery', 3, 1, 1, 'sess-frank-007', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(6, 'fresh bread', 2, NULL, NULL, 'sess-frank-008', DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- Consumer 7 searches
(7, 'halal', 4, 3, 1, 'sess-grace-009', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 'nut free groceries', 2, 6, 2, 'sess-grace-009', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 'supermarket deals', 3, NULL, NULL, 'sess-grace-010', DATE_SUB(NOW(), INTERVAL 4 DAY)),

-- Consumer 8 searches
(8, 'organic', 3, 7, 1, 'sess-henry-007', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(8, 'fresh produce box', 1, 7, 1, 'sess-henry-008', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(8, 'grocery essentials', 2, NULL, NULL, 'sess-henry-009', DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- Searches with no results (for analytics)
(1, 'sushi rolls', 0, NULL, NULL, 'sess-alice-009', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'pizza delivery', 0, NULL, NULL, 'sess-charlie-010', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 'ice cream', 0, NULL, NULL, 'sess-emily-010', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ============================================
-- END OF DATA INITIALIZATION
-- ============================================

