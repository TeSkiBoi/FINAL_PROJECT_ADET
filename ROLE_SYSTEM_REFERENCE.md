# Role System Reference Guide

## Database Structure

### Roles Table
```
CREATE TABLE roles (
    role_id INT(11) PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL,
    description TEXT,
    permissions TEXT
);
```

### Default Roles
| role_id | role_name | Description |
|---------|-----------|-------------|
| 1       | Administrator | Full system access - can manage all modules, users, and settings |
| 2       | Staff | Limited access - view-only for most modules, cannot delete or modify critical data |

### Users Table
```
CREATE TABLE users (
    user_id VARCHAR(10) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    hashed_password VARCHAR(255),
    salt VARCHAR(255),
    fullname VARCHAR(100),
    email VARCHAR(100),
    role_id INT(11),
    status VARCHAR(20),
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);
```

## Role Implementation

### 1. Data Type
- **role_id** is stored as `INT(11)` in the database
- **roleId** in Java is handled as `String` for consistency
- Comparisons use **String literals**: `"1"` for Admin, `"2"` for Staff

### 2. Role Checking Pattern

#### In UI Components:
```java
User current = SessionManager.getInstance().getCurrentUser();
if (current != null && "1".equals(current.getRoleId())) {
    // Administrator access
} else if (current != null && "2".equals(current.getRoleId())) {
    // Staff access (limited)
}
```

#### In Login.java:
```java
String roleId = currentUser.getRoleId();
if ("1".equals(roleId)) {
    // Admin - Full Dashboard access
    new Dashboard().setVisible(true);
} else if ("2".equals(roleId)) {
    // Staff - Limited Dashboard access
    new Dashboard().setVisible(true);
}
```

### 3. User Model (model/User.java)
```java
public class User {
    private String roleId;  // Stored as String
    
    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }
}
```

### 4. Database Retrieval (model/UserModel.java)
```java
public User getUserByUsernameOrEmail(String usernameOrEmail) {
    // SQL: SELECT role_id FROM users...
    return new User(
        rs.getString("user_id"),
        rs.getString("username"),
        rs.getString("hashed_password"),
        rs.getString("fullname"),
        rs.getString("email"),
        rs.getString("role_id"),  // Retrieved as String
        rs.getString("status")
    );
}

public String getRoleName(String roleId) {
    // SQL: SELECT role_name FROM roles WHERE role_id = ?
    // Returns: "Administrator" or "Staff"
}
```

## Role-Based Access Control (RBAC)

### Administrator (role_id = 1) Permissions:
- ✅ Full access to all panels
- ✅ Create, Read, Update, Delete operations
- ✅ User management
- ✅ Role management
- ✅ System configuration
- ✅ View all reports
- ✅ Delete households, residents, officials, etc.

### Staff (role_id = 2) Permissions:
- ✅ View most panels (read-only)
- ❌ Cannot add/edit/delete households
- ❌ Cannot add/edit/delete residents
- ❌ Cannot manage users
- ❌ Cannot manage roles
- ✅ Can view reports
- ❌ Limited dashboard menu

### Implementation Examples:

#### HouseholdPanel.java
```java
private boolean isStaff = false;

public HouseholdPanel() {
    User current = SessionManager.getInstance().getCurrentUser();
    if (current != null && "2".equals(current.getRoleId())) {
        isStaff = true;
    }
    
    // Disable buttons for staff
    if (isStaff) {
        btnAdd.setEnabled(false);
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);
        btnManageMembers.setEnabled(false);
    }
}
```

#### Dashboard.java
```java
boolean isAdmin = (current != null && "1".equals(current.getRoleId()));
boolean isStaff = (current != null && "2".equals(current.getRoleId()));

// Build menu based on role
if (isAdmin) {
    // Add all menu items
    menuBar.add(createMenu("Households", e -> showPanel(new HouseholdPanel())));
    menuBar.add(createMenu("Users", e -> showPanel(new UsersPanel())));
    menuBar.add(createMenu("Roles", e -> showPanel(new RolesPanel())));
    // ... etc
} else if (isStaff) {
    // Limited menu items
    menuBar.add(createMenu("Households", e -> showPanel(new HouseholdPanel())));
    menuBar.add(createMenu("Residents", e -> showPanel(new ResidentPanel())));
    // No Users, Roles, or Delete operations
}
```

## Best Practices

### 1. Always Use String Comparison
```java
// ✅ CORRECT
if ("1".equals(user.getRoleId())) { }

// ❌ WRONG - Don't parse to int
if (Integer.parseInt(user.getRoleId()) == 1) { }
```

### 2. Check for Null
```java
User current = SessionManager.getInstance().getCurrentUser();
if (current != null && "1".equals(current.getRoleId())) {
    // Safe to proceed
}
```

### 3. Use Constants (Optional Enhancement)
```java
public class Roles {
    public static final String ADMIN = "1";
    public static final String STAFF = "2";
}

// Usage:
if (Roles.ADMIN.equals(user.getRoleId())) { }
```

## Role Management UI

### Adding New Roles (RolesPanel.java)
- Administrators can add custom roles via the Roles panel
- New role_id values are auto-incremented by the database
- Each role can have:
  - role_name (required)
  - description (optional)
  - permissions (optional - for future RBAC expansion)

### Assigning Roles (UsersPanel.java)
```java
// When creating/editing users:
JComboBox<String> cboRole = new JComboBox<>();

// Populate from database
Statement st = conn.createStatement();
ResultSet rs = st.executeQuery("SELECT role_id, role_name FROM roles ORDER BY role_id");
while (rs.next()) {
    cboRole.addItem(rs.getString("role_id") + ": " + rs.getString("role_name"));
}
```

## Testing Role-Based Access

### Test Cases:
1. **Admin Login**: Verify full access to all panels and operations
2. **Staff Login**: Verify read-only access, buttons disabled
3. **Role Change**: Change user's role_id, verify access changes immediately on next login
4. **Invalid Role**: Test with role_id not in roles table (should handle gracefully)

## Troubleshooting

### Issue: Role check not working
**Solution**: Verify role_id is retrieved as String, use `.equals()` not `==`

### Issue: Staff can still edit/delete
**Solution**: Check if button disable logic is in the panel constructor after role check

### Issue: Dashboard doesn't show correct menu
**Solution**: Verify Dashboard reads role_id correctly and builds menu conditionally

## Security Notes

- Role checks are performed on UI level (button disable)
- **Important**: Should also implement server-side/database-level checks for production
- Logging: All role-based actions are logged via util.Logger
- Session: Role is cached in SessionManager, persists until logout

---

**Last Updated**: November 30, 2025
**Version**: 1.0
