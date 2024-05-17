package Services;


import Entities.User;

public class SessionContext {
    private static User currentUser;
    private static boolean isGuestUser = false;
    private static boolean isSubscribed;
    public static void setCurrentUser(User user) {
        currentUser = user;
        isGuestUser = (user == null);
        isSubscribed = false;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentUserId() {
        if (currentUser != null) {
            return currentUser.getId();
        }
        return null;
    }

    public static boolean isGuestUser() {
        return isGuestUser;
    }

    public static void setGuestUser(boolean isGuest) {
        isGuestUser = isGuest;
    }

    // Method to check if the current user is an admin based on ID
    public static boolean isUserAdmin() {
        if (currentUser != null) {
            return currentUser.getId().startsWith("ADM");
        }
        return false;
    }

    public static boolean isSubscribed() {
        return isSubscribed;
    }

    public static void setSubscribed(boolean subscribed) {
        isSubscribed = subscribed;
    }
}
