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

public class UserDAO {

    public static class LoggedInUser {
        private final int user_id;
        private final String username;
        private final String email;

        public LoggedInUser(int user_id, String username, String email) {
            this.user_id = user_id;
            this.username = username;
            this.email = email;
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


    public LoggedInUser login(String username, String password) {
        String sql = """
               SELECT user_id, username, email
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
                                rs.getString("email")
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
        String display_name = firstName + " " + lastName;

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

                LoggedInUser newUser = new LoggedInUser(newUserId, username, email);

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
}
