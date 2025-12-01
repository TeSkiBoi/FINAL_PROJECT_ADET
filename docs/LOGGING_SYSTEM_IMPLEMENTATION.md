# Logging System Implementation Guide

## Date: November 30, 2025

## Overview
Comprehensive logging system implemented with separate user activity logs (user.log) and error logs (error.log). Activity log panel now displays username for better tracking.

---

## 🎯 Features Implemented

### 1. ✅ Dual Log File System
- **user.log** - All user activities, logins, CRUD operations
- **error.log** - All errors, exceptions, warnings, database failures

### 2. ✅ Username Display in Activity Log Panel
- Activity log panel now shows username alongside user ID
- JOIN query with users table to retrieve username
- "Unknown" displayed for deleted or missing users

### 3. ✅ Comprehensive Error Logging
- Database connection errors
- SQL query failures
- Login attempts (success and failure)
- CRUD operations tracking
- Exception stack traces

### 4. ✅ Enhanced Activity Log Panel
- Search functionality
- Refresh button
- Clear old logs button (removes logs older than 30 days)
- Formatted timestamps using DateTimeFormatter
- Increased limit to 500 logs

---

## 📁 Files Created

### 1. util/Logger.java
Central logging utility class with the following methods:

```java
// User Activity Logging
Logger.logUserActivity(username, action, details)
Logger.logUserActivity(action, details) // Uses current logged-in user

// Error Logging
Logger.logError(errorMessage, exception)
Logger.logError(action, errorMessage, exception)
Logger.logDatabaseError(query, exception)

// Authentication Logging
Logger.logAuthenticationAttempt(username, success, ipAddress)

// CRUD Operation Logging
Logger.logCRUDOperation(operation, entity, recordId, details)
// Example: Logger.logCRUDOperation("CREATE", "Household", "123", "Family No: 5")

// Info/Warning Logging
Logger.logInfo(message)
Logger.logWarning(message)

// Utility
Logger.clearLog("user" or "error")
```

---

## 📄 Log File Locations

```
FINAL_PROJECT_ADET/
├── logs/
│   ├── user.log    ← User activities
│   └── error.log   ← Errors and warnings
```

The `logs/` directory is automatically created on first run.

---

## 📝 Log File Formats

### user.log Format
```
[2025-11-30 14:30:45] USER: admin | ACTION: LOGIN_SUCCESS | DETAILS: IP: 127.0.0.1
[2025-11-30 14:31:12] USER: admin | ACTION: CREATE_HOUSEHOLD | DETAILS: ID: 5 | Family No: 100
[2025-11-30 14:32:05] USER: staff1 | ACTION: UPDATE_HOUSEHOLD | DETAILS: ID: 5 | Family No: 100
[2025-11-30 14:33:20] INFO: Admin user accessed full dashboard
```

### error.log Format
```
[2025-11-30 14:35:10] ERROR: Database Connection | Failed to connect to jdbc:mysql://localhost:3306/barangay_biga_db
Exception: java.sql.SQLException
Message: Access denied for user 'root'@'localhost'
Stack Trace:
java.sql.SQLException: Access denied...
    at db.DbConnection.getConnection(DbConnection.java:25)
    ...

[2025-11-30 14:36:45] WARNING: Failed login attempt for username/email: hacker123
```

---

## 🔧 Code Changes Summary

### 1. util/Logger.java (NEW)
- Central logging utility
- Handles file I/O operations
- Formats log messages consistently
- Thread-safe file writing
- Automatic logs directory creation

### 2. ui/ActivityLogPanel.java (UPDATED)
**Before:**
- 5 columns: ID, User ID, Action, Time, IP
- 200 log limit
- No search or filtering
- No username display

**After:**
- 6 columns: Log ID, **Username**, User ID, Action, Time, IP Address
- 500 log limit
- Search functionality with filter
- Refresh button
- Clear old logs button (30+ days)
- JOIN query with users table
- Formatted timestamps with DateTimeFormatter

