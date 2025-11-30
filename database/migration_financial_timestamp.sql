-- =====================================================
-- Financial Transactions - Update to TIMESTAMP
-- =====================================================
-- Date: November 30, 2025
-- Description: Ensure transaction_date is TIMESTAMP to store date AND time
-- =====================================================

USE barangay_biga_db;

-- Check current column type
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'financial_transactions' 
AND COLUMN_NAME = 'transaction_date';

-- Modify transaction_date to TIMESTAMP if it's currently DATE
ALTER TABLE financial_transactions 
MODIFY COLUMN transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Verify the change
DESCRIBE financial_transactions;

-- Expected output:
-- transaction_id     INT(11)       PRIMARY KEY AUTO_INCREMENT
-- transaction_date   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ← Updated
-- transaction_type   VARCHAR(50)   NOT NULL
-- category           VARCHAR(100)  NOT NULL
-- amount             DECIMAL(12,2) NOT NULL
-- description        TEXT          NULL
-- payment_method     VARCHAR(50)   NOT NULL
-- created_at         TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
-- updated_at         TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

-- =====================================================
-- Sample Time Values
-- =====================================================

-- Insert transaction with specific date and time
INSERT INTO financial_transactions 
(transaction_date, transaction_type, category, amount, description, payment_method)
VALUES 
('2025-11-30 14:30:00', 'Income', 'Donation', 5000.00, 'Monthly donation', 'Cash'),
('2025-11-30 09:15:00', 'Expense', 'Supplies', 1500.50, 'Office supplies', 'Bank Transfer');

-- Query with formatted time display
SELECT 
    transaction_id,
    DATE_FORMAT(transaction_date, '%Y-%m-%d %h:%i:%s %p') as formatted_datetime,
    transaction_type,
    category,
    amount
FROM financial_transactions
ORDER BY transaction_date DESC;

-- =====================================================
-- Time Format Examples
-- =====================================================

-- 12-hour format with AM/PM
SELECT DATE_FORMAT(transaction_date, '%Y-%m-%d %h:%i:%s %p') as datetime_12h
FROM financial_transactions;
-- Output: 2025-11-30 02:30:00 PM

-- 24-hour format
SELECT DATE_FORMAT(transaction_date, '%Y-%m-%d %H:%i:%s') as datetime_24h
FROM financial_transactions;
-- Output: 2025-11-30 14:30:00

-- Date only
SELECT DATE_FORMAT(transaction_date, '%Y-%m-%d') as date_only
FROM financial_transactions;
-- Output: 2025-11-30

-- Time only (12-hour with AM/PM)
SELECT DATE_FORMAT(transaction_date, '%h:%i %p') as time_12h
FROM financial_transactions;
-- Output: 02:30 PM

-- =====================================================
-- Useful Queries
-- =====================================================

-- Transactions today
SELECT * FROM financial_transactions
WHERE DATE(transaction_date) = CURDATE();

-- Transactions this month
SELECT * FROM financial_transactions
WHERE YEAR(transaction_date) = YEAR(CURDATE())
AND MONTH(transaction_date) = MONTH(CURDATE());

-- Transactions between specific times
SELECT * FROM financial_transactions
WHERE transaction_date BETWEEN '2025-11-01 00:00:00' AND '2025-11-30 23:59:59';

-- Sum by day
SELECT 
    DATE(transaction_date) as date,
    transaction_type,
    SUM(amount) as total
FROM financial_transactions
GROUP BY DATE(transaction_date), transaction_type
ORDER BY date DESC;

-- =====================================================
-- Rollback (if needed)
-- =====================================================

-- To revert to DATE type (WARNING: will lose time information):
-- ALTER TABLE financial_transactions 
-- MODIFY COLUMN transaction_date DATE NOT NULL;

-- =====================================================
-- Notes
-- =====================================================
-- 1. TIMESTAMP stores both date and time
-- 2. Range: '1970-01-01 00:00:01' to '2038-01-19 03:14:07'
-- 3. Automatically converts to UTC for storage
-- 4. Uses 4 bytes of storage
-- 5. Can store with millisecond precision if needed
-- 6. DEFAULT CURRENT_TIMESTAMP auto-sets current time on insert
