package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SavedPostDAO {
    public boolean isPostSaved(int userId, int postId) {
        String sql = """
                SELECT 1
                FROM Saved_Posts
                WHERE user_id = ? AND post_id = ?
                LIMIT 1
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, postId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Saved post check database error: " + e.getMessage());
        }

        return false;
    }

    public boolean toggleSavedPost(int userId, int postId) {
        if (isPostSaved(userId, postId)) {
            return unsavePost(userId, postId);
        }

        return savePost(userId, postId);
    }

    private boolean savePost(int userId, int postId) {
        String sql = """
                INSERT OR IGNORE INTO Saved_Posts (user_id, post_id)
                VALUES (?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, postId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Save post database error: " + e.getMessage());
        }

        return false;
    }

    private boolean unsavePost(int userId, int postId) {
        String sql = """
                DELETE FROM Saved_Posts
                WHERE user_id = ? AND post_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, postId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Unsave post database error: " + e.getMessage());
        }

        return false;
    }
}
