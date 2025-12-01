ve# User.java - Complete Explanation

**Date:** December 1, 2025  
**Status:** ✅ User.java IS NEEDED - Session Object

---

## 🎯 What Exactly Is User.java?

**User.java is a SESSION DATA TRANSFER OBJECT (DTO)**

It represents the **currently logged-in user's session data** that is stored in memory while the user is using the application.

---

## 📊 The Login Flow (Where User.java Is Used)

### **Step 1: User Enters Credentials**
```java
// Login.java
String usernameOrEmail = txtUsername.getText();
String password = new String(txtPassword.getPassword());
```

### **Step 2: LoginModel Verifies Credentials**
```java
// LoginModel.login()
boolean success = loginModel.login(usernameOrEmail, password);
// This checks password hash vs entered password
// Returns true/false BUT doesn't return User object
```

### **Step 3: Create Session - User Object Created**
```java
// Login.java (line 188)
if (success) {
    // ← THIS IS WHERE User.java IS USED!
    User currentUser = new UserModel().getUserByUsernameOrEmail(usernameOrEmail);
    SessionManager.getInstance().setCurrentUser(currentUser);
}
```

### **Step 4: Session Stored**
```java
// SessionManager.java
private User currentUser;  // ← Stores the User object

public void setCurrentUser(User user) {
    this.currentUser = user;  // ← Keeps user data in memory
}
```

### **Step 5: Throughout App - Check Current User**
```java
// Any panel can check who's logged in:
User currentUser = SessionManager.getInstance().getCurrentUser();

// Access user info:
String username = currentUser.getUsername();
String roleId = currentUser.getRoleId();
String fullname = currentUser.getFullname();

// Check permissions:
if (currentUser.getRoleId().equals("1")) {
    // Admin only features
}
```

---

## 🔍 Why User.java Has passwordHash Field

**Question:** "Why does User.java have passwordHash if it's for session?"

**Answer:** **It shouldn't actually have it for security reasons, BUT it's there from database SELECT *.**

### Current Implementation:
```java
// UserModel.getUserByUsernameOrEmail() - line 25
String sql = "SELECT * FROM users WHERE username = ? OR email = ?";
// ↑ This selects ALL columns including hashed_password

return new User(
    rs.getString("user_id"),
    rs.getString("username"),
    rs.getString("hashed_password"),  // ← Gets stored in session
    rs.getString("fullname"),
    rs.getString("email"),
    rs.getString("role_id"),
    rs.getString("status")
);
```

### **Security Issue:**
The passwordHash is stored in the session User object even though it's not needed after login. This is slightly insecure.

### **Better Implementation Would Be:**
```java
// Only select what's needed for session
String sql = "SELECT user_id, username, fullname, email, role_id, status " +
             "FROM users WHERE username = ? OR email = ?";
// ↑ NO password hash

return new User(
    rs.getString("user_id"),
    rs.getString("username"),
    null,  // ← No password hash in session
    rs.getString("fullname"),
    rs.getString("email"),
    rs.getString("role_id"),
    rs.getString("status")
);
```

---

## 📋 Three Different "User" Concepts

Your project actually has THREE different user-related classes:

### **1. User.java** (Session Object)
**Purpose:** Store currently logged-in user's data in memory  
**Lifespan:** While user is logged in  
**Contains:** userId, username, fullname, email, roleId, status (+ passwordHash unnecessarily)  
**Used by:** SessionManager, Login.java, Dashboard, various panels  
**Quantity:** 1 instance (the current user)

### **2. UserModel.UserDisplay** (UI Display Object)
**Purpose:** Display users in UsersPanel table for management  
**Lifespan:** Temporarily created when loading table  
**Contains:** userId, username, fullname, email, **roleName**, status (NO password!)  
**Used by:** UsersPanel.java  
**Quantity:** List of all users in system

