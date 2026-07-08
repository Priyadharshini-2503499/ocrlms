CREATE SCHEMA omnichannel;
use omnichannel;

CREATE TABLE Product (
                         productId INT AUTO_INCREMENT PRIMARY KEY,
                         skuCode VARCHAR(50),
                         productName VARCHAR(100),
                         category VARCHAR(100),
                         basePrice DECIMAL(10,2),
                         productStatus ENUM('ACTIVE','INACTIVE','DISCONTINUED')
);

CREATE TABLE Customer (
                          customerId INT AUTO_INCREMENT PRIMARY KEY,
                          fullName VARCHAR(100),
                          emailId VARCHAR(100),
                          phoneNumber VARCHAR(20),
                          loyaltyPoints INT,
                          loyaltyTier ENUM('SILVER','GOLD','PLATINUM')
);

CREATE TABLE Orders (
                        orderId INT AUTO_INCREMENT PRIMARY KEY,
                        customerId INT,
                        orderChannel VARCHAR(50),
                        totalAmount DECIMAL(10,2),
                        orderDate DATE,
                        orderStatus ENUM('PLACED','CONFIRMED','SHIPPED','DELIVERED','CANCELLED'),
                        FOREIGN KEY (customerId) REFERENCES Customer(customerId)
);

CREATE TABLE Coupon (
                        couponId INT AUTO_INCREMENT PRIMARY KEY,
                        couponCode VARCHAR(50),
                        discountType VARCHAR(50),
                        discountValue DECIMAL(10,2),
                        validFrom DATE,
                        validTo DATE,
                        couponStatus ENUM('ACTIVE','EXPIRED','REDEEMED')
);

CREATE TABLE ReturnRequest (
                               returnId INT AUTO_INCREMENT PRIMARY KEY,
                               orderId INT,
                               returnReason VARCHAR(255),
                               refundAmount DECIMAL(10,2),
                               requestDate DATE,
                               returnStatus ENUM('REQUESTED','APPROVED','REJECTED','REFUNDED'),
                               FOREIGN KEY (orderId) REFERENCES Orders(orderId)
);