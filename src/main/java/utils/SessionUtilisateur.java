package utils;

public class SessionUtilisateur {

    private static Integer userId = 1; // temporaire pour tester Stripe

    public static void setUserId(Integer id) {
        userId = id;
    }

    public static Integer getUserId() {
        return userId;
    }

    public static void logout() {
        userId = null;
    }
}