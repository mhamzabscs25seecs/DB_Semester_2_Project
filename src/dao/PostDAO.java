package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {

    public static class FeedPost {
        private final int postId;
        private final int authorId;
        private final int communityId;
        private final String title;
        private final String body;
        private final String author;
        private final String community;
        private final String createdAt;
        private final int score;
        private final int commentCount;

        public FeedPost(int postId, int authorId, int communityId, String title, String body, String author, String community,
                        String createdAt, int score, int commentCount) {
            this.postId = postId;
            this.authorId = authorId;
            this.communityId = communityId;
            this.title = title;
            this.body = body;
            this.author = author;
            this.community = community;
            this.createdAt = createdAt;
            this.score = score;
            this.commentCount = commentCount;
        }

        public int getPostId() { return postId; }
        public int getAuthorId() { return authorId; }
        public int getCommunityId() { return communityId; }
        public String getTitle() { return title; }
        public String getBody() { return body; }
        public String getAuthor() { return author; }
        public String getCommunity() { return community; }
        public String getCreatedAt() { return createdAt; }
        public int getScore() { return score; }
        public int getCommentCount() { return commentCount; }
    }

    public static class CommunityOption {
        private final int communityId;
        private final String communityName;

        public CommunityOption(int communityId, String communityName) {
            this.communityId = communityId;
            this.communityName = communityName;
        }

        public int getCommunityId() { return communityId; }
        public String getCommunityName() { return communityName; }

        @Override
        public String toString() {
            return "r/" + communityName;
        }
    }

    public static class AdminPostRow {
        private final int postId;
        private final String title;
        private final String author;
        private final String community;
        private final String createdAt;
        private final int score;
        private final int commentCount;

        public AdminPostRow(int postId, String title, String author, String community,
                            String createdAt, int score, int commentCount) {
            this.postId = postId;
            this.title = title;
            this.author = author;
            this.community = community;
            this.createdAt = createdAt;
            this.score = score;
            this.commentCount = commentCount;
        }

        public int getPostId() { return postId; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getCommunity() { return community; }
        public String getCreatedAt() { return createdAt; }
        public int getScore() { return score; }
        public int getCommentCount() { return commentCount; }
    }

    public List<FeedPost> getFeedPosts() {
        return getFeedPosts(null);
    }

    public List<FeedPost> getFeedPosts(Integer communityId) {
        List<FeedPost> posts = new ArrayList<>();

        String sql = """
                SELECT
                    p.post_id,
                    p.posted_by,
                    p.community_id,
                    p.title,
                    p.body,
                    u.username,
                    c.community_name,
                    p.created_at,
                    COALESCE(v.score, 0) AS score,
                    COALESCE(cc.comment_count, 0) AS comment_count
                FROM Posts p
                JOIN Users u ON p.posted_by = u.user_id
                JOIN Communities c ON p.community_id = c.community_id
                LEFT JOIN (
                    SELECT post_id, SUM(vote_type) AS score
                    FROM Post_Votes
                    GROUP BY post_id
                ) v ON p.post_id = v.post_id
                LEFT JOIN (
                    SELECT post_id, COUNT(*) AS comment_count
                    FROM Comments
                    GROUP BY post_id
                ) cc ON p.post_id = cc.post_id
                WHERE (? IS NULL OR p.community_id = ?)
                ORDER BY p.created_at DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (communityId == null) {
                stmt.setNull(1, java.sql.Types.INTEGER);
                stmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, communityId);
                stmt.setInt(2, communityId);
            }

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    posts.add(new FeedPost(
                            rs.getInt("post_id"),
                            rs.getInt("posted_by"),
                            rs.getInt("community_id"),
                            rs.getString("title"),
                            rs.getString("body"),
                            rs.getString("username"),
                            rs.getString("community_name"),
                            rs.getString("created_at"),
                            rs.getInt("score"),
                            rs.getInt("comment_count")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Feed posts database error: " + e.getMessage());
        }

        return posts;
    }

    public List<CommunityOption> getCommunities() {
        List<CommunityOption> communities = new ArrayList<>();

        String sql = """
                SELECT community_id, community_name
                FROM Communities
                ORDER BY community_name ASC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                communities.add(new CommunityOption(
                        rs.getInt("community_id"),
                        rs.getString("community_name")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Communities database error: " + e.getMessage());
        }

        return communities;
    }

    public int createPost(int userId, int communityId, String title, String body) {
        String sql = """
                INSERT INTO Posts (posted_by, community_id, title, body)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, communityId);
            stmt.setString(3, title);
            stmt.setString(4, body);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("Create post database error: " + e.getMessage());
        }

        return 0;
    }

    public List<AdminPostRow> getAdminPostRows() {
        List<AdminPostRow> posts = new ArrayList<>();
        String sql = """
                SELECT
                    p.post_id,
                    p.title,
                    u.username,
                    c.community_name,
                    p.created_at,
                    COALESCE(v.score, 0) AS score,
                    COALESCE(cc.comment_count, 0) AS comment_count
                FROM Posts p
                JOIN Users u ON p.posted_by = u.user_id
                JOIN Communities c ON p.community_id = c.community_id
                LEFT JOIN (
                    SELECT post_id, SUM(vote_type) AS score
                    FROM Post_Votes
                    GROUP BY post_id
                ) v ON p.post_id = v.post_id
                LEFT JOIN (
                    SELECT post_id, COUNT(*) AS comment_count
                    FROM Comments
                    GROUP BY post_id
                ) cc ON p.post_id = cc.post_id
                ORDER BY p.created_at DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                posts.add(new AdminPostRow(
                        rs.getInt("post_id"),
                        rs.getString("title"),
                        rs.getString("username"),
                        rs.getString("community_name"),
                        rs.getString("created_at"),
                        rs.getInt("score"),
                        rs.getInt("comment_count")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Admin posts database error: " + e.getMessage());
        }

        return posts;
    }

    public boolean deletePostByAdmin(int adminUserId, int postId) {
        if (!new UserDAO().isAdmin(adminUserId)) {
            return false;
        }

        String sql = """
                DELETE FROM Posts
                WHERE post_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Admin delete post database error: " + e.getMessage());
        }

        return false;
    }
}
