package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {
    public static class UserCommentRow {
        private final int commentId;
        private final int postId;
        private final String postTitle;
        private final String community;
        private final String body;
        private final String commentedAt;
        private final int score;

        public UserCommentRow(int commentId, int postId, String postTitle, String community,
                              String body, String commentedAt, int score) {
            this.commentId = commentId;
            this.postId = postId;
            this.postTitle = postTitle;
            this.community = community;
            this.body = body;
            this.commentedAt = commentedAt;
            this.score = score;
        }

        public int getCommentId() { return commentId; }
        public int getPostId() { return postId; }
        public String getPostTitle() { return postTitle; }
        public String getCommunity() { return community; }
        public String getBody() { return body; }
        public String getCommentedAt() { return commentedAt; }
        public int getScore() { return score; }
    }

    public static class CommentRecord {
        private final int commentId;
        private final int parentCommentId;
        private final int authorId;
        private final String author;
        private final String body;
        private final String commentedAt;
        private final int score;

        public CommentRecord(int commentId, int parentCommentId, int authorId, String author,
                             String body, String commentedAt, int score) {
            this.commentId = commentId;
            this.parentCommentId = parentCommentId;
            this.authorId = authorId;
            this.author = author;
            this.body = body;
            this.commentedAt = commentedAt;
            this.score = score;
        }

        public int getCommentId() { return commentId; }
        public int getParentCommentId() { return parentCommentId; }
        public int getAuthorId() { return authorId; }
        public String getAuthor() { return author; }
        public String getBody() { return body; }
        public String getCommentedAt() { return commentedAt; }
        public int getScore() { return score; }
    }

    public List<CommentRecord> getCommentsForPost(int postId) {
        List<CommentRecord> comments = new ArrayList<>();

        String sql = """
                SELECT
                    cm.comment_id,
                    COALESCE(cm.parent_comment_id, 0) AS parent_comment_id,
                    cm.commenter_id,
                    u.username,
                    cm.comment_body,
                    cm.commented_at,
                    COALESCE(SUM(cv.comment_vote_type), 0) AS score
                FROM Comments cm
                JOIN Users u ON cm.commenter_id = u.user_id
                LEFT JOIN Comment_Votes cv ON cm.comment_id = cv.comment_id
                WHERE cm.post_id = ?
                GROUP BY cm.comment_id, cm.parent_comment_id, cm.commenter_id, u.username, cm.comment_body, cm.commented_at
                ORDER BY cm.commented_at ASC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(new CommentRecord(
                            rs.getInt("comment_id"),
                            rs.getInt("parent_comment_id"),
                            rs.getInt("commenter_id"),
                            rs.getString("username"),
                            rs.getString("comment_body"),
                            rs.getString("commented_at"),
                            rs.getInt("score")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Comments database error: " + e.getMessage());
        }

        return comments;
    }

    public int addComment(int postId, int userId, String body, Integer parentCommentId) {
        String sql = """
                INSERT INTO Comments (post_id, commenter_id, comment_body, parent_comment_id)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            stmt.setString(3, body);

            if (parentCommentId == null) {
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(4, parentCommentId);
            }

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("Add comment database error: " + e.getMessage());
        }

        return 0;
    }

    public boolean deleteComment(int commentId, int userId) {
        String sql = """
                DELETE FROM Comments
                WHERE comment_id = ?
                  AND (
                        commenter_id = ?
                        OR EXISTS (
                            SELECT 1
                            FROM Users
                            WHERE user_id = ? AND role = 'admin'
                        )
                  )
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Delete comment database error: " + e.getMessage());
        }

        return false;
    }

    public List<UserCommentRow> getCommentsByUser(int userId) {
        List<UserCommentRow> comments = new ArrayList<>();
        String sql = """
                SELECT
                    cm.comment_id,
                    cm.post_id,
                    p.title,
                    c.community_name,
                    cm.comment_body,
                    cm.commented_at,
                    COALESCE(SUM(cv.comment_vote_type), 0) AS score
                FROM Comments cm
                JOIN Posts p ON cm.post_id = p.post_id
                JOIN Communities c ON p.community_id = c.community_id
                LEFT JOIN Comment_Votes cv ON cm.comment_id = cv.comment_id
                WHERE cm.commenter_id = ?
                GROUP BY cm.comment_id, cm.post_id, p.title, c.community_name, cm.comment_body, cm.commented_at
                ORDER BY cm.commented_at DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(new UserCommentRow(
                            rs.getInt("comment_id"),
                            rs.getInt("post_id"),
                            rs.getString("title"),
                            rs.getString("community_name"),
                            rs.getString("comment_body"),
                            rs.getString("commented_at"),
                            rs.getInt("score")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("User comments database error: " + e.getMessage());
        }

        return comments;
    }

    public List<UserCommentRow> getAdminCommentRows() {
        List<UserCommentRow> comments = new ArrayList<>();
        String sql = """
                SELECT
                    cm.comment_id,
                    cm.post_id,
                    p.title,
                    c.community_name,
                    u.username || ': ' || cm.comment_body AS comment_body,
                    cm.commented_at,
                    COALESCE(SUM(cv.comment_vote_type), 0) AS score
                FROM Comments cm
                JOIN Users u ON cm.commenter_id = u.user_id
                JOIN Posts p ON cm.post_id = p.post_id
                JOIN Communities c ON p.community_id = c.community_id
                LEFT JOIN Comment_Votes cv ON cm.comment_id = cv.comment_id
                GROUP BY cm.comment_id, cm.post_id, p.title, c.community_name, u.username, cm.comment_body, cm.commented_at
                ORDER BY cm.commented_at DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                comments.add(new UserCommentRow(
                        rs.getInt("comment_id"),
                        rs.getInt("post_id"),
                        rs.getString("title"),
                        rs.getString("community_name"),
                        rs.getString("comment_body"),
                        rs.getString("commented_at"),
                        rs.getInt("score")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Admin comments database error: " + e.getMessage());
        }

        return comments;
    }
}