**New Query:**
```sql
SELECT ul.log_id, ul.user_id, u.username, ul.action, ul.log_time, ul.ip_address
FROM user_logs ul
LEFT JOIN users u ON ul.user_id = u.user_id
ORDER BY ul.log_time DESC LIMIT 500
```

### 3. model/UserModel.java (UPDATED)
**Enhanced logUserActivity method:**
- Logs to database (as before)
- **NEW:** Also writes to user.log file
- Retrieves username for better logging
- Error handling for file write failures

### 4. ui/HouseholdPanel.java (UPDATED)
Added logging to all CRUD operations:
- **CREATE:** Logs new household creation with details
- **UPDATE:** Logs household modifications
- **DELETE:** Logs household deletion with member count
- **ERROR:** All exceptions logged to error.log

### 5. ui/Login.java (UPDATED)
Added comprehensive login logging:
- Successful login attempts (user.log)
- Failed login attempts (user.log + warning)
- Dashboard access by role (user.log)
- Error conditions logged to error.log

### 6. db/DbConnection.java (UPDATED)
- Database connection failures logged to error.log
- Includes full exception stack trace
- Reduced console spam (removed success messages)

---

## 🎨 Activity Log Panel UI

```
┌─────────────────────────────────────────────────────────────┐
│ Search: [________________] [🔄 Refresh] [🗑 Clear Old Logs] │
├────┬──────────┬─────────┬──────────────┬──────────┬────────┤
│ ID │ Username │ User ID │    Action    │   Time   │   IP   │
├────┼──────────┼─────────┼──────────────┼──────────┼────────┤
│ 45 │  admin   │   1     │LOGIN_SUCCESS │2025-11-30│127.0.0.│
│ 44 │  staff1  │   2     │UPDATE_...    │2025-11-30│127.0.0.│
│ 43 │  admin   │   1     │CREATE_...    │2025-11-30│127.0.0.│
└────┴──────────┴─────────┴──────────────┴──────────┴────────┘
```

---

## 📊 Logging Examples

### Example 1: Successful Login
```java
// In Login.java
util.Logger.logAuthenticationAttempt("admin", true, "127.0.0.1");
```

**Result in user.log:**
```
[2025-11-30 14:30:45] USER: admin | ACTION: LOGIN_SUCCESS | DETAILS: IP: 127.0.0.1
```

**Result in database:**
```
INSERT INTO user_logs (user_id, action, ip_address) 
VALUES ('1', 'User logged in', '127.0.0.1')
```

### Example 2: Create Household
```java
// In HouseholdPanel.java
util.Logger.logCRUDOperation("CREATE", "Household", "5", 
    "Family No: 100, Address: Main St");
```

**Result in user.log:**
```
[2025-11-30 14:31:12] USER: admin | ACTION: CREATE_HOUSEHOLD | DETAILS: ID: 5 | Family No: 100, Address: Main St
```

### Example 3: Database Error
```java
// In HouseholdPanel.java
catch (SQLException e) {
    util.Logger.logError("Load households", 
        "Failed to load households from database", e);
}
```

**Result in error.log:**
```
[2025-11-30 14:35:10] ERROR: User: admin | Action: Load households | Error: Failed to load households from database
Exception: java.sql.SQLException
Message: Table 'barangay_biga_db.households' doesn't exist
Stack Trace:
java.sql.SQLException: Table 'barangay_biga_db.households' doesn't exist
    at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(...)
    at ui.HouseholdPanel.loadHouseholds(HouseholdPanel.java:155)
    ...
```

### Example 4: Failed Login
```java
// In Login.java
util.Logger.logAuthenticationAttempt("wronguser", false, "127.0.0.1");
util.Logger.logWarning("Failed login attempt for username/email: wronguser");
```

**Result in user.log:**
```
[2025-11-30 14:36:45] USER: wronguser | ACTION: LOGIN_FAILED | DETAILS: IP: 127.0.0.1
```

**Result in error.log:**
```
[2025-11-30 14:36:45] WARNING: Failed login attempt for username/email: wronguser
```

---

