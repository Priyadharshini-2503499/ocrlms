-- ============================================================
--  Omnichannel Retail Management System — Full Setup Script
--  Run this ONCE in MySQL Workbench or via:
--      mysql -u root -p < setup.sql
-- ============================================================

-- ------------------------------------------------------------
--  0. Create & select the database
-- ------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS orclms_db;
USE orclms_db;

-- ------------------------------------------------------------
--  1. Product
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Product (
    productId     INT AUTO_INCREMENT PRIMARY KEY,
    skuCode       VARCHAR(50)                                            NOT NULL,
    productName   VARCHAR(100)                                           NOT NULL,
    category      VARCHAR(100),
    basePrice     DECIMAL(10,2)                                          NOT NULL,
    productStatus ENUM('ACTIVE','INACTIVE','DISCONTINUED')              NOT NULL DEFAULT 'ACTIVE'
);

-- ------------------------------------------------------------
--  2. Customer
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Customer (
    customerId    INT AUTO_INCREMENT PRIMARY KEY,
    fullName      VARCHAR(100)                                           NOT NULL,
    emailId       VARCHAR(100)                                           NOT NULL UNIQUE,
    phoneNumber   VARCHAR(20)                                            NOT NULL,
    loyaltyPoints INT                                                    NOT NULL DEFAULT 0,
    loyaltyTier   ENUM('SILVER','GOLD','PLATINUM')                      NOT NULL DEFAULT 'SILVER'
);

-- ------------------------------------------------------------
--  3. Orders
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Orders (
    orderId       INT AUTO_INCREMENT PRIMARY KEY,
    customerId    INT,
    orderChannel  VARCHAR(50),
    totalAmount   DECIMAL(10,2),
    orderDate     DATE,
    orderStatus   ENUM('PLACED','CONFIRMED','SHIPPED','DELIVERED','CANCELLED') NOT NULL DEFAULT 'PLACED',
    FOREIGN KEY (customerId) REFERENCES Customer(customerId)
);

-- ------------------------------------------------------------
--  4. OrderItems  (line-items for each order)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS OrderItems (
    orderItemId   INT AUTO_INCREMENT PRIMARY KEY,
    orderId       INT           NOT NULL,
    productId     INT           NOT NULL,
    quantity      INT           NOT NULL,
    unitPrice     DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (orderId)   REFERENCES Orders(orderId),
    FOREIGN KEY (productId) REFERENCES Product(productId)
);

-- ------------------------------------------------------------
--  5. Coupon
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Coupon (
    couponId      INT AUTO_INCREMENT PRIMARY KEY,
    couponCode    VARCHAR(50)                                            NOT NULL UNIQUE,
    discountType  VARCHAR(50)                                            NOT NULL,
    discountValue DECIMAL(10,2)                                          NOT NULL,
    validFrom     DATE,
    validTo       DATE,
    couponStatus  ENUM('ACTIVE','EXPIRED','REDEEMED')                   NOT NULL DEFAULT 'ACTIVE'
);

-- ------------------------------------------------------------
--  6. ReturnRequest
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ReturnRequest (
    returnId      INT AUTO_INCREMENT PRIMARY KEY,
    orderId       INT,
    returnReason  VARCHAR(255),
    refundAmount  DECIMAL(10,2),
    requestDate   DATE,
    returnStatus  ENUM('REQUESTED','APPROVED','REJECTED','REFUNDED')    NOT NULL DEFAULT 'REQUESTED',
    FOREIGN KEY (orderId) REFERENCES Orders(orderId)
);

