package model;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public void logout() {
        if (currentUser != null) {
            try {
                new UserModel().logUserActivity(currentUser.getUserId(), "User logged out", "127.0.0.1");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        currentUser = null;
    }
}
