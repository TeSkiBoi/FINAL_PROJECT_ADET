# Household Head Name Update - Implementation Summary

## Date: November 30, 2025

## Overview
Updated the household management system to properly retrieve household head names from the `residents` table via SQL JOIN instead of storing them redundantly in the `households` table.

## Changes Made

### 1. HouseholdPanel.java - loadHouseholds() Method
**Location:** Line ~133-155

**Previous Query:**
```sql
SELECT h.household_id, h.family_no, CONCAT(first), h.address, h.income,
COUNT(r.resident_id) as member_count
FROM households h
LEFT JOIN residents r ON h.household_id = r.household_id
GROUP BY h.household_id ORDER BY h.household_id
```
**Issues:** Incomplete CONCAT function, incorrect column reference

**New Query:**
```sql
SELECT h.household_id, h.family_no,
CONCAT(COALESCE(r.first_name, ''), ' ', COALESCE(r.middle_name, ''), ' ', COALESCE(r.last_name, '')) AS head_name,
h.address, h.income,
(SELECT COUNT(*) FROM residents r2 WHERE r2.household_id = h.household_id) as member_count
FROM households h
LEFT JOIN residents r ON h.household_id = r.household_id AND r.resident_id = 
(SELECT MIN(r3.resident_id) FROM residents r3 WHERE r3.household_id = h.household_id)
ORDER BY h.household_id
```

**Benefits:**
- Properly concatenates first_name, middle_name, and last_name from residents table
- Uses COALESCE to handle NULL middle names
- Selects the first resident (MIN resident_id) as household head
- Uses subquery for accurate member count
- Eliminates dependency on stored head name in households table

### 2. HouseholdPanel.java - openHouseholdDialog() Method
**Location:** Line ~183-199

**Changes:**
- Updated SELECT query to join with residents table and get head_name dynamically
- Made head name field read-only (not editable by user)
- Added tooltip: "Head name is automatically set from the first household member"
- Removed head_fullname from INSERT and UPDATE queries
- Updated INSERT to only include: family_no, address, income
- Updated UPDATE to only include: family_no, address, income

**Previous INSERT:**
```sql
INSERT INTO households (family_no, head_fullname, address, income) VALUES (?, ?, ?, ?)
```

**New INSERT:**
```sql
INSERT INTO households (family_no, address, income) VALUES (?, ?, ?)
```

### 3. HouseholdPanel.java - openMemberDialog() Method
**Location:** Line ~437-450

**Changes:**
- Removed the UPDATE households SET head_fullname query
- Head name is now automatically retrieved via JOIN, no need to update households table
- Added comment: "No need to update households table - head name is now retrieved via JOIN from residents"

**Removed Code:**
```java
if (chkIsHead.isSelected() || finalIsFirstMember) {
    String fullName = txtFirst.getText().trim() + " " + txtLast.getText().trim();
    try (Connection conn = DbConnection.getConnection()) {
        PreparedStatement ps = conn.prepareStatement("UPDATE households SET head_fullname = ? WHERE household_id = ?");
        ps.setString(1, fullName);
        ps.setInt(2, householdId);
        ps.executeUpdate();
    }
}
```

## Database Schema Notes

### Current households table (assumed):
- household_id (INT, PRIMARY KEY)
- family_no (INT)
- address (VARCHAR)
- income (DECIMAL)
- full_name or head_fullname (VARCHAR) - **No longer used for display, kept for backward compatibility**

### residents table:
- resident_id (INT, PRIMARY KEY)
- household_id (INT, FOREIGN KEY)
- first_name (VARCHAR)
- middle_name (VARCHAR)
- last_name (VARCHAR)
- birth_date (DATE)
- age (INT)
- gender (VARCHAR)
- contact_no (VARCHAR)
- email (VARCHAR)

## Benefits of This Implementation

1. **Single Source of Truth:** Resident names are stored only in the residents table
2. **Automatic Updates:** When a resident's name is updated, the household display updates automatically
3. **No Data Redundancy:** Eliminates the need to maintain duplicate name data
4. **Better Relational Design:** Follows proper database normalization principles
5. **Consistent Logic:** The first resident (lowest resident_id) is always the head
6. **Null Safety:** COALESCE handles cases where middle name might be null

## Testing Recommendations

1. Test loading households with and without residents
2. Test adding new households and then adding residents
3. Test editing resident names and verify household display updates
4. Test households with residents that have null middle names
5. Verify member count is accurate for each household

## Migration Notes

If you need to clean up the old head_fullname/full_name column from the households table:
```sql
ALTER TABLE households DROP COLUMN head_fullname;
-- or
ALTER TABLE households DROP COLUMN full_name;
```

**Warning:** Only do this after confirming all code has been updated and tested!

## Files Modified
1. `src/ui/HouseholdPanel.java` - All household-related queries and UI

## Files Verified (No Changes Needed)
1. `src/ui/SeniorPanel.java` - Already uses proper CONCAT for resident names
2. `src/model/HouseholdModel.java` - Still references full_name (for backward compatibility)
3. `src/model/ResidentModel.java` - No changes needed

## Compilation Status
✅ No compilation errors
✅ All syntax validated
✅ Ready for testing
