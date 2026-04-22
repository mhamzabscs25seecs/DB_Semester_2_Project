package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {

    public static class FeedPost {
        private final int postId;
        private final String title;
        private final String author;
        private final String community;
        private final String createdAt;
        private final int score;
        private final int commentCount;

        public FeedPost(int postId, String title, String author, String community,
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
        List<FeedPost> posts = new ArrayList<>();

        String sql = """
                SELECT
                    p.post_id,
                    p.title,
                    u.username,
                    c.community_name,
                    p.created_at,
                    COALESCE(SUM(pv.vote_type), 0) AS score,
                    COUNT(DISTINCT cm.comment_id) AS comment_count
                FROM Posts p
                JOIN Users u ON p.posted_by = u.user_id
                JOIN Communities c ON p.community_id = c.community_id
                LEFT JOIN Post_Votes pv ON p.post_id = pv.post_id
                LEFT JOIN Comments cm ON p.post_id = cm.post_id
                GROUP BY p.post_id, p.title, u.username, c.community_name, p.created_at
                ORDER BY p.created_at DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                posts.add(new FeedPost(
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
            System.out.println("Feed posts database error: " + e.getMessage());
        }

        return posts;
    }
}
