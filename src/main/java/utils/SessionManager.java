package utils;

import entities.User;

public class SessionManager {

    private static User currentUser;

    public static void setUser(User user) {
        currentUser = user;
    }

    public static User getUser() {
        return currentUser;
    }

    public static boolean isAdmin() {
        return currentUser != null &&
                "ADMIN".equalsIgnoreCase(currentUser.getRoleType());
    }

    public static void logout() {
        currentUser = null;
    }
}