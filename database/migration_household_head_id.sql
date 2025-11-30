-- =====================================================
-- Household Table Schema Update Migration
-- =====================================================
-- Date: November 30, 2025
-- Description: Update households table to use household_head_id 
--              instead of full_name/head_fullname column
-- =====================================================

USE inventorydb;

-- Step 1: Backup existing data (optional but recommended)
-- CREATE TABLE households_backup_20251130 AS SELECT * FROM households;

-- Step 2: Check if household_head_id column exists, if not add it
ALTER TABLE households 
ADD COLUMN IF NOT EXISTS household_head_id INT(11) NULL AFTER family_no;

-- Step 3: Add timestamps if they don't exist
ALTER TABLE households 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER income;

ALTER TABLE households 
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

-- Step 4: Populate household_head_id with the first resident_id from each household
UPDATE households h
SET h.household_head_id = (
    SELECT MIN(r.resident_id) 
    FROM residents r 
    WHERE r.household_id = h.household_id
    LIMIT 1
)
WHERE h.household_head_id IS NULL;

-- Step 5: Add index on household_head_id for better query performance
ALTER TABLE households
ADD INDEX IF NOT EXISTS idx_household_head_id (household_head_id);

-- Step 6: Add foreign key constraint (optional - only if residents table exists)
-- Uncomment the following lines if you want to enforce referential integrity:
-- ALTER TABLE households
-- ADD CONSTRAINT fk_household_head
-- FOREIGN KEY (household_head_id) REFERENCES residents(resident_id)
-- ON DELETE SET NULL
-- ON UPDATE CASCADE;

-- Step 7: Drop old full_name or head_fullname column
-- WARNING: Only run this after confirming all code has been updated!
-- Uncomment one of the following lines based on your current column name:

-- ALTER TABLE households DROP COLUMN IF EXISTS full_name;
-- OR
-- ALTER TABLE households DROP COLUMN IF EXISTS head_fullname;

-- Step 8: Verify the new schema
DESCRIBE households;

-- Expected output:
-- household_id         INT(11)       PRIMARY KEY AUTO_INCREMENT
-- family_no            INT(11)       NOT NULL
-- household_head_id    INT(11)       NULL (with index)
-- address              VARCHAR(255)  NOT NULL
-- income               DECIMAL(12,2) NULL DEFAULT 0.00
-- created_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
-- updated_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

-- =====================================================
-- Rollback Script (if needed)
-- =====================================================
-- To rollback this migration:
-- 1. Restore from backup: INSERT INTO households SELECT * FROM households_backup_20251130;
-- 2. Drop new columns: ALTER TABLE households DROP COLUMN household_head_id, DROP COLUMN created_at, DROP COLUMN updated_at;
-- 3. Add back old column: ALTER TABLE households ADD COLUMN full_name VARCHAR(255) AFTER family_no;
