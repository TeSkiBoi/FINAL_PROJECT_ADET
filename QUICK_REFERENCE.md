# QUICK REFERENCE GUIDE

## Staff vs Admin Access

### STAFF CAN:
✅ VIEW: Households, Residents, Children, Senior, Adults
✅ EDIT: Barangay Projects, Financial Management
✅ USE: Search and Sort on all accessible panels

### STAFF CANNOT:
❌ Edit any resident/household records
❌ Access Officials, Blotter, Users, Roles, Activity Logs
❌ Delete any demographic data

### ADMIN CAN:
✅ Everything Staff can do, PLUS:
✅ Edit Households and Members
✅ Manage Officials, Blotter, Users, Roles
✅ View Activity Logs

---

## How to Manage Residents

### OLD WAY (Removed):
❌ ResidentPanel > Add/Edit/Delete buttons

### NEW WAY:
1. Go to **Households Panel**
2. Select a household
3. Click **"👥 Manage Members"**
4. Modal opens showing all household members
5. Add/Edit/Delete members there

### First Member Rule:
- First member added = Automatically becomes household head
- Checkbox "Set as Household Head" is auto-checked and disabled
- You can change the head later by editing other members

---

## Search & Sort

### Search:
- Type in the search box at top of any panel
- Results filter automatically as you type
- Case-insensitive
- Searches all columns

### Sort:
- Click any column header to sort
- Click again to reverse order
- Hold Ctrl + Click to sort by multiple columns

---

## Color Scheme

- **Dark Brown** (#2C1B18) - Buttons, primary text
- **Light Peach** (#FFE8D6) - Backgrounds
- **Bright Orange** (#FF8C42) - Secondary elements
- **Deep Red** (#D94E4E) - Accents, logout button

---

## Dashboard Stats

**3x3 Grid showing:**
- Total Households, Residents, Projects
- Children (0-17), Adults (18-59), Seniors (60+)
- Active Projects, Total Users, Total Officials

All numbers update in real-time from database.

---

## Key Files Changed

✅ Login.java - Role routing
✅ Dashboard.java - Role menus + stats
✅ HouseholdPanel.java - Complete rewrite
✅ ResidentPanel.java - View-only
✅ Theme.java - Better colors
✅ All demographic panels - Search + Sort

## New Files

✅ ChildrenPanel.java
✅ SeniorPanel.java
✅ AdultPanel.java
✅ RolesPanel.java (Admin only)

---

## Testing Credentials

**Admin**: role_id = "1"
**Staff**: role_id = "2"

Make sure to test with both accounts!
