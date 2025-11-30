-- =====================================================
-- Add Suffix Field to Residents Table
-- =====================================================
-- Date: November 30, 2025
-- Description: Add suffix field to residents table for name suffixes like Jr., Sr., II, III, etc.
-- =====================================================

USE barangay_biga_db;

-- Check if suffix column already exists before adding
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'residents' 
AND COLUMN_NAME = 'suffix';

-- Add suffix column after last_name
ALTER TABLE residents 
ADD COLUMN suffix VARCHAR(20) NULL AFTER last_name;

-- Verify the column was added
DESCRIBE residents;

-- Expected output should include:
-- resident_id      INT(11)      PRIMARY KEY AUTO_INCREMENT
-- household_id     INT(11)      INDEX
-- first_name       VARCHAR(100) NOT NULL
-- middle_name      VARCHAR(100) NULL
-- last_name        VARCHAR(100) NOT NULL
-- suffix           VARCHAR(20)  NULL  ← NEW COLUMN
-- birth_date       DATE         NULL
-- age              INT(11)      NOT NULL
-- gender           VARCHAR(20)  NULL
-- contact_no       VARCHAR(20)  NULL
-- email            VARCHAR(150) NULL
-- created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
-- updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

-- =====================================================
-- Common Suffix Values
-- =====================================================
-- Jr.    - Junior
-- Sr.    - Senior
-- II     - The Second
-- III    - The Third
-- IV     - The Fourth
-- V      - The Fifth
-- Esq.   - Esquire (for lawyers)
-- PhD    - Doctor of Philosophy
-- MD     - Medical Doctor

-- =====================================================
-- Sample Queries
-- =====================================================

-- Find residents with suffix
SELECT first_name, middle_name, last_name, suffix 
FROM residents 
WHERE suffix IS NOT NULL;

-- Full name with suffix
SELECT CONCAT(first_name, ' ', 
              COALESCE(middle_name, ''), ' ', 
              last_name, ' ',
              COALESCE(suffix, '')) AS full_name
FROM residents;

-- Count by suffix
SELECT suffix, COUNT(*) as count
FROM residents
WHERE suffix IS NOT NULL
GROUP BY suffix;

-- =====================================================
-- Rollback Script (if needed)
-- =====================================================
-- To remove the suffix column:
-- ALTER TABLE residents DROP COLUMN suffix;

-- =====================================================
-- Notes
-- =====================================================
-- 1. Suffix is optional (NULL allowed)
-- 2. Max length: 20 characters
-- 3. Position: After last_name for logical ordering
-- 4. Common values provided in UI dropdown
-- 5. Dropdown is editable for custom suffixes
