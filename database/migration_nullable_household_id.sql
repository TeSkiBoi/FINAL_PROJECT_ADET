-- Migration Script: Make household_id nullable in residents table
-- Date: November 30, 2025
-- Purpose: Remove requirement for household_id to allow residents without households

-- Step 1: Modify the residents table to make household_id nullable
ALTER TABLE residents 
MODIFY COLUMN household_id INT NULL;

-- Step 2: Optionally, remove the foreign key constraint if it exists
-- (Uncomment the lines below if you have a foreign key constraint)
-- ALTER TABLE residents DROP FOREIGN KEY fk_residents_households;

-- Step 3: Optionally, recreate the foreign key with ON DELETE SET NULL
-- (Uncomment if you want to keep the foreign key but allow null values)
-- ALTER TABLE residents 
-- ADD CONSTRAINT fk_residents_households 
-- FOREIGN KEY (household_id) REFERENCES households(household_id) 
-- ON DELETE SET NULL ON UPDATE CASCADE;

-- Note: This migration allows residents to exist independently without being 
-- associated with a household. Existing residents with household_id values 
-- will remain unchanged.

-- Verify the change
DESCRIBE residents;