## 🧪 Testing Checklist

### User Activity Logging:
- [ ] Login successfully → Check user.log for LOGIN_SUCCESS
- [ ] Login with wrong credentials → Check user.log for LOGIN_FAILED
- [ ] Create household → Check user.log for CREATE_HOUSEHOLD
- [ ] Update household → Check user.log for UPDATE_HOUSEHOLD
- [ ] Delete household → Check user.log for DELETE_HOUSEHOLD
- [ ] Check Activity Log Panel shows username correctly

### Error Logging:
- [ ] Cause database connection error → Check error.log
- [ ] Try invalid SQL operation → Check error.log for stack trace
- [ ] Force exception in CRUD → Check error.log has details

### Activity Log Panel:
- [ ] Open Activity Log Panel
- [ ] Verify username column shows correctly
- [ ] Test search functionality
- [ ] Click Refresh button
- [ ] Click Clear Old Logs button
- [ ] Verify timestamps are formatted correctly

### Log File Creation:
- [ ] Verify logs/ directory is created
- [ ] Verify user.log file exists
- [ ] Verify error.log file exists
- [ ] Check file permissions are correct

---

## 🔍 Viewing Logs

### From Application:
Navigate to **Activity Log** from the Dashboard menu.

### From File System:
```bash
# View user logs
type logs\user.log

# View error logs
type logs\error.log

# View last 50 lines of user log
powershell -command "Get-Content logs\user.log -Tail 50"

# Search for specific user activity
findstr "admin" logs\user.log

# Search for errors
findstr "ERROR" logs\error.log
```

---

## 🛡️ Security Considerations

1. **Sensitive Information:** Passwords are never logged
2. **User Identification:** Both username and user_id logged for tracking
3. **IP Addresses:** Currently hardcoded to 127.0.0.1 (can be enhanced)
4. **Log Rotation:** Manual clearing via "Clear Old Logs" button (30 days)
5. **File Permissions:** Logs directory created with default permissions

---

## 🚀 Future Enhancements

1. **Log Rotation:** Automatic daily/weekly log file rotation
2. **IP Detection:** Real IP address detection instead of hardcoded
3. **Log Levels:** DEBUG, INFO, WARN, ERROR, FATAL
4. **Log Viewer:** Built-in log viewer with syntax highlighting
5. **Export Logs:** Export filtered logs to CSV/Excel
6. **Real-time Monitoring:** Live log tail in dashboard
7. **Log Archiving:** Compress and archive old logs automatically
8. **Email Alerts:** Send email on critical errors
9. **Database Logging:** Option to log errors to database table
10. **Audit Trail:** Detailed before/after values for updates

---

## 📈 Performance Considerations

- **File I/O:** Synchronized to prevent concurrent write issues
- **Memory:** Log messages written immediately, not buffered
- **Database:** Separate from logging to avoid circular dependencies
- **Limit:** Activity log panel limited to 500 most recent entries
- **Cleanup:** Manual cleanup required for old logs

---

## 🔧 Configuration

Currently hardcoded in `Logger.java`:
```java
private static final String USER_LOG_FILE = "logs/user.log";
private static final String ERROR_LOG_FILE = "logs/error.log";
```

To customize log locations, edit these constants.

---

## ✅ Compilation Status

- ✅ No compilation errors
- ✅ All dependencies resolved
- ✅ Ready for testing

---

## 📚 Summary

**Files Modified:** 6
1. util/Logger.java (NEW)
2. ui/ActivityLogPanel.java
3. model/UserModel.java
4. ui/HouseholdPanel.java
5. ui/Login.java
6. db/DbConnection.java

**Key Features:**
- Dual log file system (user.log + error.log)
- Username display in activity log panel
- Comprehensive error logging with stack traces
- CRUD operation tracking
- Authentication attempt logging
- Enhanced activity log panel UI

**Benefits:**
- Better debugging capabilities
- Audit trail for user actions
- Security monitoring (failed logins)
- Error diagnosis with full stack traces
- Searchable activity logs
- Historical data retention
