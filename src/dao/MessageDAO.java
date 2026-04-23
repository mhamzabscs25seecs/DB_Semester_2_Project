package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public static class ChatUser {
        private final int userId;
        private final String username;
        private final String displayName;
        private final String lastMessage;
        private final String lastSentAt;
        private final int unreadCount;

        public ChatUser(int userId, String username, String displayName,
                        String lastMessage, String lastSentAt, int unreadCount) {
            this.userId = userId;
            this.username = username;
            this.displayName = displayName;
            this.lastMessage = lastMessage;
            this.lastSentAt = lastSentAt;
            this.unreadCount = unreadCount;
        }

        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getDisplayName() { return displayName; }
        public String getLastMessage() { return lastMessage; }
        public String getLastSentAt() { return lastSentAt; }
        public int getUnreadCount() { return unreadCount; }
    }

    public static class ChatMessage {
        private final int messageId;
        private final int senderId;
        private final String senderUsername;
        private final String body;
        private final String sentAt;

        public ChatMessage(int messageId, int senderId, String senderUsername, String body, String sentAt) {
            this.messageId = messageId;
            this.senderId = senderId;
            this.senderUsername = senderUsername;
            this.body = body;
            this.sentAt = sentAt;
        }

        public int getMessageId() { return messageId; }
        public int getSenderId() { return senderId; }
        public String getSenderUsername() { return senderUsername; }
        public String getBody() { return body; }
        public String getSentAt() { return sentAt; }
    }

    public List<ChatUser> searchChatUsers(int currentUserId, String query) {
        List<ChatUser> users = new ArrayList<>();
        String like = "%" + (query == null ? "" : query.trim()) + "%";
        String sql = """
                SELECT
                    u.user_id,
                    u.username,
                    up.display_name,
                    (
                        SELECT m.message_body
                        FROM Messages m
                        WHERE (m.sender_id = ? AND m.recipient_id = u.user_id)
                           OR (m.sender_id = u.user_id AND m.recipient_id = ?)
                        ORDER BY m.sent_at DESC, m.message_id DESC
                        LIMIT 1
                    ) AS last_message,
                    (
                        SELECT m.sent_at
                        FROM Messages m
                        WHERE (m.sender_id = ? AND m.recipient_id = u.user_id)
                           OR (m.sender_id = u.user_id AND m.recipient_id = ?)
                        ORDER BY m.sent_at DESC, m.message_id DESC
                        LIMIT 1
                    ) AS last_sent_at,
                    (
                        SELECT COUNT(*)
                        FROM Messages m
                        WHERE m.sender_id = u.user_id
                          AND m.recipient_id = ?
                          AND m.read_at IS NULL
                    ) AS unread_count
                FROM Users u
                LEFT JOIN User_Profiles up ON u.user_id = up.user_id
                WHERE u.user_id <> ?
                  AND (? = '' OR u.username LIKE ? OR COALESCE(up.display_name, '') LIKE ?)
                ORDER BY
                    CASE WHEN last_sent_at IS NULL THEN 1 ELSE 0 END,
                    last_sent_at DESC,
                    u.username ASC
                LIMIT 50
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            stmt.setInt(3, currentUserId);
            stmt.setInt(4, currentUserId);
            stmt.setInt(5, currentUserId);
            stmt.setInt(6, currentUserId);
            stmt.setString(7, query == null ? "" : query.trim());
            stmt.setString(8, like);
            stmt.setString(9, like);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new ChatUser(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("display_name"),
                            rs.getString("last_message"),
                            rs.getString("last_sent_at"),
                            rs.getInt("unread_count")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Chat users database error: " + e.getMessage());
        }

        return users;
    }

    public List<ChatMessage> getConversation(int currentUserId, int otherUserId) {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = """
                SELECT m.message_id, m.sender_id, u.username AS sender_username, m.message_body, m.sent_at
                FROM Messages m
                JOIN Users u ON m.sender_id = u.user_id
                WHERE (m.sender_id = ? AND m.recipient_id = ?)
                   OR (m.sender_id = ? AND m.recipient_id = ?)
                ORDER BY m.sent_at ASC, m.message_id ASC
                LIMIT 300
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);
            stmt.setInt(2, otherUserId);
            stmt.setInt(3, otherUserId);
            stmt.setInt(4, currentUserId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(new ChatMessage(
                            rs.getInt("message_id"),
                            rs.getInt("sender_id"),
                            rs.getString("sender_username"),
                            rs.getString("message_body"),
                            rs.getString("sent_at")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Conversation database error: " + e.getMessage());
        }

        return messages;
    }

    public boolean sendMessage(int senderId, int recipientId, String body) {
        if (senderId == recipientId || body == null || body.trim().isEmpty()) {
            return false;
        }

        String sql = """
                INSERT INTO Messages (sender_id, recipient_id, message_body)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, senderId);
            stmt.setInt(2, recipientId);
            stmt.setString(3, body.trim());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Send message database error: " + e.getMessage());
        }

        return false;
    }

    public void markConversationRead(int currentUserId, int otherUserId) {
        String sql = """
                UPDATE Messages
                SET read_at = CURRENT_TIMESTAMP
                WHERE recipient_id = ? AND sender_id = ? AND read_at IS NULL
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);
            stmt.setInt(2, otherUserId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Mark messages read database error: " + e.getMessage());
        }
    }
}
