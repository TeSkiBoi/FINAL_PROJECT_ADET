# ✅ User.java Complete Answer + Security Fix Applied

**Date:** December 1, 2025

---

## 🎯 What EXACTLY is User.java?

**User.java is a SESSION DATA OBJECT** - it stores information about the currently logged-in user in memory.

---

## 📊 How It Works With LoginModel and UserModel

You have THREE components working together:

### **1. LoginModel** (Authentication)
**Purpose:** Verify username + password  
**What it does:**
- Takes username/email + password
- Queries database for password hash + salt
- Uses `PasswordHashing.verifyPassword()` to check
- Returns `true` or `false`
- **Does NOT create User objects**

**Code:**
```java
boolean success = loginModel.login(usernameOrEmail, password);
// Just returns true/false
```

---

### **2. UserModel** (User CRUD + Session Object Creation)
**Purpose:** Manage users in database + create User objects

**Has TWO different inner classes:**

#### **A. UserDisplay** (For UI Management)
```java
UserModel.UserDisplay  // For UsersPanel table
- NO password
- Shows role NAME not ID
- Used to display/manage all users
```

#### **B. getUserByUsernameOrEmail()** (For Session)
```java
User user = new UserModel().getUserByUsernameOrEmail("admin");
// Creates User object for session
// Used AFTER successful login
```

---

### **3. User.java** (Session Data Container)
**Purpose:** Store logged-in user's data in memory  
**What it contains:**
- userId
- username
- fullname
- email
- roleId (for permission checks)
- status
- ~~passwordHash~~ ← **NOW REMOVED FOR SECURITY!**

**Used by:** SessionManager to track current user

---

## 🔄 Complete Login Flow

```
USER LOGS IN
    ↓
┌─────────────────────────────────────┐
│ 1. LoginModel.login()               │
│    - Verify password                │
│    - Returns: true/false             │
└──────────────┬──────────────────────┘
               ↓ if true
┌─────────────────────────────────────┐
│ 2. UserModel.getUserByUsername()    │
│    - Query: SELECT user_id, username│
│              fullname, email, role  │
│    - Create: new User(...)          │
│    - Returns: User object           │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 3. SessionManager.setCurrentUser()  │
│    - Stores User in memory          │
│    - User stays until logout        │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 4. Throughout App                   │
│    User u = SessionManager          │
│             .getCurrentUser();      │
│    if (u.getRoleId().equals("1"))   │
│       // Admin features              │
└─────────────────────────────────────┘
```

---

## ✅ Security Improvement Applied

### **BEFORE (Insecure):**
```java
// UserModel.getUserByUsernameOrEmail()
String sql = "SELECT * FROM users WHERE username = ?";
//              ↑ This selects password hash too!

return new User(
    rs.getString("user_id"),
    rs.getString("username"),
    rs.getString("hashed_password"),  // ← Stored in session! BAD!
    rs.getString("fullname"),
    rs.getString("email"),
    rs.getString("role_id"),
    rs.getString("status")
);
```

### **AFTER (Secure) ✅:**
```java
// UserModel.getUserByUsernameOrEmail()
String sql = "SELECT user_id, username, fullname, email, role_id, status " +
             "FROM users WHERE username = ?";
//  ↑ Only selects what's needed - NO password!

return new User(
    rs.getString("user_id"),
    rs.getString("username"),
    null,  // ← No password hash in session! SECURE!
    rs.getString("fullname"),
    rs.getString("email"),
    rs.getString("role_id"),
    rs.getString("status")
);
```

---

## 📋 Summary: Why Each Exists

| Component | Purpose | When Used |
|-----------|---------|-----------|
| **LoginModel** | Verify credentials | During login |
| **UserModel** | Manage users + create User objects | Login + UsersPanel |
| **User.java** | Store session data | After login, throughout app |
| **UserModel.UserDisplay** | Display users in table | UsersPanel only |

---

## ✅ Final Answer

**Q:** What exactly is User.java when we have LoginModel and UserModel?

**A:** 
- **LoginModel** = Authenticates (verifies password)
- **UserModel** = Manages users (CRUD) + creates User objects
- **User.java** = Session data container (stores logged-in user info)

**They work TOGETHER:**
1. LoginModel verifies password ✅
2. UserModel creates User object ✅
3. User.java stores session data ✅
4. SessionManager keeps User in memory ✅

**All three are needed!** This is proper MVC + Session architecture.

**Security fix applied:** Password hash NO LONGER stored in session! ✅

---

## 🎯 What You Have Now

✅ **LoginModel** - Authentication logic  
✅ **UserModel** - User CRUD + 2 display classes  
✅ **User.java** - Session object (NO password in session anymore!)  
✅ **SessionManager** - Tracks current user  
✅ **Clean separation** - Each has distinct purpose  
✅ **Secure** - Password hash not in session  

**Perfect architecture!** 🏆
