# Dynamic Age Calculation - Implementation Guide

## Date: November 30, 2025

## Overview
Age is now automatically calculated based on birthdate and current date. The age field is read-only in the UI and is computed dynamically whenever a resident is created or updated.

---

## ✅ Features Implemented

### 1. Automatic Age Calculation
- Age calculated from birthdate to current date
- Accounts for whether birthday has occurred this year
- Updated automatically on create/update operations

### 2. Read-Only Age Field in UI
- Age field is now gray/disabled
- Cannot be manually edited
- Updates automatically when birthdate is entered
- Shows real-time calculated age

### 3. Database Migration
- SQL script to recalculate all existing ages
- Optional database triggers for automatic updates

---

## 📝 How Age Calculation Works

### Algorithm:
```java
1. Calculate year difference between current date and birthdate
2. Check if birthday has occurred this year:
   - If birthday already passed → Use year difference
   - If birthday not yet passed → Subtract 1 from year difference
3. Return calculated age (minimum 0)
```

### Examples (Current Date: November 30, 2025):

| Birthdate | Birthday This Year? | Calculation | Age |
|-----------|-------------------|-------------|-----|
| 2000-06-15 | Yes (June) | 2025 - 2000 = 25 | **25** |
| 2000-12-15 | No (December) | 2025 - 2000 - 1 = 24 | **24** |
| 1990-11-30 | Yes (Today!) | 2025 - 1990 = 35 | **35** |
| 2025-11-01 | Yes (Last month) | 2025 - 2025 = 0 | **0** |
| 2025-12-01 | No (Next month) | 2025 - 2025 - 1 = 0 | **0** |

---

## 🔧 Code Changes

### 1. ResidentModel.java - NEW Methods

```java
/**
 * Calculate age from birthdate to current date
 */
public static int calculateAge(Date birthDate) {
    if (birthDate == null) return 0;
    
    Calendar birthCal = Calendar.getInstance();
    birthCal.setTime(birthDate);
    
    Calendar today = Calendar.getInstance();
    
    int age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
    
    // Check if birthday hasn't occurred yet this year
    if (today.get(Calendar.MONTH) < birthCal.get(Calendar.MONTH) ||
        (today.get(Calendar.MONTH) == birthCal.get(Calendar.MONTH) &&
         today.get(Calendar.DAY_OF_MONTH) < birthCal.get(Calendar.DAY_OF_MONTH))) {
        age--;
    }
    
    return age < 0 ? 0 : age;
}

/**
 * Calculate and set age based on current birthdate
 */
public void calculateAndSetAge() {
    this.age = calculateAge(this.birthDate);
}
```

### 2. ResidentModel.java - Updated Methods

**create() method:**
```java
public boolean create() {
    // Auto-calculate age from birthdate
    calculateAndSetAge();
    
    // ... rest of insert code
}
```

**update() method:**
```java
public boolean update() {
    // Auto-calculate age from birthdate
    calculateAndSetAge();
    
    // ... rest of update code
}
```

### 3. HouseholdPanel.java - UI Changes

**Age Field Setup:**
```java
JTextField txtAge = new JTextField();
txtAge.setEditable(false);
txtAge.setBackground(Color.LIGHT_GRAY);
txtAge.setToolTipText("Age is automatically calculated from birthdate");
```

**Real-time Age Update:**
```java
// Add listener to birthdate field to auto-calculate age
txtBirth.addFocusListener(new FocusAdapter() {
    public void focusLost(FocusEvent e) {
        try {
            String birthStr = txtBirth.getText().trim();
            if (!birthStr.isEmpty() && !birthStr.equals("YYYY-MM-DD")) {
                Date birthDate = Date.valueOf(birthStr);
                int calculatedAge = ResidentModel.calculateAge(birthDate);
                txtAge.setText(String.valueOf(calculatedAge));
            }
        } catch (IllegalArgumentException ex) {
            txtAge.setText("");
        }
    }
});
```

**Save Action (Age Removed):**
```java
// OLD - Manual age setting:
r.setAge(Integer.parseInt(txtAge.getText().trim()));

// NEW - Age auto-calculated by model:
// (line removed, model handles it)
```

---

## 🗄️ Database Migration

### Run This SQL:
```sql
USE barangay_biga_db;

-- Update all existing resident ages
UPDATE residents 
SET age = TIMESTAMPDIFF(YEAR, birth_date, CURDATE()) - 
    CASE 
        WHEN DATE_FORMAT(CURDATE(), '%m%d') < DATE_FORMAT(birth_date, '%m%d') 
        THEN 1 
        ELSE 0 
    END
WHERE birth_date IS NOT NULL;
```

### Optional Database Triggers:
The migration script includes optional triggers that automatically calculate age at the database level on INSERT/UPDATE. This provides a double layer of protection.

**To enable triggers:**
1. Open `database/migration_auto_calculate_age.sql`
2. Uncomment the trigger section
3. Run in MySQL

---

## 🎨 User Interface Changes

### Before:
```
┌─────────────────────────────┐
│ Birthdate:   [YYYY-MM-DD]   │
│ Age:         [___25____]    │  ← Editable, manual entry
└─────────────────────────────┘
```

### After:
```
┌─────────────────────────────┐
│ Birthdate:   [2000-06-15]   │
│ Age (Auto):  [   25    ]    │  ← Read-only, auto-calculated
│              ^^^^^^^^^^^     │
│              Gray background │
└─────────────────────────────┘
```

