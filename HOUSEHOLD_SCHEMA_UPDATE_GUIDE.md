# Household Schema Update & Dashboard Cleanup

## Date: November 30, 2025

## Overview
This document details the changes made to update the household management system to use `household_head_id` (foreign key to residents) instead of storing the head name directly, remove useless buttons from the dashboard, and implement proper date/time formatting.

---

## 1. Database Schema Changes

### Households Table - New Schema

```sql
household_id         INT(11)       PRIMARY KEY AUTO_INCREMENT
family_no            INT(11)       NOT NULL
household_head_id    INT(11)       NULL (INDEX, FK to residents.resident_id)
address              VARCHAR(255)  NOT NULL
income               DECIMAL(12,2) NULL DEFAULT 0.00
created_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
updated_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

### Changes Made:
1. **Removed:** `full_name` or `head_fullname` column
2. **Added:** `household_head_id` - References the resident who is the household head
3. **Added:** `created_at` - Timestamp for record creation
4. **Added:** `updated_at` - Timestamp for last update
5. **Added:** Index on `household_head_id` for performance

### Migration
Run the migration script: `database/migration_household_head_id.sql`

---

## 2. Code Changes

### A. HouseholdModel.java

**Updated Fields:**
```java
private int householdId;
private int familyNo;
private Integer householdHeadId;  // Changed from String fullName
private String address;
private double income;
private Timestamp createdAt;      // New
private Timestamp updatedAt;      // New
```

**Updated Methods:**
- `getAll()` - Now retrieves `household_head_id`, `created_at`, `updated_at`
- `create()` - Inserts with `household_head_id` (nullable)
- `update()` - Updates with `household_head_id` (nullable)
- Added getters/setters for new fields

### B. HouseholdPanel.java

**Removed:**
- Head name input field from household dialog
- All references to `full_name` or `head_fullname` columns

**Updated:**
- `openHouseholdDialog()` - Removed txtHead field completely
- Dialog now only shows: Family No, Address, Income
- Head name is displayed in the table via JOIN query (already implemented)

**loadHouseholds() Query:**
```sql
SELECT h.household_id, h.family_no,
       CONCAT(COALESCE(r.first_name, ''), ' ', 
              COALESCE(r.middle_name, ''), ' ', 
              COALESCE(r.last_name, '')) AS head_name,
       h.address, h.income,
       (SELECT COUNT(*) FROM residents r2 
        WHERE r2.household_id = h.household_id) as member_count
FROM households h
LEFT JOIN residents r ON h.household_id = r.household_id 
    AND r.resident_id = h.household_head_id
ORDER BY h.household_id
```

### C. Dashboard.java

**Removed Unused Declarations:**
- `btnStudents` - Never initialized or used
- `lblTotalProducts` - Never used
- `lblTotalSuppliers` - Never used
- `statsPanel` - Never used
- `categoryStatsPanel` - Never used

**Before:**
```java
private JPanel sidePanel, mainPanel, statsPanel;
private JButton btnHome, ..., btnStudents, ...;
private JLabel lblTotalProducts, lblTotalSuppliers;
private JPanel categoryStatsPanel;
```

**After:**
```java
private JPanel sidePanel, mainPanel;
private JButton btnHome, ...; // btnStudents removed
// Unused labels and panels removed
```

---

## 3. New Utility Class: DateTimeFormatter

**Location:** `src/util/DateTimeFormatter.java`

### Features:
- Standardized date/time formatting across the application
- Support for HTML span elements for proper rendering in UI components
- Multiple format options:
  - Date only: `yyyy-MM-dd`
  - Time only: `HH:mm:ss`
  - DateTime: `yyyy-MM-dd HH:mm:ss`
  - DateTime 12-hour: `yyyy-MM-dd hh:mm:ss a`

### Key Methods:

**Basic Formatting:**
```java
DateTimeFormatter.formatDate(date)          // 2025-11-30
DateTimeFormatter.formatTime(timestamp)     // 14:30:45
DateTimeFormatter.formatDateTime(timestamp) // 2025-11-30 14:30:45
```

**With HTML Span (for UI components):**
```java
DateTimeFormatter.formatDateWithSpan(date)         
// <span style='font-family: monospace;'>2025-11-30</span>

