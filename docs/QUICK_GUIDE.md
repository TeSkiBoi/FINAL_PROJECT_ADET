# 🎯 QUICK REFERENCE - Model-View Architecture

## ✅ What Changed

### Before (❌ BAD)
```java
// UI Panel with SQL queries
private void loadData() {
    try (Connection conn = DbConnection.getConnection()) {
        String sql = "SELECT * FROM table";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        // process...
    }
}
```

### After (✅ GOOD)
```java
// UI Panel using Model
private void loadData() {
    try {
        List<Map<String, Object>> data = MyModel.getAllData();
        // process...
    } catch (SQLException e) {
        ErrorHandler.showError(this, "loading data", e);
    }
}
```

---

## 📋 Available Models

| Model | Purpose |
|-------|---------|
| **ProjectModel** | barangay_projects operations |
| **FinancialModel** | financial_transactions operations |
| **DashboardModel** | Statistics and counts |
| **ResidentModel** | Residents CRUD + age filtering |
| **HouseholdModel** | Households CRUD + members |
| **BlotterModel** | Incident reports |
| **ActivityLogModel** | User activity logs |
| **OfficialModel** | Barangay officials |
| **UserModel** | User management |
| **RoleModel** | Role management |
| **ChildrenModel** | Children-specific operations |
| **AdultModel** | Adult-specific operations |
| **SeniorModel** | Senior-specific operations |

---

## 🔧 Common Operations

### Get All Records
```java
List<Map<String, Object>> data = MyModel.getAllData();
```

### Search/Filter
```java
List<Map<String, Object>> results = MyModel.search(keyword);
```

### Get By ID
```java
Map<String, Object> record = MyModel.getById(id);
```

### Add Record
```java
boolean success = MyModel.addRecord(field1, field2, ...);
```

### Update Record
```java
boolean success = MyModel.updateRecord(id, field1, field2, ...);
```

### Delete Record
```java
boolean success = MyModel.deleteRecord(id);
```

---

## 🚫 RULES

1. **NEVER** write SQL in UI classes
2. **NEVER** import `DbConnection` in UI classes
3. **NEVER** use `PreparedStatement` in UI classes
4. **ALWAYS** use Model classes for data access
5. **ALWAYS** handle exceptions properly
6. **ALWAYS** log CRUD operations

---

## ✅ Checklist for New Features

When adding a new feature:

- [ ] Create/update Model class with method
- [ ] Implement SQL query in Model
- [ ] Use prepared statements
- [ ] Add error handling
- [ ] Add logging
- [ ] Call Model method from UI
- [ ] Handle exceptions in UI
- [ ] Show user-friendly error messages
- [ ] Test all CRUD operations

---

## 📞 Need Help?

Review these files:
- **CLEANUP_COMPLETE.md** - Full refactoring documentation
- **TASKS_COMPLETE.md** - Task completion summary
- **src/model/** - All model implementations
- **src/ui/OfficialsPanel.java** - Reference implementation

---

**Remember:** UI = Presentation, Model = Data Access
