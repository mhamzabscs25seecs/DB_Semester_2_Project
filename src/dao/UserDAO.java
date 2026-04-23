package dao;
// DAO stands for Data Access Object. It is simply a java class that whose job is to talk
// to the database. Database only understands SQL and Java GUI only understands java.
// To translate between SQL and Java is the job of the dao i.e translates java into SQL
// and SQL into java.

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public static class LoggedInUser {
        private final int user_id;
        private final String username;
        private final String email;
        private final String role;

        public LoggedInUser(int user_id, String username, String email, String role) {
            this.user_id = user_id;
            this.username = username;
            this.email = email;
            this.role = role;
        }

        // Getters
        public int getUser_id() {
            return user_id;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }


    }

    public static class RegisterResult {
        private final boolean success;
        private final String message;
        private final LoggedInUser user;

        public RegisterResult(boolean success, String message, LoggedInUser user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public LoggedInUser getUser() {
            return user;
        }

    }

    public static class UserProfile {
        private final int userId;
        private final String username;
        private final String email;
        private final String displayName;
        private final String bioText;
        private final String country;
        private final String phoneNo;
        private final int birthYear;
        private final boolean isPrivate;

        public UserProfile(int userId, String username, String email, String displayName,
                           String bioText, String country, String phoneNo,
                           int birthYear, boolean isPrivate) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.displayName = displayName;
            this.bioText = bioText;
            this.country = country;
            this.phoneNo = phoneNo;
            this.birthYear = birthYear;
            this.isPrivate = isPrivate;
        }

        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getDisplayName() { return displayName; }
        public String getBioText() { return bioText; }
        public String getCountry() { return country; }
        public String getPhoneNo() { return phoneNo; }
        public int getBirthYear() { return birthYear; }
        public boolean isPrivate() { return isPrivate; }
    }

    public static class AdminUserRow {
        private final int userId;
        private final String username;
        private final String email;
        private final String role;
        private final int postCount;
        private final int commentCount;
        private final int communityCount;

        public AdminUserRow(int userId, String username, String email, String role,
                            int postCount, int commentCount, int communityCount) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.role = role;
            this.postCount = postCount;
            this.commentCount = commentCount;
            this.communityCount = communityCount;
        }

        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public int getPostCount() { return postCount; }
        public int getCommentCount() { return commentCount; }
        public int getCommunityCount() { return communityCount; }
    }


    public LoggedInUser login(String username, String password) {
        String sql = """
               SELECT user_id, username, email, role
               FROM Users
               WHERE username = ? AND password_hash = ?
               """;
        /* Note that we do not give the User input directly to the SQL String because it can lead to
         SQL Injection which is a security vulnerability where an attacker can manipulate the SQL
         query by injecting malicious input. By using PreparedStatement and parameterized queries,
          we can prevent SQL injection attacks. The '?' in the SQL string are placeholders for the
           parameters that will be set later using the setString method. This way, the user input
            is treated as data rather than part of the SQL command, making it safe from injection
            attacks. */

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql))
            {
                // setString(1, ...) means "replace the 1st question mark with the
                // username variable. and so on.
                pstmt.setString(1, username);

                pstmt.setString(2, password);

                // Now we want the database to execute that query.
                // executeQuery() returns back a sql output i.e a mini spreadsheet.
                /* After the query is executed, the cursor is actually at the top hidder header. So we use
                 rs.next() to move to the first row of the result set.
                 If there is a row, it returns true, otherwise it returns false.
                 */
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        // If the if condition is satisfied, it found a row where username AND
                        // password matched.
                        return new LoggedInUser(
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getString("email"),
                                rs.getString("role")
                        );
                    }
                }

            }

        catch (SQLException e) {
            System.out.println("Login Database Eror: " + e.getMessage());
        }

        return null;
    }

    public RegisterResult register(String firstName, String lastName, String username, String email, String password) {
        String display_name = (firstName + " " + lastName).trim();

        String insertUserSQL = """
                INSERT INTO Users (username, email, password_hash)
                VALUES (?, ?, ?)
                """;

        String insertUserProfileSQL = """
                INSERT INTO User_Profiles (user_id, display_name, birth_year, is_private)
                VALUES (?, ?, ?, 0)
                """;


        try (Connection conn = DBConnection.getConnection()) {
            // if user is not created user profile should not be created
            conn.setAutoCommit(false);

            try (
                    PreparedStatement userStmt = conn.prepareStatement(insertUserSQL, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement userProfileStmt = conn.prepareStatement(insertUserProfileSQL)
            ) {
                userStmt.setString(1, username);
                userStmt.setString(2, email);
                userStmt.setString(3, password);
                userStmt.executeUpdate();

                int newUserId;

                try (ResultSet keys = userStmt.getGeneratedKeys()) {
                    if (!keys.next()) {
                        conn.rollback();
                        return new RegisterResult(false, "Couldn't create user account", null);

                    }

                    newUserId = keys.getInt(1);
                }

                userProfileStmt.setInt(1, newUserId);
                userProfileStmt.setString(2, display_name);
                userProfileStmt.setInt(3, 2000);


                userProfileStmt.executeUpdate();


                conn.commit();

                LoggedInUser newUser = new LoggedInUser(newUserId, username, email, "user");

                return new RegisterResult(true, "Successfully registered user", newUser);

            } catch (SQLException e) {
                conn.rollback();

                String error = e.getMessage() == null ? "" : e.getMessage().toLowerCase();

                if (error.contains("users.username")) {
                    return new RegisterResult(false, "Username is already taken.", null);
                }

                if (error.contains("users.email")) {
                    return new RegisterResult(false, "Email is already registered.", null);
                }

                return new RegisterResult(false, "Registration failed: " + e.getMessage(), null);

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            return new RegisterResult(false, "Database error: " + e.getMessage(), null);
        }
    }

    public UserProfile getProfileById(int userId) {
        String sql = """
                SELECT
                    u.user_id,
                    u.username,
                    u.email,
                    up.display_name,
                    up.bio_text,
                    up.country,
                    up.phone_no,
                    up.birth_year,
                    up.is_private
                FROM Users u
                LEFT JOIN User_Profiles up ON u.user_id = up.user_id
                WHERE u.user_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserProfile(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("display_name"),
                            rs.getString("bio_text"),
                            rs.getString("country"),
                            rs.getString("phone_no"),
                            rs.getInt("birth_year"),
                            rs.getInt("is_private") == 1
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Profile database error: " + e.getMessage());
        }

        return null;
    }

    public boolean updateProfile(int userId, String displayName, String bioText, String country,
                                 String phoneNo, int birthYear, boolean isPrivate) {
        String sql = """
                UPDATE User_Profiles
                SET display_name = ?,
                    bio_text = ?,
                    country = ?,
                    phone_no = ?,
                    birth_year = ?,
                    is_private = ?
                WHERE user_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, displayName);
            stmt.setString(2, blankToNull(bioText));
            stmt.setString(3, blankToNull(country));
            stmt.setString(4, blankToNull(phoneNo));
            stmt.setInt(5, birthYear);
            stmt.setInt(6, isPrivate ? 1 : 0);
            stmt.setInt(7, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Update profile database error: " + e.getMessage());
        }

        return false;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public List<AdminUserRow> getAdminUserRows() {
        List<AdminUserRow> users = new ArrayList<>();
        String sql = """
                SELECT
                    u.user_id,
                    u.username,
                    u.email,
                    u.role,
                    COALESCE(p.post_count, 0) AS post_count,
                    COALESCE(cm.comment_count, 0) AS comment_count,
                    COALESCE(c.community_count, 0) AS community_count
                FROM Users u
                LEFT JOIN (
                    SELECT posted_by, COUNT(*) AS post_count
                    FROM Posts
                    GROUP BY posted_by
                ) p ON u.user_id = p.posted_by
                LEFT JOIN (
                    SELECT commenter_id, COUNT(*) AS comment_count
                    FROM Comments
                    GROUP BY commenter_id
                ) cm ON u.user_id = cm.commenter_id
                LEFT JOIN (
                    SELECT created_by, COUNT(*) AS community_count
                    FROM Communities
                    GROUP BY created_by
                ) c ON u.user_id = c.created_by
                ORDER BY u.role DESC, u.username ASC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(new AdminUserRow(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getInt("post_count"),
                        rs.getInt("comment_count"),
                        rs.getInt("community_count")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Admin users database error: " + e.getMessage());
        }

        return users;
    }

    public boolean deleteUserByAdmin(int adminUserId, int targetUserId) {
        if (!isAdmin(adminUserId) || adminUserId == targetUserId) {
            return false;
        }

        String sql = """
                DELETE FROM Users
                WHERE user_id = ? AND role <> 'admin'
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, targetUserId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Admin delete user database error: " + e.getMessage());
        }

        return false;
    }

    public boolean isAdmin(int userId) {
        String sql = """
                SELECT 1
                FROM Users
                WHERE user_id = ? AND role = 'admin'
                LIMIT 1
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.out.println("Admin check database error: " + e.getMessage());
        }

        return false;
    }
}
