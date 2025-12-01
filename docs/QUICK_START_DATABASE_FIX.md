# Quick Start: Financial Transactions Database Fix

## Step 1: Run Database Migration

Open MySQL Workbench or command line and run:

```sql
USE barangay_biga_db;

-- Add payment_method column
ALTER TABLE financial_transactions 
ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50) NOT NULL DEFAULT 'Cash' 
AFTER description;

-- Add payee_payer column
ALTER TABLE financial_transactions 
ADD COLUMN IF NOT EXISTS payee_payer VARCHAR(255) NULL 
AFTER payment_method;

-- Add reference_no column
ALTER TABLE financial_transactions 
ADD COLUMN IF NOT EXISTS reference_no VARCHAR(100) NULL 
AFTER payee_payer;

-- Update transaction_date to TIMESTAMP
ALTER TABLE financial_transactions 
MODIFY COLUMN transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Verify structure
DESCRIBE financial_transactions;
```

## Step 2: Restart Eclipse

1. Close Eclipse
2. Reopen Eclipse
3. Clean Project: **Project → Clean → Select FINAL_PROJECT_ADET → OK**

## Step 3: Test the Application

1. Run the application
2. Navigate to Financial panel
3. Click "Add Transaction"
4. Fill in all fields including:
   - Date & Time
   - Type (Income/Expense)
   - Category
   - Amount
   - Description
   - **Payment Method** ← NEW
   - **Payee/Payer** ← NEW
   - **Reference No** ← NEW
5. Save and verify it appears in the table

## What's Fixed

✅ Payment method now saves and displays correctly
✅ Date AND time are stored (not just date)
✅ Payee/Payer field for tracking parties
✅ Reference number field for receipts
✅ All database queries updated
✅ UI fields added for complete data entry

## If You Get Errors

1. **Column already exists**: That's OK, it means the column was already there
2. **Unknown column**: Run the full migration script from `financial_transactions_schema.sql`
3. **Compilation errors**: Clean and rebuild the project

## Need to Rollback?

If you need to undo changes, run:

```sql
-- Remove added columns (WARNING: This deletes data in those columns)
ALTER TABLE financial_transactions DROP COLUMN reference_no;
ALTER TABLE financial_transactions DROP COLUMN payee_payer;
ALTER TABLE financial_transactions DROP COLUMN payment_method;
```

**Note**: Don't rollback unless absolutely necessary - existing data will work fine!
