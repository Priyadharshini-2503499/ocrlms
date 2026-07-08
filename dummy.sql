-- =====================================================================
--  Omnichannel Retail & Customer Loyalty Management System
--  dummy.sql  —  sample data for manual testing
-- ---------------------------------------------------------------------
--  HOW TO RUN
--    1. Boot the app ONCE so Hibernate (ddl-auto=update) builds all tables.
--    2. Stop the app (optional) and run:
--         mysql -u orclms -p orclms_db < dummy.sql
--       ...or paste this whole file into MySQL Workbench against orclms_db.
--
--  Re-runnable: it clears the tables first, then re-seeds with fixed IDs.
--  Column/table names match the JPA entities exactly (camelCase preserved
--  by PhysicalNamingStrategyStandardImpl).
-- =====================================================================

USE orclms_db;

-- ---------------------------------------------------------------------
-- 0. Reset (child -> parent order; FK checks off so order doesn't matter)
-- ---------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `returnrequest`;
TRUNCATE TABLE `order_items`;
TRUNCATE TABLE `orders`;
TRUNCATE TABLE `coupon`;
TRUNCATE TABLE `product`;
TRUNCATE TABLE `Customer`;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- 1. Customers  (covers every tier + boundary point values)
-- ---------------------------------------------------------------------
INSERT INTO `Customer`
  (`customerId`, `fullName`, `emailId`, `phoneNumber`, `loyaltyPoints`, `loyaltyTier`) VALUES
  (1, 'Aarav Sharma',  'aarav.sharma@example.com',  '9876500001',  250, 'SILVER'),
  (2, 'Diya Patel',    'diya.patel@example.com',    '9876500002', 1500, 'GOLD'),
  (3, 'Vihaan Reddy',  'vihaan.reddy@example.com',  '9876500003', 5200, 'PLATINUM'),
  (4, 'Ananya Iyer',   'ananya.iyer@example.com',   '9876500004',    0, 'SILVER'),   -- zero points (redeem button disabled)
  (5, 'Kabir Nair',    'kabir.nair@example.com',    '9876500005', 1000, 'GOLD'),     -- boundary: exactly GOLD threshold
  (6, 'Saanvi Gupta',  'saanvi.gupta@example.com',  '9876500006',  999, 'SILVER'),   -- boundary: just below GOLD
  (7, 'Reyansh Mehta', 'reyansh.mehta@example.com', '9876500007', 2000, 'PLATINUM'), -- boundary: exactly PLATINUM threshold
  (8, 'Ishaan Verma',  'ishaan.verma@example.com',  '9876500008',  120, 'SILVER');

-- ---------------------------------------------------------------------
-- 2. Products  (every status + a free ₹0 item edge case)
-- ---------------------------------------------------------------------
INSERT INTO `product`
  (`productId`, `skuCode`, `productName`, `category`, `basePrice`, `productStatus`) VALUES
  (1, 'SKU-1001', 'Trail Runner Shoe',   'Footwear',    4999.00, 'ACTIVE'),
  (2, 'SKU-1002', 'Merino Wool Sweater', 'Apparel',     2999.00, 'ACTIVE'),
  (3, 'SKU-1003', 'Insulated Bottle',    'Accessories',  899.00, 'ACTIVE'),
  (4, 'SKU-1004', 'Yoga Mat',            'Fitness',      1499.00, 'ACTIVE'),
  (5, 'SKU-1005', 'Classic Cap',         'Accessories',  599.00, 'INACTIVE'),     -- cannot be ordered
  (6, 'SKU-1006', 'Vintage Jacket',      'Apparel',     7999.00, 'DISCONTINUED'), -- cannot be ordered
  (7, 'SKU-1007', 'Cotton Socks 3pk',    'Apparel',      499.00, 'ACTIVE'),
  (8, 'SKU-1008', 'Free Sample Sachet',  'Samples',        0.00, 'ACTIVE');        -- ₹0 edge case

-- ---------------------------------------------------------------------
-- 3. Orders  (every status + every channel). Totals match their items.
-- ---------------------------------------------------------------------
INSERT INTO `orders`
  (`orderId`, `customerId`, `orderChannel`, `totalAmount`, `orderDate`, `orderStatus`) VALUES
  (1, 1, 'ONLINE', 6797.00, '2026-06-01', 'PLACED'),
  (2, 2, 'STORE',  2999.00, '2026-06-02', 'CONFIRMED'),
  (3, 3, 'MOBILE', 2996.00, '2026-06-03', 'SHIPPED'),
  (4, 5, 'ONLINE', 9998.00, '2026-06-04', 'DELIVERED'),
  (5, 4, 'STORE',   899.00, '2026-06-05', 'CANCELLED'),
  (6, 7, 'MOBILE', 4498.00, '2026-06-06', 'DELIVERED'),
  (7, 8, 'ONLINE',  499.00, '2026-06-10', 'PLACED');

-- ---------------------------------------------------------------------
-- 4. Order line items  (unitPrice snapshotted from product basePrice)
-- ---------------------------------------------------------------------
INSERT INTO `order_items`
  (`orderItemId`, `orderId`, `productId`, `quantity`, `unitPrice`) VALUES
  (1, 1, 1, 1, 4999.00),
  (2, 1, 3, 2,  899.00),   -- order 1 total = 4999 + 1798 = 6797
  (3, 2, 2, 1, 2999.00),
  (4, 3, 4, 1, 1499.00),
  (5, 3, 7, 3,  499.00),   -- order 3 total = 1499 + 1497 = 2996
  (6, 4, 1, 2, 4999.00),   -- order 4 total = 9998
  (7, 5, 3, 1,  899.00),
  (8, 6, 2, 1, 2999.00),
  (9, 6, 4, 1, 1499.00),   -- order 6 total = 4498
  (10, 7, 7, 1, 499.00);

-- ---------------------------------------------------------------------
-- 5. Return requests  (OneToOne: max ONE per order; every status)
--    refundAmount mirrors the linked order's total.
-- ---------------------------------------------------------------------
INSERT INTO `returnrequest`
  (`returnId`, `orderId`, `returnReason`, `refundAmount`, `requestDate`, `returnStatus`) VALUES
  (1, 4, 'Wrong size delivered',     9998.00, '2026-06-07', 'REFUNDED'),
  (2, 3, 'Changed my mind',          2996.00, '2026-06-08', 'REQUESTED'),
  (3, 6, 'Item arrived defective',   4498.00, '2026-06-09', 'APPROVED'),
  (4, 2, 'No longer needed',         2999.00, '2026-06-09', 'REJECTED');

-- ---------------------------------------------------------------------
-- 6. Coupons  (every status; mix of PERCENTAGE and FLAT)
--    Today = 2026-06-15, so date windows make validity realistic.
-- ---------------------------------------------------------------------
INSERT INTO `coupon`
  (`couponId`, `couponCode`, `discountType`, `discountValue`, `validFrom`, `validTo`, `couponStatus`) VALUES
  (1, 'WELCOME10',  'PERCENTAGE', 10.0,  '2026-01-01', '2026-12-31', 'ACTIVE'),
  (2, 'FLAT500',    'FLAT',      500.0,  '2026-01-01', '2026-12-31', 'ACTIVE'),
  (3, 'GOLD20',     'PERCENTAGE', 20.0,  '2026-06-01', '2026-06-30', 'ACTIVE'),
  (4, 'SUMMER15',   'PERCENTAGE', 15.0,  '2025-06-01', '2025-08-31', 'EXPIRED'),   -- past window
  (5, 'NEWYEAR50',  'FLAT',     1000.0,  '2026-01-01', '2026-01-15', 'EXPIRED'),   -- past window
  (6, 'FESTIVE25',  'PERCENTAGE', 25.0,  '2026-03-01', '2026-03-31', 'REDEEMED');  -- already used

-- ---------------------------------------------------------------------
-- Keep AUTO_INCREMENT counters above the seeded IDs so app inserts continue cleanly
-- ---------------------------------------------------------------------
ALTER TABLE `Customer`      AUTO_INCREMENT = 9;
ALTER TABLE `product`       AUTO_INCREMENT = 9;
ALTER TABLE `orders`        AUTO_INCREMENT = 8;
ALTER TABLE `order_items`   AUTO_INCREMENT = 11;
ALTER TABLE `returnrequest` AUTO_INCREMENT = 5;
ALTER TABLE `coupon`        AUTO_INCREMENT = 7;

-- Done.  Quick sanity check:
--   SELECT (SELECT COUNT(*) FROM Customer)   AS customers,
--          (SELECT COUNT(*) FROM product)    AS products,
--          (SELECT COUNT(*) FROM orders)     AS orders,
--          (SELECT COUNT(*) FROM order_items)AS items,
--          (SELECT COUNT(*) FROM returnrequest) AS returns,
--          (SELECT COUNT(*) FROM coupon)     AS coupons;

