package dao;
// DAO stands for Data Access Object. It is simply a java class that whose job is to talk
// to the database. Database only understands SQL and Java GUI only understands java.
// To translate between SQL and Java is the job of the dao i.e translates java into SQL
// and SQL into java.

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class UserDAO {

    public static class LoggedInUser {
        private final int user_id;
        private final String username;
        private final String email;

        public LoggedInUser (int user_id, String username, String email) {
            this.user_id = user_id;
            this.username = username;
            this.email = email;
        }

        // Getters
        public int getUser_id() { return user_id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }


    }


    public LoggedInUser login (String username, String password) {
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
                // setString(1, ...) means "replace the 1st question mark with the username variable. and so on.
                pstmt.setString (1, username);
                pstmt.setString (2, password);

                // Now we want the database to execute that query
                // executeQuery() returns back a sql output i.e a mini spreadsheet.
                /* After the query is executed, the cursor is actually at the top hidder header. So we use
                 rs.next() to move to the first row of the result set.
                 If there is a row, it returns true, otherwise it returns false.
                 */
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        // If the if condition is satified, it means that it found a row where username AND
                        // password matched.
                        return new LoggedInUser (
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getString("email")
                                                );
                    }
                }

            }

        catch (SQLException e) {
            System.out.println("Login Database Error: " + e.getMessage());
        }

        return null;
    }


}
