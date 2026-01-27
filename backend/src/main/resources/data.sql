-- ============================================
-- Food Rescue Hub - Fake Data Initialization
-- ============================================
-- This file will be automatically executed by Spring Boot on startup
-- note that it will be populated inside ec2 server upon running
-- Note: Fields with @CreationTimestamp and @UpdateTimestamp are auto-generated

-- Reset tables for deterministic seeding
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE pickup_feedback;
TRUNCATE TABLE pickup_tokens;
TRUNCATE TABLE payments;
TRUNCATE TABLE commission_ledger;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE listing_dietary_tags;
TRUNCATE TABLE listing_photos;
TRUNCATE TABLE inventory;
TRUNCATE TABLE listings;
TRUNCATE TABLE store_types;
TRUNCATE TABLE stores;
TRUNCATE TABLE payouts;
TRUNCATE TABLE wallet_transactions;
TRUNCATE TABLE wallets;
TRUNCATE TABLE admin_profiles;
TRUNCATE TABLE supplier_profiles;
TRUNCATE TABLE consumer_profiles;
TRUNCATE TABLE dietary_tags;
SET FOREIGN_KEY_CHECKS=1;

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
-- 2. CONSUMER_PROFILES
-- ============================================
-- Plain text password: "password123"
INSERT INTO consumer_profiles (email, password, phone, display_name, status, role, default_lat, default_lng, preferences_json, created_at) VALUES
('alice.tan@email.com', 'password123', '+6591234567', 'Alice Tan', 'ACTIVE', 'CONSUMER', 1.3521, 103.8198, '{"dietary": ["vegetarian"], "radius": 5}', NOW()),
('bob.lim@email.com', 'password123', '+6591234568', 'Bob Lim', 'ACTIVE', 'CONSUMER', 1.2897, 103.8501, '{"dietary": ["halal"], "radius": 3}', NOW()),
('charlie.wong@email.com', 'password123', '+6591234569', 'Charlie Wong', 'ACTIVE', 'CONSUMER', 1.3048, 103.8318, '{"dietary": [], "radius": 10}', NOW()),
('diana.ng@email.com', 'password123', '+6591234570', 'Diana Ng', 'ACTIVE', 'CONSUMER', 1.3329, 103.7436, '{"dietary": ["vegan", "gluten-free"], "radius": 5}', NOW()),
('emily.chen@email.com', 'password123', '+6591234571', 'Emily Chen', 'ACTIVE', 'CONSUMER', 1.3644, 103.9915, '{"dietary": ["dairy-free"], "radius": 7}', NOW()),
('frank.koh@email.com', 'password123', '+6591234572', 'Frank Koh', 'ACTIVE', 'CONSUMER', 1.2800, 103.8400, '{"dietary": [], "radius": 5}', NOW()),
('grace.lee@email.com', 'password123', '+6591234573', 'Grace Lee', 'ACTIVE', 'CONSUMER', 1.3200, 103.8600, '{"dietary": ["halal", "nut-free"], "radius": 8}', NOW()),
('henry.teo@email.com', 'password123', '+6591234574', 'Henry Teo', 'ACTIVE', 'CONSUMER', 1.3100, 103.8500, '{"dietary": ["organic"], "radius": 6}', NOW());

-- ============================================
-- 3. SUPPLIER_PROFILES
-- ============================================
INSERT INTO supplier_profiles (email, password, phone, display_name, status, role, company_name, uen_number, created_at) VALUES
('bakery@breadtalk.sg', 'password123', '+6562345678', 'BreadTalk Manager', 'ACTIVE', 'SUPPLIER', 'BreadTalk Singapore Pte Ltd', '199906055C', NOW()),
('cafe@toastbox.sg', 'password123', '+6562345679', 'Toast Box Manager', 'ACTIVE', 'SUPPLIER', 'Toast Box Pte Ltd', '200312345A', NOW()),
('restaurant@paradise.sg', 'password123', '+6562345680', 'Paradise Dynasty Manager', 'ACTIVE', 'SUPPLIER', 'Paradise Group (Singapore) Pte Ltd', '200312345B', NOW()),
('market@fairprice.sg', 'password123', '+6562345681', 'FairPrice Manager', 'ACTIVE', 'SUPPLIER', 'FairPrice Singapore', '196400121H', NOW()),
('cafe@starbucks.sg', 'password123', '+6562345682', 'Starbucks Manager', 'ACTIVE', 'SUPPLIER', 'Starbucks Coffee Singapore', '201212345C', NOW());

-- ============================================
-- 4. ADMIN_PROFILES
-- ============================================
INSERT INTO admin_profiles (email, password, phone, display_name, role, status, created_at) VALUES
('admin@foodrescue.sg', 'password123', '+6590000001', 'System Admin', 'ADMIN', 'ACTIVE', NOW()),
('support@foodrescue.sg', 'password123', '+6590000002', 'Support Admin', 'ADMIN', 'ACTIVE', NOW());

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
(5, 'Starbucks Changi Airport', 'Airport outlet', '60 Airport Boulevard, #02-234', '819643', 1.3644, 103.9915, 'Counter pickup', 'Mon-Sun: 06:00-23:00', true, NOW());

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
INSERT INTO orders (store_id, consumer_id, status, pickup_slot_start, pickup_slot_end, total_amount, currency, cancel_reason, created_at, updated_at) VALUES
(1, 1, 'COMPLETED', '2026-01-29 18:00:00', '2026-01-29 19:30:00', 5.00, 'SGD', NULL, NOW(), NOW()),
(3, 2, 'COMPLETED', '2026-01-29 07:30:00', '2026-01-29 09:00:00', 4.50, 'SGD', NULL, NOW(), NOW()),
(5, 3, 'COMPLETED', '2026-01-29 14:30:00', '2026-01-29 16:00:00', 12.00, 'SGD', NULL, NOW(), NOW());

-- ============================================
-- 13. ORDER_ITEMS (Line items per order)
-- ============================================
INSERT INTO order_items (order_id, listing_id, quantity, unit_price, line_total) VALUES
(1, 1, 1, 5.00, 5.00),
(2, 3, 1, 4.50, 4.50),
(3, 5, 1, 12.00, 12.00);

-- ============================================
-- 14. PAYMENTS (Payment attempts per order)
-- ============================================
INSERT INTO payments (order_id, provider, provider_ref, amount, status, created_at, captured_at, refunded_at) VALUES
(1, 'STRIPE', 'STRP-1001', 5.00, 'SUCCEEDED', NOW(), NOW(), NULL),
(2, 'DBS_PAYLAH', 'DBS-2001', 4.50, 'SUCCEEDED', NOW(), NOW(), NULL),
(3, 'STRIPE', 'STRP-1002', 12.00, 'SUCCEEDED', NOW(), NOW(), NULL);

-- ============================================
-- 15. PICKUP_TOKENS (QR tokens for pickup)
-- ============================================
INSERT INTO pickup_tokens (order_id, qr_token_hash, issued_at, expires_at, used_at) VALUES
(1, 'hash-order-1', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), NOW()),
(2, 'hash-order-2', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), NOW()),
(3, 'hash-order-3', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), NULL);

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
-- END OF DATA INITIALIZATION
-- ============================================
