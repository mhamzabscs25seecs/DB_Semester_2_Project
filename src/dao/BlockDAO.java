package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BlockDAO {
    public static class BlockedUserRow {
        private final int userId;
        private final String username;
        private final String displayName;
        private final String blockedAt;

        public BlockedUserRow(int userId, String username, String displayName, String blockedAt) {
            this.userId = userId;
            this.username = username;
            this.displayName = displayName;
            this.blockedAt = blockedAt;
        }

        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getDisplayName() { return displayName; }
        public String getBlockedAt() { return blockedAt; }
    }

    public static class BlockedCommunityRow {
        private final int communityId;
        private final String communityName;
        private final String description;
        private final String blockedAt;

        public BlockedCommunityRow(int communityId, String communityName, String description, String blockedAt) {
            this.communityId = communityId;
            this.communityName = communityName;
            this.description = description;
            this.blockedAt = blockedAt;
        }

        public int getCommunityId() { return communityId; }
        public String getCommunityName() { return communityName; }
        public String getDescription() { return description; }
        public String getBlockedAt() { return blockedAt; }
    }

    public boolean isUserBlocked(int blockerId, int blockedId) {
        return exists("""
                SELECT 1 FROM User_Blocks
                WHERE blocker_id = ? AND blocked_id = ?
                LIMIT 1
                """, blockerId, blockedId);
    }

    public boolean isEitherUserBlocked(int firstUserId, int secondUserId) {
        return exists("""
                SELECT 1 FROM User_Blocks
                WHERE (blocker_id = ? AND blocked_id = ?)
                   OR (blocker_id = ? AND blocked_id = ?)
                LIMIT 1
                """, firstUserId, secondUserId, secondUserId, firstUserId);
    }

    public boolean blockUser(int blockerId, int blockedId) {
        if (blockerId == blockedId) {
            return false;
        }

        return update("""
                INSERT OR IGNORE INTO User_Blocks (blocker_id, blocked_id)
                VALUES (?, ?)
                """, blockerId, blockedId);
    }

    public boolean unblockUser(int blockerId, int blockedId) {
        return update("""
                DELETE FROM User_Blocks
                WHERE blocker_id = ? AND blocked_id = ?
                """, blockerId, blockedId);
    }

    public boolean isCommunityBlocked(int userId, int communityId) {
        return exists("""
                SELECT 1 FROM Community_Blocks
                WHERE user_id = ? AND community_id = ?
                LIMIT 1
                """, userId, communityId);
    }

    public boolean blockCommunity(int userId, int communityId) {
        return update("""
                INSERT OR IGNORE INTO Community_Blocks (user_id, community_id)
                VALUES (?, ?)
                """, userId, communityId);
    }

    public boolean unblockCommunity(int userId, int communityId) {
        return update("""
                DELETE FROM Community_Blocks
                WHERE user_id = ? AND community_id = ?
                """, userId, communityId);
    }

    public List<BlockedUserRow> getBlockedUsers(int userId) {
        List<BlockedUserRow> users = new ArrayList<>();
        String sql = """
                SELECT u.user_id, u.username, up.display_name, ub.blocked_at
                FROM User_Blocks ub
                JOIN Users u ON ub.blocked_id = u.user_id
                LEFT JOIN User_Profiles up ON u.user_id = up.user_id
                WHERE ub.blocker_id = ?
                ORDER BY ub.blocked_at DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new BlockedUserRow(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("display_name"),
                            rs.getString("blocked_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Blocked users database error: " + e.getMessage());
        }

        return users;
    }

    public List<BlockedCommunityRow> getBlockedCommunities(int userId) {
        List<BlockedCommunityRow> communities = new ArrayList<>();
        String sql = """
                SELECT c.community_id, c.community_name, c.description, cb.blocked_at
                FROM Community_Blocks cb
                JOIN Communities c ON cb.community_id = c.community_id
                WHERE cb.user_id = ?
                ORDER BY cb.blocked_at DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    communities.add(new BlockedCommunityRow(
                            rs.getInt("community_id"),
                            rs.getString("community_name"),
                            rs.getString("description"),
                            rs.getString("blocked_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Blocked communities database error: " + e.getMessage());
        }

        return communities;
    }

    private boolean exists(String sql, int... values) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                stmt.setInt(i + 1, values[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Block check database error: " + e.getMessage());
        }
        return false;
    }

    private boolean update(String sql, int firstId, int secondId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, firstId);
            stmt.setInt(2, secondId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Block update database error: " + e.getMessage());
        }
        return false;
    }
}
