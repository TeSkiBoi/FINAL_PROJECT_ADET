# Dynamic Age Calculation - Visual Guide

## Before & After Comparison

### ❌ BEFORE (Manual Entry)
```
┌──────────────────────────────────────┐
│  Add/Edit Member                     │
├──────────────────────────────────────┤
│  First Name:   [John         ]       │
│  Last Name:    [Doe          ]       │
│  Birthdate:    [2000-06-15   ]       │
│  Age:          [25           ]  ← Manual entry, can be wrong!
│                 ^^^^^^^^^^^^         │
│                 User must calculate  │
└──────────────────────────────────────┘

Problems:
❌ User must calculate age manually
❌ Can enter wrong age (typos)
❌ Age gets outdated over time
❌ Inconsistent data
```

### ✅ AFTER (Auto-Calculated)
```
┌──────────────────────────────────────┐
│  Add/Edit Member                     │
├──────────────────────────────────────┤
│  First Name:   [John         ]       │
│  Last Name:    [Doe          ]       │
│  Birthdate:    [2000-06-15   ]       │
│  Age (Auto):   [   25        ]  ← Automatically calculated!
│                 ^^^^^^^^^^^^         │
│                 Read-only, gray      │
└──────────────────────────────────────┘

Benefits:
✅ Age calculated automatically
✅ Always accurate
✅ No human errors
✅ Updates in real-time
```

---

## How It Works - Step by Step

### Scenario: Adding a New Resident

```
Step 1: User opens Add Member dialog
┌──────────────────────────────────────┐
│  Birthdate:    [YYYY-MM-DD   ]       │
│  Age (Auto):   [             ]       │
└──────────────────────────────────────┘

Step 2: User enters birthdate
┌──────────────────────────────────────┐
│  Birthdate:    [2000-06-15   ]       │
│  Age (Auto):   [             ]       │
└──────────────────────────────────────┘
                      ↓
                [Tab or Click]
                      ↓
Step 3: Age automatically calculated
┌──────────────────────────────────────┐
│  Birthdate:    [2000-06-15   ]       │
│  Age (Auto):   [   25        ] ✨    │
└──────────────────────────────────────┘

Step 4: User clicks Save
         ↓
  Age = 25 stored in database
```

---

## Age Calculation Logic

### Today: November 30, 2025

```
Example 1: Birthday Already Passed
───────────────────────────────────
Birthdate: June 15, 2000
Today:     November 30, 2025

2025 - 2000 = 25
Birthday (June) < Today (November) ✓
Age = 25 ✅


Example 2: Birthday Coming Soon
───────────────────────────────────
Birthdate: December 15, 2000
Today:     November 30, 2025

2025 - 2000 = 25
Birthday (December) > Today (November) ✓
Age = 25 - 1 = 24 ✅


Example 3: Birthday Today!
───────────────────────────────────
Birthdate: November 30, 2000
Today:     November 30, 2025

2025 - 2000 = 25
Birthday (Nov 30) = Today (Nov 30) ✓
Age = 25 ✅
```

---

## Testing Examples

### Test 1: Adult (25 years old)
```
Input:  Birthdate = 2000-06-15
Output: Age = 25
Reason: Birthday passed (June < November)
```

### Test 2: Almost 25 (still 24)
```
Input:  Birthdate = 2000-12-15
Output: Age = 24
Reason: Birthday coming (December > November)
```

### Test 3: Senior Citizen (65 years old)
```
Input:  Birthdate = 1960-01-01
Output: Age = 65
Reason: Birthday passed (January < November)
```

### Test 4: Child (5 years old)
```
Input:  Birthdate = 2020-03-20
Output: Age = 5
Reason: Birthday passed (March < November)
```

### Test 5: Infant (0 years old)
```
Input:  Birthdate = 2025-06-15
Output: Age = 0
Reason: Born this year, less than 1 year old
```

---

## UI Indicators

### Visual Cues:
```
┌─────────────────────────────────────────┐
│  Age (Auto):  [   25        ]           │
│                ^^^^^^^^^^^^              │
│                     │                    │
│    ┌────────────────┴──────────────┐    │
│    │ Gray background = Read-only   │    │
│    │ (Auto) label = Calculated     │    │
│    │ Tooltip: "Auto-calculated..."  │    │
│    └───────────────────────────────┘    │
└─────────────────────────────────────────┘
```

### Tooltip Message:
> "Age is automatically calculated from birthdate"

---

## Error Handling

### Invalid Date Format
```
User enters: "2000-13-45"
         ↓
System response: Age field stays empty
         ↓
On Save: Error message about invalid date
```

### Future Date
```
User enters: "2030-01-01"
         ↓
Age calculated: 0 (or negative, shown as 0)
         ↓
(Consider adding validation to prevent future dates)
```

---

## Age Categories

### Automatic Categorization:

```
Age < 18      →  ChildrenPanel
     ↓
  Children
─────────────────

18 ≤ Age < 60 →  AdultPanel
     ↓
   Adults
─────────────────

Age ≥ 60      →  SeniorPanel
     ↓
  Seniors
```

All categories automatically updated based on calculated age!

---

## Database View

### Before (Static):
```sql
resident_id | first_name | birth_date  | age
──────────────────────────────────────────
1           | John       | 2000-06-15  | 25  ← Manual, may be wrong
2           | Jane       | 1990-03-20  | 34  ← May be outdated
```

### After (Dynamic):
```sql
resident_id | first_name | birth_date  | age
──────────────────────────────────────────
1           | John       | 2000-06-15  | 25  ← Auto-calculated on save
2           | Jane       | 1990-03-20  | 35  ← Updated automatically
```

---

## Migration Impact

### Update All Existing Ages:
```sql
Before Migration:
─────────────────
Outdated ages in database
Some wrong ages from typos

Run: migration_auto_calculate_age.sql
         ↓
After Migration:
─────────────────
All ages recalculated
All accurate as of today
```

---

## Quick Reference Card

### For Users:
1. Enter birthdate in format: YYYY-MM-DD
2. Tab away or click elsewhere
3. Age appears automatically
4. Click Save

### For Developers:
```java
// Calculate age
int age = ResidentModel.calculateAge(birthDate);

// Auto-calculate for resident
resident.calculateAndSetAge();
```

### For Admins:
```bash
# Update all ages in database
mysql -u root -p barangay_biga_db < migration_auto_calculate_age.sql
```

---

## Status: ✅ READY TO USE

**Implementation Date:** November 30, 2025
**Current Date Used:** November 30, 2025

All age calculations are based on the current implementation date.
