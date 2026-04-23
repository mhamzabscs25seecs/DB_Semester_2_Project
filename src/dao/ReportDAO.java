package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {
    public static class AdminReportRow {
        private final int reportId;
        private final String reporter;
        private final String targetType;
        private final int targetId;
        private final String targetTitle;
        private final String reason;
        private final String details;
        private final String status;
        private final String createdAt;

        public AdminReportRow(int reportId, String reporter, String targetType, int targetId,
                              String targetTitle, String reason, String details, String status,
                              String createdAt) {
            this.reportId = reportId;
            this.reporter = reporter;
            this.targetType = targetType;
            this.targetId = targetId;
            this.targetTitle = targetTitle;
            this.reason = reason;
            this.details = details;
            this.status = status;
            this.createdAt = createdAt;
        }

        public int getReportId() { return reportId; }
        public String getReporter() { return reporter; }
        public String getTargetType() { return targetType; }
        public int getTargetId() { return targetId; }
        public String getTargetTitle() { return targetTitle; }
        public String getReason() { return reason; }
        public String getDetails() { return details; }
        public String getStatus() { return status; }
        public String getCreatedAt() { return createdAt; }
    }

    public boolean reportPost(int reporterId, int postId, String reason, String details) {
        return createReport(reporterId, "post", postId, reason, details);
    }

    public boolean reportComment(int reporterId, int commentId, String reason, String details) {
        return createReport(reporterId, "comment", commentId, reason, details);
    }

    public boolean reportCommunity(int reporterId, int communityId, String reason, String details) {
        return createReport(reporterId, "community", communityId, reason, details);
    }

    private boolean createReport(int reporterId, String targetType, int targetId, String reason, String details) {
        String sql = """
                INSERT INTO Reports (reported_by, target_type, target_id, reason, details)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reporterId);
            stmt.setString(2, targetType);
            stmt.setInt(3, targetId);
            stmt.setString(4, reason);
            stmt.setString(5, details == null || details.isBlank() ? null : details.trim());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Create report database error: " + e.getMessage());
        }

        return false;
    }

    public List<AdminReportRow> getAdminReportRows() {
        return getReportRows(null);
    }

    public List<AdminReportRow> getReportsByUser(int userId) {
        return getReportRows(userId);
    }

    private List<AdminReportRow> getReportRows(Integer reporterId) {
        List<AdminReportRow> reports = new ArrayList<>();
        String sql = """
                SELECT
                    r.report_id,
                    u.username AS reporter,
                    r.target_type,
                    r.target_id,
                    CASE r.target_type
                        WHEN 'post' THEN p.title
                        WHEN 'comment' THEN SUBSTR(cm.comment_body, 1, 80)
                        WHEN 'community' THEN c.community_name
                    END AS target_title,
                    r.reason,
                    r.details,
                    r.status,
                    r.created_at
                FROM Reports r
                JOIN Users u ON r.reported_by = u.user_id
                LEFT JOIN Posts p ON r.target_type = 'post' AND r.target_id = p.post_id
                LEFT JOIN Comments cm ON r.target_type = 'comment' AND r.target_id = cm.comment_id
                LEFT JOIN Communities c ON r.target_type = 'community' AND r.target_id = c.community_id
                WHERE (? IS NULL OR r.reported_by = ?)
                ORDER BY r.created_at DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (reporterId == null) {
                stmt.setNull(1, java.sql.Types.INTEGER);
                stmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, reporterId);
                stmt.setInt(2, reporterId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(new AdminReportRow(
                            rs.getInt("report_id"),
                            rs.getString("reporter"),
                            rs.getString("target_type"),
                            rs.getInt("target_id"),
                            rs.getString("target_title"),
                            rs.getString("reason"),
                            rs.getString("details"),
                            rs.getString("status"),
                            rs.getString("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Reports database error: " + e.getMessage());
        }

        return reports;
    }
}
