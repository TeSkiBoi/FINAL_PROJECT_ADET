-- =====================================================
-- Update Resident Ages to be Auto-Calculated
-- =====================================================
-- Date: November 30, 2025
-- Description: Recalculate all resident ages based on birthdates
-- =====================================================

USE barangay_biga_db;

-- Update all existing resident ages based on their birthdates
UPDATE residents 
SET age = TIMESTAMPDIFF(YEAR, birth_date, CURDATE()) - 
    CASE 
        WHEN DATE_FORMAT(CURDATE(), '%m%d') < DATE_FORMAT(birth_date, '%m%d') 
        THEN 1 
        ELSE 0 
    END
WHERE birth_date IS NOT NULL;

-- Verify the update
SELECT 
    resident_id,
    first_name,
    last_name,
    birth_date,
    age,
    TIMESTAMPDIFF(YEAR, birth_date, CURDATE()) - 
        CASE 
            WHEN DATE_FORMAT(CURDATE(), '%m%d') < DATE_FORMAT(birth_date, '%m%d') 
            THEN 1 
            ELSE 0 
        END as calculated_age
FROM residents
ORDER BY birth_date DESC
LIMIT 20;

-- Optional: Create a trigger to auto-update age on insert/update
-- Uncomment the following to create triggers:

/*
DELIMITER $$

-- Trigger for INSERT
CREATE TRIGGER before_resident_insert
BEFORE INSERT ON residents
FOR EACH ROW
BEGIN
    IF NEW.birth_date IS NOT NULL THEN
        SET NEW.age = TIMESTAMPDIFF(YEAR, NEW.birth_date, CURDATE()) - 
            CASE 
                WHEN DATE_FORMAT(CURDATE(), '%m%d') < DATE_FORMAT(NEW.birth_date, '%m%d') 
                THEN 1 
                ELSE 0 
            END;
    END IF;
END$$

-- Trigger for UPDATE
CREATE TRIGGER before_resident_update
BEFORE UPDATE ON residents
FOR EACH ROW
BEGIN
    IF NEW.birth_date IS NOT NULL THEN
        SET NEW.age = TIMESTAMPDIFF(YEAR, NEW.birth_date, CURDATE()) - 
            CASE 
                WHEN DATE_FORMAT(CURDATE(), '%m%d') < DATE_FORMAT(NEW.birth_date, '%m%d') 
                THEN 1 
                ELSE 0 
            END;
    END IF;
END$$

DELIMITER ;
*/

-- =====================================================
-- Notes:
-- =====================================================
-- The age calculation accounts for:
-- 1. The year difference
-- 2. Whether the birthday has occurred this year
-- 
-- Example: 
-- - Born: 2000-06-15
-- - Today: 2025-11-30
-- - Birthday already passed this year, so age = 25
--
-- Example:
-- - Born: 2000-12-15  
-- - Today: 2025-11-30
-- - Birthday not yet passed this year, so age = 24
-- =====================================================
