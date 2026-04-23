package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommunityDAO {
    public static class CommunitySummary {
        private final int communityId;
        private final String communityName;
        private final String description;
        private final String createdAt;
        private final int memberCount;
        private final int postCount;
        private final boolean joined;

        public CommunitySummary(int communityId, String communityName, String description, String createdAt,
                                int memberCount, int postCount, boolean joined) {
            this.communityId = communityId;
            this.communityName = communityName;
            this.description = description;
            this.createdAt = createdAt;
            this.memberCount = memberCount;
            this.postCount = postCount;
            this.joined = joined;
        }

        public int getCommunityId() { return communityId; }
        public String getCommunityName() { return communityName; }
        public String getDescription() { return description; }
        public String getCreatedAt() { return createdAt; }
        public int getMemberCount() { return memberCount; }
        public int getPostCount() { return postCount; }
        public boolean isJoined() { return joined; }
    }

    public boolean deleteCommunityByAdmin(int adminUserId, int communityId) {
        if (!new UserDAO().isAdmin(adminUserId)) {
            return false;
        }

        String sql = """
                DELETE FROM Communities
                WHERE community_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, communityId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Admin delete community Database error: " + e.getMessage());
        }

        return false;
    }

    // The person making a community cutomatically becomes a member of the
    // Community, So actually we going to execute two INSERT statements
    // One into the Communities and the other one into the Community_Membership

    public int createCommunity(int creatorId, String communityName, String communityDescription) {
        String normalizedName = communityName == null ? "" : communityName.trim();
        if (normalizedName.isEmpty() || communityNameExists(normalizedName)) {
            return 0;
        }

        String insertCommunity = """
                INSERT INTO Communities (community_name, description, created_by)
                VALUES (?, ?, ?)
                """;

        String insertMembership = """
            INSERT INTO Community_Membership (user_id, community_id)
            VALUES (?, ?)
            """;

        try (Connection conn = DBConnection.getConnection() ) {
            conn.setAutoCommit(false);

            try (
                    PreparedStatement communityStmt = conn.prepareStatement(insertCommunity, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement membershipStmt = conn.prepareStatement(insertMembership)) {
                communityStmt.setString(1, normalizedName);
                communityStmt.setString(2, communityDescription == null || communityDescription.isBlank() ? null : communityDescription);
                communityStmt.setInt(3, creatorId);

                communityStmt.executeUpdate();

                int communityId;
                try (ResultSet rs = communityStmt.getGeneratedKeys()) {
                    if (!rs.next()) {
                        // If if condition is true it indirectly means that the
                        // insert statement for the Community Table failed
                        conn.rollback();
                        return 0;
                    }

                    communityId = rs.getInt(1);
                }

                // If by now the method hasnt return back to the caller function, it means
                // that the Community Table's INSERT query worked. So now it should execute the
                // INSERT Query for the Community_Membership Table

                membershipStmt.setInt(1, creatorId);
                membershipStmt.setInt(2, communityId);

                membershipStmt.executeUpdate();

                conn.commit();
                return communityId;
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Create community Database error: " + e.getMessage());


            } finally {
                conn.setAutoCommit(true);
            }
        }
        catch (SQLException e) {
            System.out.println("Create community Database error: " + e.getMessage());
        }

        return 0;
    }

    public boolean communityNameExists(String communityName) {
        String sql = """
                SELECT 1
                FROM Communities
                WHERE LOWER(TRIM(community_name)) = LOWER(TRIM(?))
                LIMIT 1
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, communityName == null ? "" : communityName.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Community name check Database error: " + e.getMessage());
            return false;
        }
    }

    public List<CommunitySummary> getCommunitySummaries(int userId) {
        List<CommunitySummary> communities = new ArrayList<>();
        String sql = """
                SELECT
                    c.community_id,
                    c.community_name,
                    c.description,
                    c.created_at,
                    COALESCE(m.member_count, 0) AS member_count,
                    COALESCE(p.post_count, 0) AS post_count,
                    CASE WHEN cm.user_id IS NULL THEN 0 ELSE 1 END AS joined
                FROM Communities c
                LEFT JOIN (
                    SELECT community_id, COUNT(*) AS member_count
                    FROM Community_Membership
                    GROUP BY community_id
                ) m ON c.community_id = m.community_id
                LEFT JOIN (
                    SELECT community_id, COUNT(*) AS post_count
                    FROM Posts
                    GROUP BY community_id
                ) p ON c.community_id = p.community_id
                LEFT JOIN Community_Membership cm
                    ON c.community_id = cm.community_id AND cm.user_id = ?
                ORDER BY c.community_name ASC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    communities.add(new CommunitySummary(
                            rs.getInt("community_id"),
                            rs.getString("community_name"),
                            rs.getString("description"),
                            rs.getString("created_at"),
                            rs.getInt("member_count"),
                            rs.getInt("post_count"),
                            rs.getInt("joined") == 1
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Community summary Database error: " + e.getMessage());
        }

        return communities;
    }

    public boolean joinCommunity(int userId, int communityId) {
        String sql = """
                INSERT OR IGNORE INTO Community_Membership (user_id, community_id)
                VALUES (?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, communityId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Join community Database error: " + e.getMessage());
            return false;
        }
    }

    public boolean leaveCommunity(int userId, int communityId) {
        String sql = """
                DELETE FROM Community_Membership
                WHERE user_id = ? AND community_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, communityId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Leave community Database error: " + e.getMessage());
            return false;
        }
    }

}
