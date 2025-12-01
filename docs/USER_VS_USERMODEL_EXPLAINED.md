# User.java vs UserModel Explanation

**Date:** December 1, 2025

## ✅ Both Are Correct - Different Purposes!

### **User.java** - For Authentication/Session Management
**Location:** `model/User.java`  
**Purpose:** Represents the **currently logged-in user** with credentials

**Contains:**
```java
public class User {
    private String userId;
    private String username;
    private String passwordHash;  // ← For login verification
    private String fullname;
    private String email;
    private String roleId;
    private String status;
}
```

**Used By:**
- `Login.java` - For authentication
- `Dashboard.java` - To display current user info
- `SessionManager.java` - To track who's logged in
- Various panels - To check permissions

**Why it has passwordHash:**
When user logs in, system needs to:
1. Get user record from database (with password hash)
2. Hash the entered password
3. Compare hashes to verify login
4. Store User object in session (SessionManager)

This is for **AUTHENTICATION** - verifying WHO the user is.

---

### **UserModel.UserDisplay** - For UI Display
**Location:** `model/UserModel.java` (inner class)  
**Purpose:** Represents users shown in **UsersPanel table** (admin managing users)

**Contains:**
```java
public static class UserDisplay {
    private String userId;
    private String username;
    private String fullname;
    private String email;
    private String roleName;  // ← Role NAME, not ID
    private String status;
    // NO passwordHash ← Security!
    // NO salt ← Security!
}
```

**Used By:**
- `UsersPanel.java` - To display list of all users

**Why it has NO password:**
- Security! Passwords should NEVER be visible in UI
- Even admins shouldn't see password hashes
- Password is only for login verification, not display

This is for **USER MANAGEMENT** - admin viewing/editing user list.

---

## 📊 Comparison

| Feature | User.java | UserModel.UserDisplay |
|---------|-----------|----------------------|
| **Purpose** | Authentication | UI Display |
| **Represents** | Current logged-in user | All users in system |
| **Has password** | ✅ Yes (for login) | ❌ No (security) |
| **Has salt** | ❌ No | ❌ No |
| **Has role** | role_id (for permissions) | role_name (for display) |
| **Used in** | Login, Session, Dashboard | UsersPanel only |
| **Quantity** | 1 instance (current user) | List of all users |

---

## 🔐 Security Flow

### **Login (uses User.java):**
```java
// 1. User enters credentials
String username = "admin";
String password = "password123";

// 2. LoginModel gets User from DB (with hash)
User user = LoginModel.getUserByUsername(username);
// user.passwordHash = "xyz123abc..." (from database)

// 3. Verify password
boolean valid = PasswordHashing.verifyPassword(
    password,           // entered
    user.getSalt(),     // from DB
    user.getPasswordHash()  // from DB
);

// 4. If valid, store in session
if (valid) {
    SessionManager.setCurrentUser(user);
    // User object now in session (WITH hash for permission checks)
}
```

### **User Management (uses UserDisplay):**
```java
// 1. Load users for display
List<UserDisplay> users = UserModel.getAllUsers();
// Query: SELECT user_id, username, fullname, email, role_name, status
//        (NOTE: password and salt NOT selected!)

// 2. Display in table
for (UserDisplay user : users) {
    table.addRow(new Object[]{
        user.getUserId(),
        user.getUsername(),
        user.getFullname(),  // NOT passwordHash!
        user.getEmail(),
        user.getRoleName(),  // "Admin" not "1"
        user.getStatus()
    });
}
// Password column NEVER shown!
```

---

## ✅ Why Both Exist

1. **User.java** = Session object (who's logged in RIGHT NOW)
   - Needs hash for authentication
   - One instance per logged-in user
   - Used throughout app for permissions

2. **UserModel.UserDisplay** = Display object (list of ALL users)
   - NO hash for security
   - Many instances (all users in system)
   - Used only in UsersPanel for management

---

## 🎯 Summary

**Q:** Why is there User.java if we have UserModel?  
**A:** Different purposes! User.java is for authentication/session, UserModel is for CRUD operations.

**Q:** Why does User.java have passwordHash?  
**A:** For login verification. When user logs in, we need to compare hashes. This is NOT displayed in UI.

**Q:** Why doesn't UserDisplay have password?  
**A:** Security! Admins managing users should NEVER see passwords or hashes. Password is only retrieved during login, never for display.

**Q:** Should we remove User.java?  
**A:** NO! It's essential for authentication and session management.

---

## ✅ Conclusion

**Both are correct and necessary:**
- ✅ Keep `User.java` - for authentication
- ✅ Keep `UserModel.UserDisplay` - for UI display
- ✅ They serve different purposes
- ✅ This is proper separation of concerns

**The architecture is CORRECT!** 🎯
