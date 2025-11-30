# Validation Flow - Visual Guide

## 🎯 How Validation Works

### Before Save:
```
┌────────────────────────────────────┐
│  Add Household                     │
├────────────────────────────────────┤
│  Family No: [100     ] ✓           │
│  Address:   [Main St ] ✓           │
│  Income:    [25000   ] ✓           │
│                                    │
│  [Save] [Cancel]                   │
└────────────────────────────────────┘
         ↓ Click Save
         ↓
    Validation
         ↓
    ✅ All OK
         ↓
  Insert to DB
         ↓
   Show Success
```

### With Errors:
```
┌────────────────────────────────────┐
│  Add Household                     │
├────────────────────────────────────┤
│  Family No: [        ] ❌ Empty!   │
│  Address:   [Main St ] ✓           │
│  Income:    [25000   ] ✓           │
│                                    │
│  [Save] [Cancel]                   │
└────────────────────────────────────┘
         ↓ Click Save
         ↓
    Validation
         ↓
    ❌ Error!
         ↓
  Show Error Message
         ↓
  Focus on Family No
         ↓
  NO Database Insert
```

---

## 📋 Validation Checks

### Household Validation:
```
Check 1: Family No
├─ Is empty? ────────────→ ❌ Error
├─ Not a number? ────────→ ❌ Error  
├─ Negative or zero? ────→ ❌ Error
└─ All OK ───────────────→ ✅ Continue

Check 2: Address
├─ Is empty? ────────────→ ❌ Error
└─ Has value ────────────→ ✅ Continue

Check 3: Income
├─ Is empty? ────────────→ ❌ Error
├─ Not a number? ────────→ ❌ Error
├─ Negative? ────────────→ ❌ Error
└─ All OK ───────────────→ ✅ Continue

All Checks Pass ─────────→ ✅ Insert to Database
```

### Member Validation:
```
Check 1: First Name
├─ Is empty? ────────────→ ❌ Error
└─ Has value ────────────→ ✅ Continue

Check 2: Last Name
├─ Is empty? ────────────→ ❌ Error
└─ Has value ────────────→ ✅ Continue

Check 3: Birthdate
├─ Is null? ─────────────→ ❌ Error
├─ Is future? ───────────→ ❌ Error
└─ Valid past date ──────→ ✅ Continue

Check 4: Gender
├─ Not selected? ────────→ ❌ Error
└─ Selected ─────────────→ ✅ Continue

Check 5: Email (if provided)
├─ Invalid format? ──────→ ❌ Error
└─ Valid format ─────────→ ✅ Continue

All Checks Pass ─────────→ ✅ Insert to Database
```

---

## 🎨 Error Message Examples

### Empty Field:
```
┌─────────────────────────────────────┐
│             Error                   │
├─────────────────────────────────────┤
│  Please fill in the required field: │
│  Family No                          │
│                                     │
│  This field cannot be empty.        │
│                                     │
│            [OK]                     │
└─────────────────────────────────────┘
```

### Invalid Format:
```
┌─────────────────────────────────────┐
│             Error                   │
├─────────────────────────────────────┤
│  Invalid format in field: Family No │
│  Expected format:                   │
│  positive integer (e.g., 1, 100)    │
│                                     │
│  Please correct and try again.      │
│                                     │
│            [OK]                     │
└─────────────────────────────────────┘
```

### Success:
```
┌─────────────────────────────────────┐
│            Success                  │
├─────────────────────────────────────┤
│  Household added successfully!      │
│                                     │
│  Next step:                         │
│  Click 'Manage Members' to add      │
│  household members.                 │
│                                     │
│            [OK]                     │
└─────────────────────────────────────┘
```

---

## 📊 Logging Examples

### Success in user.log:
```
[2025-11-30 22:45:10] USER: admin | ACTION: CREATE_HOUSEHOLD | DETAILS: ID: 17 | Family No: 102, Address: Oak Street
```

### Error in error.log:
```
[2025-11-30 22:45:30] ERROR: User: admin | Action: Household validation | Error: Invalid Family No format: xyz
Exception: java.lang.NumberFormatException
Message: For input string: "xyz"
Stack Trace:
java.lang.NumberFormatException: For input string: "xyz"
    at java.lang.Integer.parseInt(Integer.java:652)
    at ui.HouseholdPanel.lambda$openHouseholdDialog$2(HouseholdPanel.java:235)
    ...
```

---

## 🧪 Test Scenarios

### Scenario 1: Empty Required Field
```
Input:  Family No = ""
        Address = "Main St"
        Income = "25000"

Action: Click Save

Result: ❌ Error shown
        "Please fill in required field: Family No"
        Focus on Family No
        NO database insert
```

### Scenario 2: Invalid Number
```
Input:  Family No = "abc"
        Address = "Main St"
        Income = "25000"

Action: Click Save

Result: ❌ Error shown
        "Invalid format in field: Family No"
        Focus on Family No
        Logged to error.log
        NO database insert
```

### Scenario 3: Negative Income
```
Input:  Family No = "100"
        Address = "Main St"
        Income = "-5000"

Action: Click Save

Result: ❌ Error shown
        "Income cannot be negative"
        Focus on Income
        NO database insert
```

### Scenario 4: All Valid
```
Input:  Family No = "100"
        Address = "Main St"
        Income = "25000"

Action: Click Save

Result: ✅ Success shown
        Record inserted to database
        Logged to user.log
        Dialog closes
        Table refreshes
```

---

## 🎯 Quick Reference

### Required Fields by Form:

**Household:**
- Family No ✓
- Address ✓
- Income ✓

**Member:**
- First Name ✓
- Last Name ✓
- Birthdate ✓
- Gender ✓

### Validation Types:

**Empty Check:**
```java
if (field.isEmpty()) {
    ErrorHandler.showValidationError(...);
    return;
}
```

**Number Check:**
```java
try {
    int value = Integer.parseInt(field);
} catch (NumberFormatException e) {
    ErrorHandler.showFormatError(...);
    return;
}
```

**Date Check:**
```java
if (date.after(new Date())) {
    ErrorHandler.showError(...);
    return;
}
```

---

## ✅ Benefits

| Before | After |
|--------|-------|
| Can save empty fields | ❌ Cannot save empty |
| Can save invalid data | ❌ Cannot save invalid |
| Confusing errors | ✅ Clear messages |
| No logging | ✅ Full logging |
| Bad data quality | ✅ High quality data |

---

**Status:** ✅ COMPLETE
**Testing:** Ready
**Production:** Ready
