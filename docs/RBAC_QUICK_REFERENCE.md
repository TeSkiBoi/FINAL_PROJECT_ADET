# RBAC Quick Reference Guide

## User Credentials for Testing

### Administrator Account
- **Username**: admin
- **Role**: Administrator (role_id = 1)
- **Access**: Full access to all modules

### Staff Account
- **Username**: staff
- **Role**: Staff (role_id = 2)
- **Access**: Limited as defined below

---

## Staff Access Summary

### ✅ CAN ACCESS (View Only)
- **Households** - Can view but cannot add/edit/delete
- **Residents** - Can view all residents
- **Children** - Can view children (under 18)
- **Adults** - Can view adults (18-59)
- **Seniors** - Can view seniors (60+)

### ✅ CAN EDIT (Full CRUD)
- **Barangay Projects** - Can add/edit/delete projects
- **Financial** - Can add/edit/delete transactions

### ❌ NO ACCESS (Hidden from Menu)
- **Barangay Officials** - Admin only
- **Blotter/Incidents** - Admin only
- **Users** - Admin only
- **Roles** - Admin only
- **Activity Log** - Admin only

---

## Admin Access Summary

### ✅ FULL ACCESS TO EVERYTHING
- All records modules (with full CRUD)
- All features modules (with full CRUD)
- All administration modules (with full CRUD)

---

## Visual Indicators

### Dashboard Menu Labels
- **Records (View Only)** - Staff sees this label
- **Features (Editable)** - Staff sees this label
- **Records**, **Features**, **Admin** - Admin sees these labels

### Button States
- **Disabled Buttons** - Grayed out for staff on view-only panels
- **Enabled Buttons** - Active for authorized operations

---

## Panel-Specific Behavior

### HouseholdPanel (Staff: View Only)
```
Buttons Disabled for Staff:
- ❌ + Add Household
- ❌ ✏ Edit Household
- ❌ 👥 Manage Members
- ❌ 🗑 Delete Household

Buttons Enabled for Staff:
- ✅ 🔄 Refresh
- ✅ Search functionality
```

### ProjectsPanel (Staff: Full Access)
```
All Buttons Enabled for Staff:
- ✅ + Add Project
- ✅ ✏ Edit Project
- ✅ 🗑 Delete Project
- ✅ 🔄 Refresh
- ✅ Search functionality
```

### FinancialPanel (Staff: Full Access)
```
All Buttons Enabled for Staff:
- ✅ ➕ Add Transaction
- ✅ ✏️ Edit Transaction
- ✅ 🗑️ Delete Transaction
- ✅ 🔄 Refresh
- ✅ Search and Filter functionality
```

---

## Code Implementation Patterns

### Checking User Role
```java
User current = SessionManager.getInstance().getCurrentUser();
boolean isStaff = (current != null && "2".equals(current.getRoleId()));
boolean isAdmin = (current != null && "1".equals(current.getRoleId()));
```

### Disabling Buttons for Staff
```java
if (isStaff) {
    btnAdd.setEnabled(false);
    btnEdit.setEnabled(false);
    btnDelete.setEnabled(false);
}
```

### Hiding Menu Items
```java
// In Dashboard.java
if (isAdmin) {
    // Add all menu items
    sidePanel.add(btnUsers);
    sidePanel.add(btnRoles);
    sidePanel.add(btnLogs);
} else if (isStaff) {
    // Only add authorized items
    // btnUsers, btnRoles, btnLogs NOT added
}
```

---

## Database Schema Compliance

### Roles Table (As per schema)
```sql
CREATE TABLE `roles` (
  `role_id` int(11) NOT NULL,
  `role_name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

✅ **No description column**  
✅ **No permissions column**  
✅ **Simple and clean**

---

## Activity Logging

All CRUD operations are logged with:
- **User ID** - Who performed the action
- **Action Type** - CREATE, UPDATE, DELETE
- **Entity** - What was modified
- **Timestamp** - When it happened
- **IP Address** - Where it came from

Example:
```
Logger.logCRUDOperation("CREATE", "Barangay_Project", "12", "Project: New Community Center");
```

---

## Testing Steps

### Test Staff View-Only Access
1. Login as staff
2. Navigate to Households
3. Try to click "Add Household" - Should be disabled
4. Try to click "Edit Household" - Should be disabled
5. Try to click "Delete Household" - Should be disabled
6. Search and view should work normally

### Test Staff Edit Access
1. While logged in as staff
2. Navigate to Barangay Projects
3. Click "Add Project" - Should work
4. Fill form and save - Should succeed
5. Click "Edit Project" - Should work
6. Click "Delete Project" - Should work with confirmation

### Test Admin Full Access
1. Login as admin
2. Navigate to all modules - All should be accessible
3. Test CRUD on all modules - All should work
4. Verify activity log shows all operations

---

## Common Issues & Solutions

### Issue: Staff can still edit records
**Solution**: Check if role checking is implemented in panel constructor

### Issue: Menu items not hidden
**Solution**: Check Dashboard menu construction logic

### Issue: Buttons still enabled for staff
**Solution**: Verify `isStaff` boolean is set correctly and buttons are disabled

### Issue: Eclipse shows errors
**Solution**: Clean project (Project → Clean) and rebuild

---

## Files Modified Summary

### New Files
- `src/ui/ProjectsPanel.java`

### Modified Files
- `src/ui/Dashboard.java`
- `src/ui/HouseholdPanel.java`

### Documentation Files
- `docs/RBAC_IMPLEMENTATION.md`
- `docs/RBAC_QUICK_REFERENCE.md` (this file)

---

**Last Updated**: December 1, 2025  
**Status**: ✅ Implementation Complete