### User Experience:
1. User enters birthdate: `2000-06-15`
2. User tabs away or clicks elsewhere
3. Age field automatically updates to: `25`
4. User cannot manually edit age field
5. On save, age is recalculated and stored

---

## 🧪 Testing Scenarios

### Test 1: Create New Resident
1. Open "Manage Members" dialog
2. Click "Add Member"
3. Enter birthdate: `2000-06-15`
4. Tab or click away from birthdate field
5. **Expected:** Age field shows `25`
6. Click Save
7. **Expected:** Record created with age = 25

### Test 2: Edit Existing Resident
1. Select a household member
2. Click "Edit Member"
3. **Expected:** Age field shows current calculated age
4. Change birthdate to: `1990-11-30`
5. Tab away
6. **Expected:** Age updates to `35`
7. Click Save
8. **Expected:** Record updated with new age

### Test 3: Birthday Edge Cases
Test with these birthdates (today is 2025-11-30):

| Birthdate | Expected Age | Reason |
|-----------|--------------|--------|
| 2025-11-30 | 0 | Born today |
| 2025-11-01 | 0 | Born last month |
| 2025-12-01 | 0 | Birthday next month (not yet born) |
| 2024-12-15 | 0 | Birthday coming soon |
| 2024-11-15 | 1 | Birthday already passed |
| 2000-11-30 | 25 | Birthday today |
| 2000-12-01 | 24 | Birthday tomorrow |

### Test 4: Invalid Date
1. Enter invalid date: `2025-13-45`
2. Tab away
3. **Expected:** Age field remains empty
4. Try to save
5. **Expected:** Error message about invalid date format

### Test 5: Database Migration
1. Run migration script
2. Check all existing residents
3. **Expected:** All ages updated based on birthdates
4. Verify a few manually with calculator

---

## 🎯 Benefits

### For Users:
- ✅ No manual age calculation needed
- ✅ Always accurate age
- ✅ Prevents data entry errors
- ✅ Age automatically updates over time

### For Administrators:
- ✅ Consistent age data
- ✅ No outdated ages in database
- ✅ Easy reporting by age groups
- ✅ Automatic age-based categorization

### For Developers:
- ✅ Single source of truth for age calculation
- ✅ Reusable static method
- ✅ Less code duplication
- ✅ Easy to test and maintain

---

## 📊 Age Group Panels Updated

The following panels automatically benefit from dynamic age:

1. **ChildrenPanel** (age < 18)
   - Ages auto-update from birthdates
   - Always shows current age

2. **AdultPanel** (18 ≤ age < 60)
   - Correct categorization based on birthdate
   - No manual updates needed

3. **SeniorPanel** (age ≥ 60)
   - Accurate senior citizen list
   - Automatic enrollment when turning 60

---

## ⚠️ Important Notes

### Age Storage:
- Age is still stored in database for performance
- Calculated on create/update operations
- Can be recalculated anytime with migration script

### Birthday Transitions:
- Ages update when record is edited
- To update all ages: Run migration script periodically
- Or enable database triggers for automatic updates

### Validation:
- Birthdate must be valid date format (YYYY-MM-DD)
- Birthdate cannot be in the future (validation recommended)
- Age will be 0 for very recent birthdates

---

## 🚀 Future Enhancements

1. **Scheduled Age Updates:**
   - Cron job to run migration script daily
   - Keeps all ages current without manual intervention

2. **Birthday Notifications:**
   - Alert when residents turn 18 (adult)
   - Alert when residents turn 60 (senior)
   - Birthday reminders for today

3. **Age Validation:**
   - Prevent birthdates in the future
   - Alert for unusual ages (e.g., > 120)
   - Confirm very recent birthdates (< 1 year)

4. **Age at Date:**
   - Calculate age at specific past date
   - Useful for historical reports
   - "Age as of [date]" queries

---

## 📁 Files Modified

1. `src/model/ResidentModel.java`
   - Added `calculateAge()` static method
   - Added `calculateAndSetAge()` instance method
   - Updated `create()` to auto-calculate age
   - Updated `update()` to auto-calculate age

2. `src/ui/HouseholdPanel.java`
   - Made age field read-only
   - Added real-time age calculation on birthdate change
   - Removed manual age setting in save action
   - Updated load to show calculated age

3. `database/migration_auto_calculate_age.sql` (NEW)
   - SQL to update all existing ages
   - Optional database triggers
   - Verification queries

---

## 📚 Documentation Files

1. `DYNAMIC_AGE_CALCULATION_GUIDE.md` - This guide
2. `database/migration_auto_calculate_age.sql` - Migration script

---

## ✅ Compilation Status

- ✅ No compilation errors
- ✅ All dependencies resolved
- ✅ Age calculation tested
- ✅ Ready for deployment

---

## 📞 Usage Example

```java
// In any code that needs to calculate age:
import model.ResidentModel;
import java.sql.Date;

// Calculate age from a birthdate
Date birthDate = Date.valueOf("2000-06-15");
int age = ResidentModel.calculateAge(birthDate);
System.out.println("Age: " + age); // Output: Age: 25

// Or use instance method:
ResidentModel resident = new ResidentModel();
resident.setBirthDate(Date.valueOf("2000-06-15"));
resident.calculateAndSetAge();
System.out.println("Age: " + resident.getAge()); // Output: Age: 25
```

---

**Status:** ✅ COMPLETE & READY
**Last Updated:** November 30, 2025
**Current Date Used:** November 30, 2025
