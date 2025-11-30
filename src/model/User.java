package model;

public class User {
    private String userId;
    private String username;
    private String passwordHash;
    private String fullname;
    private String email;
    private String roleId;
    private String status;

    public User(String userId, String username, String passwordHash, String fullname, String email, String roleId, String status) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullname = fullname;
        this.email = email;
        this.roleId = roleId;
        this.status = status;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullname() { return fullname; }
    public String getEmail() { return email; }
    public String getRoleId() { return roleId; }
    public String getStatus() { return status; }

    // Setters if needed
    public void setFullname(String fullname) { this.fullname = fullname; }
    public void setEmail(String email) { this.email = email; }
    public void setRoleId(String roleId) { this.roleId = roleId; }
    public void setStatus(String status) { this.status = status; }
}
