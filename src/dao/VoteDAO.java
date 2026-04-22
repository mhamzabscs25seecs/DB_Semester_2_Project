package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VoteDAO {

    public int votePost(int postId, int userId, int voteType) {
        if (getPostUserVote(postId, userId) == voteType) {
            clearPostVote(postId, userId);
            return getPostScore(postId);
        }

        String sql = """
                INSERT INTO Post_Votes (post_id, user_id, vote_type)
                VALUES (?, ?, ?)
                ON CONFLICT(user_id, post_id)
                DO UPDATE SET vote_type = excluded.vote_type
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            stmt.setInt(3, voteType);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Post vote database error: " + e.getMessage());
        }

        return getPostScore(postId);
    }

    public int getPostUserVote(int postId, int userId) {
        String sql = """
                SELECT vote_type
                FROM Post_Votes
                WHERE post_id = ? AND user_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("vote_type");
                }
            }

        } catch (SQLException e) {
            System.out.println("Post user vote database error: " + e.getMessage());
        }

        return 0;
    }

    private void clearPostVote(int postId, int userId) {
        String sql = """
                DELETE FROM Post_Votes
                WHERE post_id = ? AND user_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Clear post vote database error: " + e.getMessage());
        }
    }

    public int getPostScore(int postId) {
        String sql = """
                SELECT COALESCE(SUM(vote_type), 0) AS score
                FROM Post_Votes
                WHERE post_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("score");
                }
            }

        } catch (SQLException e) {
            System.out.println("Post score database error: " + e.getMessage());
        }

        return 0;
    }

    public int voteComment(int commentId, int userId, int voteType) {
        if (getCommentUserVote(commentId, userId) == voteType) {
            clearCommentVote(commentId, userId);
            return getCommentScore(commentId);
        }

        String sql = """
                INSERT INTO Comment_Votes (comment_id, comment_voter_id, comment_vote_type)
                VALUES (?, ?, ?)
                ON CONFLICT(comment_voter_id, comment_id)
                DO UPDATE SET comment_vote_type = excluded.comment_vote_type
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            stmt.setInt(2, userId);
            stmt.setInt(3, voteType);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Comment vote database error: " + e.getMessage());
        }

        return getCommentScore(commentId);
    }

    public int getCommentUserVote(int commentId, int userId) {
        String sql = """
                SELECT comment_vote_type
                FROM Comment_Votes
                WHERE comment_id = ? AND comment_voter_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("comment_vote_type");
                }
            }

        } catch (SQLException e) {
            System.out.println("Comment user vote database error: " + e.getMessage());
        }

        return 0;
    }

    private void clearCommentVote(int commentId, int userId) {
        String sql = """
                DELETE FROM Comment_Votes
                WHERE comment_id = ? AND comment_voter_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Clear comment vote database error: " + e.getMessage());
        }
    }

    public int getCommentScore(int commentId) {
        String sql = """
                SELECT COALESCE(SUM(comment_vote_type), 0) AS score
                FROM Comment_Votes
                WHERE comment_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("score");
                }
            }

        } catch (SQLException e) {
            System.out.println("Comment score database error: " + e.getMessage());
        }

        return 0;
    }
}
