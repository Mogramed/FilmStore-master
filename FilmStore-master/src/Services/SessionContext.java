package Services;


import Entities.User;

public class SessionContext {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
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

    // Method to check if the current user is an admin based on ID
    public static boolean isUserAdmin() {
        if (currentUser != null) {
            return currentUser.getId().startsWith("ADM");
        }
        return false;
    }

}
