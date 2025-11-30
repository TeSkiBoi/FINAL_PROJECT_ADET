# Logging System - Quick Reference

## 📝 How to Use the Logger

### Import the Logger
```java
import util.Logger;
```

### Log User Activity
```java
// With specific username
Logger.logUserActivity("admin", "CREATE_RECORD", "Created record ID: 123");

// With current logged-in user (auto-detects from SessionManager)
Logger.logUserActivity("VIEW_DASHBOARD", "Accessed main dashboard");
```

### Log Errors
```java
// Basic error
Logger.logError("Error message", exception);

// Error with context
Logger.logError("Save household", "Failed to save household data", exception);

// Database error
Logger.logDatabaseError("SELECT * FROM households", sqlException);
```

### Log CRUD Operations
```java
// CREATE
Logger.logCRUDOperation("CREATE", "Household", "5", "Family No: 100");

// READ (recordId can be null)
Logger.logCRUDOperation("READ", "Household", null, "Loaded 50 records");

// UPDATE
Logger.logCRUDOperation("UPDATE", "Household", "5", "Updated address");

// DELETE
Logger.logCRUDOperation("DELETE", "Household", "5", "Deleted with 3 members");
```

### Log Authentication
```java
// Success
Logger.logAuthenticationAttempt("admin", true, "127.0.0.1");

// Failure
Logger.logAuthenticationAttempt("hacker", false, "192.168.1.100");
```

### Log Info/Warning
```java
Logger.logInfo("Application started successfully");
Logger.logWarning("Configuration file not found, using defaults");
```

---

## 📊 Activity Log Panel Features

### Columns Displayed:
1. **Log ID** - Unique log identifier
2. **Username** - User who performed action (NEW!)
3. **User ID** - Database user ID
4. **Action** - What was done
5. **Time** - When it happened (formatted)
6. **IP Address** - Where it came from

### Features:
- **Search Box** - Filter logs by any text
- **Refresh Button** - Reload logs from database
- **Clear Old Logs** - Delete logs older than 30 days

---

## 📁 Log Files

### user.log
Contains all user activities:
- Login/logout
- CRUD operations
- Dashboard access
- General info messages

### error.log
Contains all errors:
- Exceptions with stack traces
- Database failures
- Failed login attempts
- Warning messages

---

## 🔍 Common Logging Patterns

### In CRUD Operations
```java
try {
    // Database operation
    ps.executeUpdate();
    
    // Log success
    Logger.logCRUDOperation("CREATE", "Entity", id, details);
    
} catch (SQLException e) {
    // Log error
    Logger.logError("Create entity", "Failed to create entity", e);
    // Show user error
}
```

### In Login Process
```java
if (loginSuccess) {
    Logger.logAuthenticationAttempt(username, true, ipAddress);
    // Continue...
} else {
    Logger.logAuthenticationAttempt(username, false, ipAddress);
    Logger.logWarning("Failed login for: " + username);
    // Show error...
}
```

### In Database Operations
```java
try {
    conn = DbConnection.getConnection();
    // Use connection...
} catch (SQLException e) {
    Logger.logDatabaseError("Connection attempt", e);
    // Handle error...
}
```

---

## ✅ Best Practices

1. **Always log CRUD operations** - Track all data changes
2. **Log authentication attempts** - Security monitoring
3. **Include context in errors** - Operation name + details
4. **Don't log sensitive data** - No passwords, credit cards, etc.
5. **Use appropriate log levels** - Info for normal, Error for problems
6. **Include IDs when possible** - Record ID, User ID, etc.

---

## 🚫 What NOT to Log

- ❌ Passwords (plain text or hashed)
- ❌ Credit card numbers
- ❌ Personal identification numbers
- ❌ API keys or tokens
- ❌ Excessive repeated messages (causes log spam)

---

## 🔧 Troubleshooting

### Problem: Logs directory not created
**Solution:** Logger automatically creates it on first use

### Problem: Can't write to log files
**Solution:** Check file permissions on logs/ directory

### Problem: Log files too large
**Solution:** Use "Clear Old Logs" button in Activity Log Panel

### Problem: Username shows as "Unknown"
**Solution:** User was deleted from database, logs remain for audit

---

## 📞 Quick Help

**View logs:** Dashboard → Activity Log
**Clear old logs:** Activity Log Panel → Clear Old Logs button
**Search logs:** Use search box in Activity Log Panel
**File location:** `FINAL_PROJECT_ADET/logs/`

---

**Status:** ✅ Ready to Use
**Last Updated:** November 30, 2025
