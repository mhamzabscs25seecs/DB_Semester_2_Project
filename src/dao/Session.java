package dao;


public class Session {
    // If a user is logged in, this variable will hold their info. Otherwise, it will be null.
    private static int currentUserId;
    private static String currentUsername;
    private static String currentEmail;

    // Behaviours of a session
    public static void login (UserDAO.LoggedInUser user) {
        currentUserId = user.getUser_id();
        currentUsername = user.getUsername();
        currentEmail = user.getEmail();
    }

    public static void logout () {
        currentUserId = 0;
        currentUsername = null;
        currentEmail = null;
    }

    // Getters
    public static int getCurrentUserId() {return currentUserId;}
    public static String getCurrentUsername() {return currentUsername;}
    public static String getCurrentEmail() {return currentEmail;}

    // Checking if user is logged in
    public static boolean isLoggedIn() { return currentUserId > 0;  }
    
}
