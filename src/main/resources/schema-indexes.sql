-- Database indexes for Resqeats performance optimization
-- These indexes support the common query patterns in the application

-- ===================== Orders Table Indexes =====================
-- Index for user's order history queries (OrderServiceImpl.findByUserId, findByUserIdAndStatus)
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_user_status ON orders(user_id, status);

-- Index for shop's order management (OrderServiceImpl.findByShopId, findByShopIdAndStatus)
CREATE INDEX IF NOT EXISTS idx_orders_shop_id ON orders(shop_id);
CREATE INDEX IF NOT EXISTS idx_orders_shop_status ON orders(shop_id, status);

-- Index for order expiry task (OrderExpiryTask queries by status and dates)
CREATE INDEX IF NOT EXISTS idx_orders_status_created ON orders(status, created_at);
CREATE INDEX IF NOT EXISTS idx_orders_status_pickup ON orders(status, pickup_time);

-- Index for shop rating calculations (OrderServiceImpl.calculateAverageRatingByShopId)
CREATE INDEX IF NOT EXISTS idx_orders_shop_rating ON orders(shop_id, rating) WHERE rating IS NOT NULL;

-- ===================== Payments Table Indexes =====================
-- Index for payment lookups by order
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id);

-- Index for payment status queries
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);

-- Index for transaction ID lookups (webhook processing)
CREATE INDEX IF NOT EXISTS idx_payments_preauth_tx ON payments(pre_auth_transaction_id);
CREATE INDEX IF NOT EXISTS idx_payments_capture_tx ON payments(capture_transaction_id);

-- ===================== User Payment Methods Table Indexes =====================
-- Index for user's payment methods
CREATE INDEX IF NOT EXISTS idx_user_payment_methods_user ON user_payment_methods(user_id, is_active);

-- ===================== Carts Table Indexes =====================
-- Index for user's cart lookup
CREATE INDEX IF NOT EXISTS idx_carts_user_id ON carts(user_id);
CREATE INDEX IF NOT EXISTS idx_carts_user_status ON carts(user_id, status);

-- Index for cart cleanup task (CartCleanupTask queries by status and expiry)
CREATE INDEX IF NOT EXISTS idx_carts_status_expiry ON carts(status, expires_at);

-- ===================== Shops Table Indexes =====================
-- Index for shop owner queries
CREATE INDEX IF NOT EXISTS idx_shops_owner_id ON shops(owner_id);

-- Index for shop status filtering (admin shop management)
CREATE INDEX IF NOT EXISTS idx_shops_status ON shops(status);
CREATE INDEX IF NOT EXISTS idx_shops_status_category ON shops(status, category);

-- Index for geospatial queries (nearby shops) - Note: Consider PostGIS for production
CREATE INDEX IF NOT EXISTS idx_shops_location ON shops(latitude, longitude);

-- ===================== Secret Boxes Table Indexes =====================
-- Index for shop's secret boxes
CREATE INDEX IF NOT EXISTS idx_secret_boxes_shop_id ON secret_boxes(shop_id);

-- Index for available secret boxes query (SecretBoxServiceImpl.findAvailableByShopId)
CREATE INDEX IF NOT EXISTS idx_secret_boxes_shop_available ON secret_boxes(shop_id, is_available, quantity);

-- ===================== Notifications Table Indexes =====================
-- Index for user's notifications (NotificationServiceImpl.getUserNotifications)
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications(user_id, is_read);

-- ===================== Users Table Indexes =====================
-- Index for email lookup (authentication)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Index for username lookup
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- ===================== Refresh Tokens Table Indexes =====================
-- Index for token lookup
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_tokens(user_id);

-- Index for token cleanup (expired tokens)
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expiry ON refresh_tokens(expiry_date);
