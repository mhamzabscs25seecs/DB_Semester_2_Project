package dao;

/* Java doesnt know how to talk to a database much less than a SQLite database,
    so to make this communication possible we need 2 things:
     One is JDBC (Java Database Connectivity) -> a library built into java that provides the
     rules of how java communicates with any database
     The other is the SQLITE JDBC Driver which I have downloaded and added to my project.

     Actually JDBC is a 'Universal Adapter'. To talk to a SQLITE Database, the Universal Adapter's
     language is to be 'translated' to java which the SQLITE JDBC driver does.
 */


// Importing java's built in database tools.
/* DriverManager: This is the "telephone operator." You give it a database address, and it
 searches through your project's drivers to find the right one (the SQLite driver) to make
 the connection.
 */
/* The Statement class is responsible for sending SQL commands to the database.
Once you have a Connection, you can create a Statement object from it, and then use
that Statement to execute SQL queries (like SELECT, INSERT, UPDATE, DELETE) against the database.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

/* This is the class that handles if an exception is thrown. It can be because of a
    connection failure, or an error in the SQL query, etc. */


public class DBConnection {
    /* in next statement, by writing jdbc we are telling Java to use the JDBC API to connect to the database.
    Then we specify to the DriverManager to look for the SQLITE driver which is in the project
        and then we tell the path to the database file. */
    private static final String URL = "jdbc:sqlite:SQL files/Clixky.db";

    public static Connection getConnection () throws SQLException {

        Connection conn = DriverManager.getConnection(URL);

        /* By default, SQLite ignore the Foreign Key constraints which are linking the
    tables together. So we need to enable it by executing the following SQL command. */
        // Opening up a Statement takes computer memory so we declare it in the
        // parantheses. This is known as 'Try-by-Resources' which automatically closes the
        // statement Obj preventing memory leaks.
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        // Notice that we do not make the DBConnection class tell us the reason why
        // a connection failed. So we do not use catch.
        return conn;
    }
}