### **3. Database Row** (Persistence)
**Location:** `users` table in MySQL  
**Contains:** user_id, username, password (hash), salt, fullname, email, role_id, status  
**Used by:** LoginModel (for verification), UserModel (for CRUD)

---

## 🔄 Complete Data Flow Diagram

```
┌─────────────────────────────────────────────────┐
│ 1. LOGIN                                        │
│    User enters: username + password             │
└────────────────────┬────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────┐
│ 2. LOGINMODEL.LOGIN()                           │
│    - Query DB for hashed_password + salt        │
│    - Verify password with PasswordHashing       │
│    - Return true/false                          │
└────────────────────┬────────────────────────────┘
                     ↓ if true
┌─────────────────────────────────────────────────┐
│ 3. CREATE SESSION (User.java)                   │
│    UserModel.getUserByUsernameOrEmail()         │
│    ↓                                            │
│    Query: SELECT * FROM users WHERE...          │
│    ↓                                            │
│    new User(userId, username, hash, ...)        │
│    ↓                                            │
│    SessionManager.setCurrentUser(user)          │
└────────────────────┬────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────┐
│ 4. SESSION STORED IN MEMORY                     │
│    SessionManager.currentUser = User object     │
│    (Stays in memory while user is logged in)    │
└────────────────────┬────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────┐
│ 5. THROUGHOUT APP                               │
│    Any panel can get current user:              │
│    User u = SessionManager.getCurrentUser();    │
│    String name = u.getFullname();               │
│    String role = u.getRoleId();                 │
└─────────────────────────────────────────────────┘
```

---

## ✅ Summary

### **What is User.java?**
A **session data object** that stores information about the currently logged-in user in memory.

### **Why does it exist alongside LoginModel and UserModel?**
- **LoginModel** = Handles authentication (verify password)
- **UserModel** = Handles user CRUD operations + creates User objects
- **User.java** = Data container for session (the object that gets stored)

### **Why does it have passwordHash?**
Because `getUserByUsernameOrEmail()` does `SELECT *` which includes the hash. **This is unnecessary and slightly insecure.** The hash should NOT be in session.

### **Should we keep User.java?**
**YES!** It's essential for session management. BUT we should:
1. ✅ Keep the class
2. ⚠️ Remove passwordHash from session (security improvement)
3. ✅ Use it for tracking logged-in user
4. ✅ Use it for permission checks throughout app

---

## 🔧 Recommended Improvements

### **1. Remove passwordHash from Session**

Update `UserModel.getUserByUsernameOrEmail()`:
```java
public User getUserByUsernameOrEmail(String usernameOrEmail) {
    if (conn == null) return null;
    
    // Only select what's needed for session (NO password!)
    String sql = "SELECT user_id, username, fullname, email, role_id, status " +
                 "FROM users WHERE username = ? OR email = ?";
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, usernameOrEmail);
        stmt.setString(2, usernameOrEmail);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return new User(
                rs.getString("user_id"),
                rs.getString("username"),
                null,  // ← No password hash in session!
                rs.getString("fullname"),
                rs.getString("email"),
                rs.getString("role_id"),
                rs.getString("status")
            );
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}
```

### **2. Update User.java Constructor**

Make passwordHash optional:
```java
public User(String userId, String username, String passwordHash, 
            String fullname, String email, String roleId, String status) {
    this.userId = userId;
    this.username = username;
    this.passwordHash = passwordHash;  // Can be null for session
    this.fullname = fullname;
    this.email = email;
    this.roleId = roleId;
    this.status = status;
}
```

---

## 🎯 Final Answer

**User.java is:**
- ✅ A session data object
- ✅ Created after successful login
- ✅ Stored in SessionManager
- ✅ Used throughout app to check current user
- ✅ NEEDED alongside LoginModel and UserModel
- ⚠️ Currently stores passwordHash (should be removed for security)

**It's CORRECT to have all three:**
- LoginModel (authentication logic)
- UserModel (user CRUD + create User objects)
- User.java (session data container)

**This is proper MVC + Session Management architecture!** ✅
