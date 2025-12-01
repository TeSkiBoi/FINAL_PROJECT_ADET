# Quick Reference - Error Handling & Search/Sort

## 🎯 User-Friendly Error Handler

### Basic Usage
```java
// In any panel, replace old error handling:

// ❌ OLD WAY:
catch (SQLException e) {
    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
}

// ✅ NEW WAY:
catch (SQLException e) {
    util.ErrorHandler.showError(this, "loading data", e);
}
```

### All Methods
```java
// Show error with exception
ErrorHandler.showError(parent, "action description", exception);

// Show simple error message
ErrorHandler.showError(parent, "Your error message");

// Show warning
ErrorHandler.showWarning(parent, "Warning message");

// Show success
ErrorHandler.showSuccess(parent, "Success message");

// Show confirmation
boolean yes = ErrorHandler.confirm(parent, "Are you sure?", "Confirm");

// Validation errors
ErrorHandler.showValidationError(parent, "Email");
ErrorHandler.showFormatError(parent, "Date", "YYYY-MM-DD");
```

---

## 🔍 Add Search to Any Panel

### Step 1: Add Fields
```java
private JTextField txtSearch;
private TableRowSorter<DefaultTableModel> sorter;
```

### Step 2: Create Search Box
```java
JLabel lblSearch = new JLabel("Search:");
txtSearch = new JTextField(30);
topPanel.add(lblSearch);
topPanel.add(txtSearch);
```

### Step 3: Add Sorter to Table
```java
sorter = new TableRowSorter<>(tableModel);
table.setRowSorter(sorter);
```

### Step 4: Add Search Method
```java
private void search() {
    String text = txtSearch.getText().trim();
    if (text.isEmpty()) {
        sorter.setRowFilter(null);
    } else {
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
    }
}
```

### Step 5: Add Listener
```java
txtSearch.getDocument().addDocumentListener(
    new javax.swing.event.DocumentListener() {
        public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
    }
);
```

---

## 📊 Error Message Translations

| User Sees | Technical Error |
|-----------|-----------------|
| "Cannot connect to the database" | Communications link failure |
| "This record already exists" | Duplicate entry |
| "Cannot delete - used by other records" | Foreign key constraint |
| "Database table is missing" | Table doesn't exist |
| "Please enter valid numbers" | NumberFormatException |
| "Access denied" | Access denied for user |

---

## ✅ Checklist for Each Panel

- [ ] Import `util.ErrorHandler`
- [ ] Add search box components
- [ ] Add TableRowSorter
- [ ] Implement search() method
- [ ] Add search listener
- [ ] Replace all error messages with ErrorHandler
- [ ] Test search functionality
- [ ] Test sort functionality
- [ ] Test error messages

---

## 🧪 Quick Test

1. **Test Search:** Type in search box → Table filters
2. **Test Sort:** Click column header → Data sorts
3. **Test Error:** Cause error → See friendly message

---

## 📁 Files

- **Error Handler:** `src/util/ErrorHandler.java`
- **Example Panel:** `src/ui/HouseholdPanel.java`
- **Full Guide:** `ERROR_HANDLING_SEARCH_SORT_GUIDE.md`

---

**Status:** ✅ Ready to Use
**Date:** November 30, 2025
