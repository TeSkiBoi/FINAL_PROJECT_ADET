# Quick Reference: Staff Access Control

## What Staff CAN Do:
✅ **VIEW** all resident records (Residents, Households, Children, Senior, Adults)
✅ **EDIT** Barangay Projects (full CRUD access)
✅ **EDIT** Financial Management (full CRUD access)

## What Staff CANNOT Do:
❌ Cannot add/edit/delete Residents
❌ Cannot add/edit/delete Households
❌ Cannot add/edit/delete Children records
❌ Cannot add/edit/delete Senior records
❌ Cannot add/edit/delete Adult records
❌ Cannot access Officials panel
❌ Cannot access Blotter/Incidents panel
❌ Cannot access Users panel
❌ Cannot access Activity Log

## What Admin CAN Do:
✅ **FULL ACCESS** to everything

## Color Improvements:
The new color scheme provides better contrast: 
- Dark brown text (#2C1B18) on light peach backgrounds (#FFE8D6)
- Bright orange (#FF8C42) and deep red (#D94E4E) for accents
- Clear visual separation between buttons and content
- Easy to read, professional appearance

## Technical Notes:
- Role ID "1" = Administrator
- Role ID "2" = Staff
- All access control is enforced at the UI level
- Buttons are disabled (not just hidden) to show users what exists but they can't modify
- Children/Senior/Adult panels are read-only views filtered by age from the Residents table