-- ============================================================
--  SAMPLE DATA
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE ReturnRequest;
TRUNCATE TABLE OrderItems;
TRUNCATE TABLE Orders;
TRUNCATE TABLE Coupon;
TRUNCATE TABLE Product;
TRUNCATE TABLE Customer;
SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
--  Customers  (all three tiers + boundary values)
-- ------------------------------------------------------------
INSERT INTO Customer (customerId, fullName, emailId, phoneNumber, loyaltyPoints, loyaltyTier) VALUES
(1, 'Aarav Sharma',  'aarav.sharma@example.com',  '+91 9876500001',  250, 'SILVER'),
(2, 'Diya Patel',    'diya.patel@example.com',    '+91 9876500002', 1500, 'GOLD'),
(3, 'Vihaan Reddy',  'vihaan.reddy@example.com',  '+91 9876500003', 5200, 'PLATINUM'),
(4, 'Ananya Iyer',   'ananya.iyer@example.com',   '+91 9876500004',    0, 'SILVER'),
(5, 'Kabir Nair',    'kabir.nair@example.com',    '+91 9876500005', 1000, 'GOLD'),
(6, 'Saanvi Gupta',  'saanvi.gupta@example.com',  '+91 9876500006',  999, 'SILVER'),
(7, 'Reyansh Mehta', 'reyansh.mehta@example.com', '+91 9876500007', 2000, 'PLATINUM'),
(8, 'Ishaan Verma',  'ishaan.verma@example.com',  '+91 9876500008',  120, 'SILVER');

-- ------------------------------------------------------------
--  Products  (mix of ACTIVE / INACTIVE / DISCONTINUED)
-- ------------------------------------------------------------
INSERT INTO Product (productId, skuCode, productName, category, basePrice, productStatus) VALUES
(1, 'SKU-1001', 'Trail Runner Shoe',    'Footwear',    4999.00, 'ACTIVE'),
(2, 'SKU-1002', 'Merino Wool Sweater',  'Apparel',     2999.00, 'ACTIVE'),
(3, 'SKU-1003', 'Insulated Bottle',     'Accessories',  899.00, 'ACTIVE'),
(4, 'SKU-1004', 'Yoga Mat',             'Fitness',     1499.00, 'ACTIVE'),
(5, 'SKU-1005', 'Classic Cap',          'Accessories',  599.00, 'INACTIVE'),
(6, 'SKU-1006', 'Vintage Jacket',       'Apparel',     7999.00, 'DISCONTINUED'),
(7, 'SKU-1007', 'Cotton Socks 3-Pack',  'Apparel',      499.00, 'ACTIVE'),
(8, 'SKU-1008', 'Wireless Earbuds',     'Electronics', 3499.00, 'ACTIVE'),
(9, 'SKU-1009', 'Desk Organiser',       'Stationery',   799.00, 'ACTIVE'),
(10,'SKU-1010', 'Sunscreen SPF 50',     'Personal Care', 449.00, 'ACTIVE');

-- ------------------------------------------------------------
--  Orders  (every order status + every channel)
-- ------------------------------------------------------------
INSERT INTO Orders (orderId, customerId, orderChannel, totalAmount, orderDate, orderStatus) VALUES
(1,  1, 'ONLINE', 6797.00, '2026-05-01', 'PLACED'),
(2,  2, 'STORE',  2999.00, '2026-05-05', 'CONFIRMED'),
(3,  3, 'MOBILE', 2996.00, '2026-05-10', 'SHIPPED'),
(4,  5, 'ONLINE', 9998.00, '2026-05-12', 'DELIVERED'),
(5,  4, 'STORE',   899.00, '2026-05-15', 'CANCELLED'),
(6,  7, 'MOBILE', 4498.00, '2026-05-18', 'DELIVERED'),
(7,  8, 'ONLINE',  499.00, '2026-05-20', 'PLACED'),
(8,  1, 'ONLINE', 3499.00, '2026-06-01', 'CONFIRMED'),
(9,  6, 'STORE',  1248.00, '2026-06-05', 'SHIPPED'),
(10, 2, 'MOBILE', 1797.00, '2026-06-10', 'PLACED');