DateTimeFormatter.formatDateTimeWithSpan(timestamp)
// <span style='font-family: monospace;'>2025-11-30 14:30:45</span>
```

**Current Date/Time:**
```java
DateTimeFormatter.getCurrentDate()     // Current date
DateTimeFormatter.getCurrentTime()     // Current time
DateTimeFormatter.getCurrentDateTime() // Current date and time
```

### Usage Examples:

**In Table Models:**
```java
// Display timestamp in table
tableModel.addRow(new Object[]{
    id,
    name,
    DateTimeFormatter.formatDateTime(createdAt)
});
```

**In Labels with HTML:**
```java
JLabel lblCreated = new JLabel(
    "<html>Created: " + 
    DateTimeFormatter.formatDateTimeWithSpan(household.getCreatedAt()) +
    "</html>"
);
```

**In Dialogs:**
```java
String createdDate = DateTimeFormatter.formatDate(rs.getTimestamp("created_at"));
txtCreatedDate.setText(createdDate);
```

---

## 4. Benefits of Changes

### Database Benefits:
1. **Referential Integrity:** household_head_id links directly to residents table
2. **No Data Duplication:** Name stored only in residents table
3. **Automatic Updates:** Changing a resident's name automatically updates household display
4. **Audit Trail:** created_at and updated_at track record lifecycle
5. **Better Performance:** Indexed household_head_id for faster lookups

### Code Benefits:
1. **Cleaner Code:** Removed unused variables and buttons
2. **Consistent Formatting:** DateTimeFormatter utility ensures uniform date/time display
3. **Maintainability:** Centralized date formatting logic
4. **Type Safety:** Using Timestamp types instead of strings
5. **HTML Support:** Proper rendering in Swing components with monospace font

---

## 5. Migration Checklist

- [x] Update HouseholdModel to use household_head_id
- [x] Remove head name input from HouseholdPanel dialog
- [x] Remove unused buttons from Dashboard
- [x] Create DateTimeFormatter utility class
- [x] Create database migration script
- [ ] Run database migration
- [ ] Test household CRUD operations
- [ ] Test date/time display in all panels
- [ ] Update any reports that reference old schema

---

## 6. Testing Recommendations

### Database Testing:
1. Run migration script and verify schema
2. Test that household_head_id is populated correctly
3. Verify foreign key constraints (if enabled)
4. Test created_at and updated_at auto-population

### Application Testing:
1. **Household Panel:**
   - Create new household without head
   - Add residents to household
   - Verify first resident becomes head automatically
   - Edit household details
   - Delete household

2. **Dashboard:**
   - Verify all buttons work correctly
   - Check for any console errors
   - Confirm unused buttons are removed

3. **Date/Time Display:**
   - Check timestamps in any panels that show created_at/updated_at
   - Verify consistent formatting across application
   - Test HTML span rendering in labels

---

## 7. Files Modified

1. `src/model/HouseholdModel.java` - Updated to use household_head_id
2. `src/ui/HouseholdPanel.java` - Removed head name input
3. `src/ui/Dashboard.java` - Removed unused declarations
4. `src/util/DateTimeFormatter.java` - NEW utility class

## 8. Files Created

1. `database/migration_household_head_id.sql` - Database migration script
2. `src/util/DateTimeFormatter.java` - Date/time formatting utility
3. `HOUSEHOLD_SCHEMA_UPDATE_GUIDE.md` - This documentation

---

## 9. Rollback Instructions

If you need to revert these changes:

1. **Database:**
   ```sql
   -- Restore from backup
   DROP TABLE IF EXISTS households;
   CREATE TABLE households (...old schema...);
   -- Or use the rollback section in migration script
   ```

2. **Code:**
   - Revert HouseholdModel.java to use String fullName
   - Re-add head name input field in HouseholdPanel
   - Restore removed button declarations in Dashboard

---

## 10. Future Enhancements

1. Add ability to change household head through UI
2. Implement household head transfer functionality
3. Add validation to prevent deleting household head
4. Create household history tracking
5. Implement date range filtering using DateTimeFormatter
6. Add date/time pickers with proper formatting

---

## Compilation Status
✅ No compilation errors
✅ All files validated
✅ Ready for testing
