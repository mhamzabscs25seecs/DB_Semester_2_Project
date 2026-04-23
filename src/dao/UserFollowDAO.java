package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserFollowDAO {
    public static class FollowedUser {
        private final int userId;
        private final String username;
        private final String displayName;
        private final String bioText;
        private final String followedAt;

        public FollowedUser(int userId, String username, String displayName, String bioText, String followedAt) {
            this.userId = userId;
            this.username = username;
            this.displayName = displayName;
            this.bioText = bioText;
            this.followedAt = followedAt;
        }

        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getDisplayName() { return displayName; }
        public String getBioText() { return bioText; }
        public String getFollowedAt() { return followedAt; }
    }

    public boolean isFollowing(int followerId, int followedId) {
        String sql = """
                SELECT 1
                FROM User_Follows
                WHERE follower_id = ? AND followed_id = ?
                LIMIT 1
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, followerId);
            stmt.setInt(2, followedId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.out.println("Follow check database error: " + e.getMessage());
        }

        return false;
    }

    public boolean followUser(int followerId, int followedId) {
        if (followerId == followedId) {
            return false;
        }

        String sql = """
                INSERT OR IGNORE INTO User_Follows (follower_id, followed_id)
                VALUES (?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, followerId);
            stmt.setInt(2, followedId);
            return stmt.executeUpdate() >= 0;

        } catch (SQLException e) {
            System.out.println("Follow user database error: " + e.getMessage());
        }

        return false;
    }

    public boolean unfollowUser(int followerId, int followedId) {
        String sql = """
                DELETE FROM User_Follows
                WHERE follower_id = ? AND followed_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, followerId);
            stmt.setInt(2, followedId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Unfollow user database error: " + e.getMessage());
        }

        return false;
    }

    public boolean toggleFollow(int followerId, int followedId) {
        if (isFollowing(followerId, followedId)) {
            return unfollowUser(followerId, followedId);
        }

        return followUser(followerId, followedId);
    }

    public int getFollowerCount(int userId) {
        return count("""
                SELECT COUNT(*)
                FROM User_Follows
                WHERE followed_id = ?
                """, userId);
    }

    public int getFollowingCount(int userId) {
        return count("""
                SELECT COUNT(*)
                FROM User_Follows
                WHERE follower_id = ?
                """, userId);
    }

    public List<FollowedUser> getFollowingUsers(int userId, String query) {
        List<FollowedUser> users = new ArrayList<>();
        String normalizedQuery = query == null ? "" : query.trim();
        String likeQuery = "%" + normalizedQuery + "%";

        String sql = """
                SELECT
                    u.user_id,
                    u.username,
                    up.display_name,
                    up.bio_text,
                    uf.followed_at
                FROM User_Follows uf
                JOIN Users u ON uf.followed_id = u.user_id
                LEFT JOIN User_Profiles up ON u.user_id = up.user_id
                WHERE uf.follower_id = ?
                  AND (
                        ? = ''
                        OR u.username LIKE ?
                        OR COALESCE(up.display_name, '') LIKE ?
                        OR COALESCE(up.bio_text, '') LIKE ?
                  )
                ORDER BY uf.followed_at DESC, u.username ASC
                LIMIT 50
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, normalizedQuery);
            stmt.setString(3, likeQuery);
            stmt.setString(4, likeQuery);
            stmt.setString(5, likeQuery);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new FollowedUser(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("display_name"),
                            rs.getString("bio_text"),
                            rs.getString("followed_at")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Following users database error: " + e.getMessage());
        }

        return users;
    }

    public List<FollowedUser> getFollowerUsers(int userId, String query) {
        List<FollowedUser> users = new ArrayList<>();
        String normalizedQuery = query == null ? "" : query.trim();
        String likeQuery = "%" + normalizedQuery + "%";

        String sql = """
                SELECT
                    u.user_id,
                    u.username,
                    up.display_name,
                    up.bio_text,
                    uf.followed_at
                FROM User_Follows uf
                JOIN Users u ON uf.follower_id = u.user_id
                LEFT JOIN User_Profiles up ON u.user_id = up.user_id
                WHERE uf.followed_id = ?
                  AND (
                        ? = ''
                        OR u.username LIKE ?
                        OR COALESCE(up.display_name, '') LIKE ?
                        OR COALESCE(up.bio_text, '') LIKE ?
                  )
                ORDER BY uf.followed_at DESC, u.username ASC
                LIMIT 50
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, normalizedQuery);
            stmt.setString(3, likeQuery);
            stmt.setString(4, likeQuery);
            stmt.setString(5, likeQuery);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new FollowedUser(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("display_name"),
                            rs.getString("bio_text"),
                            rs.getString("followed_at")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Follower users database error: " + e.getMessage());
        }

        return users;
    }

    private int count(String sql, int userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (SQLException e) {
            System.out.println("Follow count database error: " + e.getMessage());
        }

        return 0;
    }
}