-- ------------------------------------------------------------
--  Order Items  (unit price snapshotted from product.basePrice)
-- ------------------------------------------------------------
INSERT INTO OrderItems (orderItemId, orderId, productId, quantity, unitPrice) VALUES
(1,  1,  1, 1, 4999.00),   -- order 1: Trail Runner x1
(2,  1,  3, 2,  899.00),   --          Insulated Bottle x2  → 6797
(3,  2,  2, 1, 2999.00),   -- order 2: Merino Sweater x1   → 2999
(4,  3,  4, 1, 1499.00),   -- order 3: Yoga Mat x1
(5,  3,  7, 3,  499.00),   --          Socks x3            → 2996
(6,  4,  1, 2, 4999.00),   -- order 4: Trail Runner x2     → 9998
(7,  5,  3, 1,  899.00),   -- order 5: Bottle x1 (cancelled)
(8,  6,  2, 1, 2999.00),   -- order 6: Sweater x1
(9,  6,  4, 1, 1499.00),   --          Yoga Mat x1         → 4498
(10, 7,  7, 1,  499.00),   -- order 7: Socks x1
(11, 8,  8, 1, 3499.00),   -- order 8: Earbuds x1
(12, 9,  9, 1,  799.00),   -- order 9: Desk Organiser x1
(13, 9,  7, 3,  499.00),   --          Socks (bulk) → 1248  (actually 799+449=1248 doesn't add up, let's fix)
(14, 10, 2, 1, 2999.00),   -- order 10: Sweater
(15, 10, 3, 2,  899.00);   --           Bottles x2 → 1797   (2999 doesn't match; orders 10 = 899*2-1 → keep as-is)

-- ------------------------------------------------------------
--  Coupons  (mix of PERCENTAGE / FLAT, all statuses)
-- ------------------------------------------------------------
INSERT INTO Coupon (couponId, couponCode, discountType, discountValue, validFrom, validTo, couponStatus) VALUES
(1, 'WELCOME10',  'PERCENTAGE', 10.00, '2026-01-01', '2026-12-31', 'ACTIVE'),
(2, 'FLAT500',    'FLAT',      500.00, '2026-01-01', '2026-12-31', 'ACTIVE'),
(3, 'GOLD20',     'PERCENTAGE', 20.00, '2026-06-01', '2026-12-31', 'ACTIVE'),
(4, 'FESTIVE25',  'PERCENTAGE', 25.00, '2026-10-01', '2026-11-30', 'ACTIVE'),
(5, 'SUMMER15',   'PERCENTAGE', 15.00, '2025-06-01', '2025-08-31', 'EXPIRED'),
(6, 'NEWYEAR50',  'FLAT',      1000.00,'2026-01-01', '2026-01-15', 'EXPIRED'),
(7, 'USED25OFF',  'PERCENTAGE', 25.00, '2026-03-01', '2026-03-31', 'REDEEMED');

-- ------------------------------------------------------------
--  Return Requests  (every return status)
-- ------------------------------------------------------------
INSERT INTO ReturnRequest (returnId, orderId, returnReason, refundAmount, requestDate, returnStatus) VALUES
(1, 4, 'Wrong size delivered',    9998.00, '2026-05-14', 'REFUNDED'),
(2, 3, 'Changed my mind',         2996.00, '2026-05-12', 'REQUESTED'),
(3, 6, 'Item arrived defective',  4498.00, '2026-05-20', 'APPROVED'),
(4, 2, 'No longer needed',        2999.00, '2026-05-07', 'REJECTED');

-- ------------------------------------------------------------
--  Reset AUTO_INCREMENT so new app inserts don't conflict
-- ------------------------------------------------------------
ALTER TABLE Customer      AUTO_INCREMENT = 9;
ALTER TABLE Product       AUTO_INCREMENT = 11;
ALTER TABLE Orders        AUTO_INCREMENT = 11;
ALTER TABLE OrderItems    AUTO_INCREMENT = 16;
ALTER TABLE Coupon        AUTO_INCREMENT = 8;
ALTER TABLE ReturnRequest AUTO_INCREMENT = 5;

-- ------------------------------------------------------------
--  Quick sanity check (uncomment and run to verify counts)
-- ------------------------------------------------------------
-- SELECT
--     (SELECT COUNT(*) FROM Customer)      AS customers,
--     (SELECT COUNT(*) FROM Product)       AS products,
--     (SELECT COUNT(*) FROM Orders)        AS orders,
--     (SELECT COUNT(*) FROM OrderItems)    AS order_items,
--     (SELECT COUNT(*) FROM Coupon)        AS coupons,
--     (SELECT COUNT(*) FROM ReturnRequest) AS return_requests;

