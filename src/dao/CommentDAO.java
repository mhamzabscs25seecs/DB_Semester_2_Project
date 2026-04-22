package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {

    public static class CommentRecord {
        private final int commentId;
        private final int parentCommentId;
        private final String author;
        private final String body;
        private final String commentedAt;
        private final int score;

        public CommentRecord(int commentId, int parentCommentId, String author,
                             String body, String commentedAt, int score) {
            this.commentId = commentId;
            this.parentCommentId = parentCommentId;
            this.author = author;
            this.body = body;
            this.commentedAt = commentedAt;
            this.score = score;
        }

        public int getCommentId() { return commentId; }
        public int getParentCommentId() { return parentCommentId; }
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
                    u.username,
                    cm.comment_body,
                    cm.commented_at,
                    COALESCE(SUM(cv.comment_vote_type), 0) AS score
                FROM Comments cm
                JOIN Users u ON cm.commenter_id = u.user_id
                LEFT JOIN Comment_Votes cv ON cm.comment_id = cv.comment_id
                WHERE cm.post_id = ?
                GROUP BY cm.comment_id, cm.parent_comment_id, u.username, cm.comment_body, cm.commented_at
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
}
