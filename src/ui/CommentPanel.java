package ui;

import dao.Session;
import dao.ReportDAO;
import dao.UserDAO;
import dao.UserFollowDAO;
import dao.VoteDAO;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import static ui.LoginScreen.*;

/**
 * CommentPanel renders a single comment node with its children indented below it.
 * Use CommentPanel.buildTree(comments) to get a full scrollable panel.
 */
public class CommentPanel extends JPanel {

    private static final int INDENT_PX = 18;

    public interface ReplyHandler {
        boolean saveReply(int parentCommentId, String text);
    }

    public interface DeleteHandler {
        boolean deleteComment(int commentId);
    }

    // ── Comment DTO ───────────────────────────────────────────────────────────
    public static class CommentData {
        public int    id, parentId, authorId, score;
        public String author, body, timestamp;
        public List<CommentData> children = new ArrayList<>();

        public CommentData(int id, int parentId, int authorId, String author, String body, String timestamp, int score) {
            this.id = id; this.parentId = parentId; this.authorId = authorId; this.author = author;
            this.body = body; this.timestamp = timestamp; this.score = score;
        }
    }

    // ── Build full tree panel ──────────────────────────────────────────────
    public static JScrollPane buildTree(List<CommentData> flat, String currentUser,
                                        ReplyHandler onReply, DeleteHandler onDelete) {
        List<CommentData> roots = buildTree(flat);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(BG_DEEP);
        container.setBorder(new EmptyBorder(8, 0, 8, 0));

        JLabel header = new JLabel(flat.size() + " Comments");
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setForeground(NEON_PINK);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(header);

        for (CommentData root : roots) {
            container.add(new CommentPanel(root, 0, currentUser, onReply, onDelete));
            container.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.setBackground(BG_DEEP);
        scroll.getViewport().setBackground(BG_DEEP);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = new Color(NEON_PINK.getRed(), NEON_PINK.getGreen(), NEON_PINK.getBlue(), 80);
                this.trackColor = BG_DEEP;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
            }
        });

        return scroll;
    }

    // ── Single comment node ────────────────────────────────────────────────
    private final CommentData data;
    private final int depth;
    private final String currentUser;
    private final ReplyHandler onReply;
    private final DeleteHandler onDelete;
    private boolean collapsed = false;
    private JPanel childrenPanel;

    public CommentPanel(CommentData data, int depth, String currentUser,
                        ReplyHandler onReply, DeleteHandler onDelete) {
        this.data = data;
        this.depth = depth;
        this.currentUser = currentUser;
        this.onReply = onReply;
        this.onDelete = onDelete;
        buildNode();
    }

    private void buildNode() {
        setLayout(new BorderLayout(0, 4));
        setOpaque(false);

        if (depth > 0) {
            JPanel indentBar = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Neon thread lines cycling through cyberpunk colors
                    Color[] lineColors = {
                        new Color(NEON_PINK.getRed(), NEON_PINK.getGreen(), NEON_PINK.getBlue(), 100),
                        new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 100),
                        new Color(NEON_MID.getRed(),  NEON_MID.getGreen(),  NEON_MID.getBlue(),  100),
                        new Color(GOLD.getRed(),       GOLD.getGreen(),       GOLD.getBlue(),       80)
                    };
                    g2.setColor(lineColors[Math.min(depth - 1, lineColors.length - 1)]);
                    int x = getWidth() / 2;
                    g2.fillRoundRect(x - 1, 0, 2, getHeight(), 2, 2);
                }
            };
            indentBar.setOpaque(false);
            indentBar.setPreferredSize(new Dimension(INDENT_PX, 0));
            add(indentBar, BorderLayout.WEST);
        }

        add(buildCommentBlock(), BorderLayout.CENTER);
    }

    private JPanel buildCommentBlock() {
        JPanel outer = new JPanel(new BorderLayout(0, 4));
        outer.setOpaque(false);

        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(commentCardColor());
        card.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(commentBorderColor(), 1),
            new EmptyBorder(10, 12, 10, 12)
        ));

        // Author row
        JPanel authorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        authorRow.setOpaque(false);

        // Avatar
        JLabel avatar = new JLabel(data.author.substring(0, Math.min(2, data.author.length())).toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(NEON_PINK.getRed(), NEON_PINK.getGreen(), NEON_PINK.getBlue(), 120));
                g2.setStroke(new BasicStroke(1f));
                g2.drawOval(0, 0, getWidth()-1, getHeight()-1);
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(22, 22));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 10));
        avatar.setForeground(NEON_CYAN);
        avatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel authorLabel = new JLabel(data.author);
        authorLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        authorLabel.setForeground(data.author.equals(currentUser) ? GOLD : NEON_MID);
        authorLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseAdapter openProfile = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                SoundFX.click();
                showProfileDialog();
            }
            public void mouseEntered(MouseEvent e) { authorLabel.setForeground(NEON_CYAN); }
            public void mouseExited(MouseEvent e)  {
                authorLabel.setForeground(data.author.equals(currentUser) ? GOLD : NEON_MID);
            }
        };
        avatar.addMouseListener(openProfile);
        authorLabel.addMouseListener(openProfile);

        JLabel scoreLabel = new JLabel("▲ " + data.score);
        scoreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        scoreLabel.setForeground(data.score > 50 ? NEON_CYAN : TEXT_MUTED);

        JLabel timeLabel = new JLabel("· " + data.timestamp);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        timeLabel.setForeground(NEON_DIM);

        JLabel collapseBtn = new JLabel(data.children.isEmpty() ? "" : "[–]");
        collapseBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        collapseBtn.setForeground(NEON_DIM);
        collapseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        collapseBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { toggleCollapse(collapseBtn); }
            public void mouseEntered(MouseEvent e) { collapseBtn.setForeground(NEON_PINK); }
            public void mouseExited(MouseEvent e)  { collapseBtn.setForeground(NEON_DIM); }
        });

        authorRow.add(avatar);
        authorRow.add(authorLabel);
        authorRow.add(scoreLabel);
        authorRow.add(timeLabel);
        authorRow.add(Box.createHorizontalStrut(8));
        authorRow.add(collapseBtn);

        // Body
        JLabel bodyLabel = new JLabel("<html><body style='width:480px; color:" + htmlColor(TEXT_MUTED) + "; font-family:Segoe UI; font-size:14px; line-height:1.6'>"
            + data.body + "</body></html>");
        bodyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.setOpaque(false);

        JButton upBtn    = makeActionBtn("▲ Upvote");
        JButton downBtn  = makeActionBtn("▼");
        JButton replyBtn = makeActionBtn("↩ Reply");
        JButton reportBtn = makeActionBtn("⚑ Report");
        JButton deleteBtn = makeDeleteBtn("Delete");
        final int[] currentVote = {
                Session.isLoggedIn() ? new VoteDAO().getCommentUserVote(data.id, Session.getCurrentUserId()) : 0
        };
        updateCommentVoteButtons(upBtn, downBtn, scoreLabel, currentVote[0]);

        upBtn.addActionListener(e -> {
            if (!Session.isLoggedIn()) {
                JOptionPane.showMessageDialog(this, "Please log in first.", "Clixky", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int newScore = new VoteDAO().voteComment(data.id, Session.getCurrentUserId(), 1);
            currentVote[0] = currentVote[0] == 1 ? 0 : 1;
            data.score = newScore;
            scoreLabel.setText("▲ " + newScore);
            updateCommentVoteButtons(upBtn, downBtn, scoreLabel, currentVote[0]);
        });
        downBtn.addActionListener(e -> {
            if (!Session.isLoggedIn()) {
                JOptionPane.showMessageDialog(this, "Please log in first.", "Clixky", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int newScore = new VoteDAO().voteComment(data.id, Session.getCurrentUserId(), -1);
            currentVote[0] = currentVote[0] == -1 ? 0 : -1;
            data.score = newScore;
            scoreLabel.setText("▲ " + newScore);
            updateCommentVoteButtons(upBtn, downBtn, scoreLabel, currentVote[0]);
        });
        replyBtn.addActionListener(e -> showReplyComposer(outer));
        reportBtn.addActionListener(e -> reportComment());
        deleteBtn.addActionListener(e -> handleDelete());

        actions.add(upBtn);
        actions.add(downBtn);
        actions.add(replyBtn);
        actions.add(reportBtn);
        if (canDeleteComment()) {
            actions.add(deleteBtn);
        }

        card.add(authorRow, BorderLayout.NORTH);
        card.add(bodyLabel, BorderLayout.CENTER);
        card.add(actions,   BorderLayout.SOUTH);

        outer.add(card, BorderLayout.NORTH);

        childrenPanel = new JPanel();
        childrenPanel.setLayout(new BoxLayout(childrenPanel, BoxLayout.Y_AXIS));
        childrenPanel.setOpaque(false);
        childrenPanel.setBorder(new EmptyBorder(4, 0, 0, 0));

        for (CommentData child : data.children) {
            childrenPanel.add(new CommentPanel(child, depth + 1, currentUser, onReply, onDelete));
            childrenPanel.add(Box.createVerticalStrut(5));
        }

        if (!data.children.isEmpty()) {
            outer.add(childrenPanel, BorderLayout.CENTER);
        }

        return outer;
    }

    private boolean isOwnComment() {
        return data.author.equals(currentUser) || data.author.equals("u/" + currentUser);
    }

    private boolean canDeleteComment() {
        return isOwnComment() || Session.isAdmin();
    }

    private void showProfileDialog() {
        UserDAO.UserProfile profile = new UserDAO().getProfileById(data.authorId);
        if (profile == null) {
            JOptionPane.showMessageDialog(this, "Profile data could not be loaded.", "Clixky", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Clixky Profile", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(430, 640);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setUndecorated(true);

        JPanel card = new RoundPanel(14, BG_PANEL, BORDER_COL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel avatar = new JLabel(profile.getUsername().substring(0, 1).toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(NEON_PINK);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(1, 1, getWidth() - 2, getHeight() - 2);
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(54, 54));
        avatar.setMaximumSize(new Dimension(54, 54));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 20));
        avatar.setForeground(NEON_CYAN);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel(nullToFallback(profile.getDisplayName(), profile.getUsername()));
        name.setFont(new Font("Segoe UI", Font.BOLD, 18));
        name.setForeground(NEON_PINK);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel username = new JLabel("@" + profile.getUsername());
        username.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        username.setForeground(NEON_MID);
        username.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton close = makeButton("CLOSE", BG_CARD, NEON_CYAN, BORDER_COL);
        close.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        close.addActionListener(e -> dialog.dispose());

        card.add(avatar);
        card.add(Box.createVerticalStrut(10));
        card.add(name);
        card.add(Box.createVerticalStrut(4));
        card.add(username);
        card.add(Box.createVerticalStrut(18));
        card.add(profileRow("BIO", nullToFallback(profile.getBioText(), "No bio yet.")));
        card.add(profileRow("COUNTRY", nullToFallback(profile.getCountry(), "Not set")));
        card.add(profileRow("BIRTH YEAR", String.valueOf(profile.getBirthYear())));
        card.add(profileRow("PRIVACY", profile.isPrivate() ? "Private" : "Public"));
        card.add(Box.createVerticalStrut(12));
        card.add(new FollowingPanel(profile.getUserId()));
        card.add(Box.createVerticalStrut(12));
        card.add(buildFollowAction(profile));
        card.add(Box.createVerticalStrut(8));
        card.add(close);

        dialog.setContentPane(card);
        dialog.setVisible(true);
    }

    private JButton buildFollowAction(UserDAO.UserProfile profile) {
        UserFollowDAO followDAO = new UserFollowDAO();
        boolean ownProfile = profile.getUserId() == Session.getCurrentUserId();
        boolean following = !ownProfile && followDAO.isFollowing(Session.getCurrentUserId(), profile.getUserId());

        JButton follow = makeButton(following ? "UNFOLLOW" : "FOLLOW", BG_CARD, following ? TEXT_MUTED : NEON_PINK, BORDER_COL);
        follow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        follow.setEnabled(!ownProfile);
        if (ownProfile) {
            follow.setText("YOUR PROFILE");
        }

        follow.addActionListener(e -> {
            if (!Session.isLoggedIn()) {
                JOptionPane.showMessageDialog(this, "Please log in first.", "Clixky", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean ok = followDAO.toggleFollow(Session.getCurrentUserId(), profile.getUserId());
            if (!ok) {
                SoundFX.error();
                JOptionPane.showMessageDialog(this, "Follow action failed.", "Clixky", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean nowFollowing = followDAO.isFollowing(Session.getCurrentUserId(), profile.getUserId());
            follow.setText(nowFollowing ? "UNFOLLOW" : "FOLLOW");
            follow.setForeground(nowFollowing ? TEXT_MUTED : NEON_PINK);
            SoundFX.success();
        });

        return follow;
    }

    private JPanel profileRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel key = new JLabel(label);
        key.setFont(new Font("Segoe UI", Font.BOLD, 10));
        key.setForeground(NEON_MID);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        val.setForeground(TEXT_MAIN);
        val.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(key, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        return row;
    }

    private String nullToFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void handleDelete() {
        boolean confirmed = showCyberConfirm(
                this,
                "Delete Comment",
                "Delete this comment?",
                "DELETE"
        );
        if (!confirmed) {
            return;
        }

        boolean deleted = onDelete != null && onDelete.deleteComment(data.id);
        if (!deleted) {
            SoundFX.error();
            JOptionPane.showMessageDialog(this, "Comment could not be deleted.", "Clixky", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SoundFX.success();
    }

    private void reportComment() {
        if (!Session.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Please log in first.", "Clixky", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ReportInput report = showReportDialog(this, "Comment");
        if (report == null) {
            return;
        }

        boolean ok = new ReportDAO().reportComment(
                Session.getCurrentUserId(),
                data.id,
                report.getReason(),
                report.getDetails()
        );
        if (!ok) {
            SoundFX.error();
            JOptionPane.showMessageDialog(this, "Report could not be submitted.", "Clixky", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SoundFX.success();
        JOptionPane.showMessageDialog(this, "Report submitted.", "Clixky", JOptionPane.INFORMATION_MESSAGE);
    }

    private Color commentCardColor() {
        if (LIGHT_MODE) {
            return depth == 0 ? BG_PANEL : BG_CARD;
        }

        return depth == 0 ? BG_PANEL : BG_DEEP;
    }

    private Color commentBorderColor() {
        if (depth == 0) {
            return BORDER_COL;
        }

        return new Color(BORDER_COL.getRed(), BORDER_COL.getGreen(), BORDER_COL.getBlue(), LIGHT_MODE ? 180 : 120);
    }

    private void updateCommentVoteButtons(JButton upBtn, JButton downBtn, JLabel score, int currentVote) {
        Color upColor = currentVote == 1 ? NEON_CYAN : TEXT_MUTED;
        Color downColor = currentVote == -1 ? NEON_PINK : TEXT_MUTED;
        upBtn.putClientProperty("normalForeground", upColor);
        downBtn.putClientProperty("normalForeground", downColor);
        upBtn.setForeground(upColor);
        downBtn.setForeground(downColor);
        score.setForeground(currentVote == 1 ? NEON_CYAN : currentVote == -1 ? NEON_PINK : TEXT_MUTED);
    }

    private void toggleCollapse(JLabel btn) {
        collapsed = !collapsed;
        childrenPanel.setVisible(!collapsed);
        btn.setText(collapsed ? "[+]" : "[–]");
        revalidate();
        repaint();
    }

    private void showReplyComposer(JPanel parent) {
        for (Component c : parent.getComponents()) {
            if ("replyBox".equals(c.getName())) {
                parent.remove(c);
                parent.revalidate();
                parent.repaint();
                return;
            }
        }

        JPanel replyBox = new JPanel(new BorderLayout(0, 8));
        replyBox.setName("replyBox");
        replyBox.setBackground(BG_PANEL);
        replyBox.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(NEON_PINK.getRed(), NEON_PINK.getGreen(), NEON_PINK.getBlue(), 120), 1),
            new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel replyLabel = new JLabel("Replying to " + data.author);
        replyLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        replyLabel.setForeground(GOLD);

        JTextArea input = new JTextArea(3, 30);
        input.setBackground(BG_DEEP);
        input.setForeground(TEXT_MAIN);
        input.setCaretColor(NEON_CYAN);
        input.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        input.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL),
            new EmptyBorder(6, 8, 6, 8)
        ));
        input.setLineWrap(true);
        input.setWrapStyleWord(true);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JButton cancel = makeActionBtn("Cancel");
        JButton submit = makeActionBtn("Submit Reply");
        submit.setForeground(NEON_CYAN);

        cancel.addActionListener(e -> {
            parent.remove(replyBox);
            parent.revalidate();
            parent.repaint();
        });
        submit.addActionListener(e -> {
            String text = input.getText().trim();
            if (!text.isEmpty()) {
                if (!Session.isLoggedIn()) {
                    SoundFX.error();
                    JOptionPane.showMessageDialog(this, "Please log in first.", "Clixky", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean saved = onReply != null && onReply.saveReply(data.id, text);
                if (!saved) {
                    SoundFX.error();
                    JOptionPane.showMessageDialog(this, "Reply could not be saved.", "Clixky", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                SoundFX.success();
                parent.remove(replyBox);
                parent.revalidate();
                parent.repaint();
            }
        });

        btnRow.add(cancel);
        btnRow.add(submit);

        replyBox.add(replyLabel, BorderLayout.NORTH);
        replyBox.add(new JScrollPane(input) {{ setBorder(null); }}, BorderLayout.CENTER);
        replyBox.add(btnRow, BorderLayout.SOUTH);

        parent.add(replyBox, BorderLayout.SOUTH);
        parent.revalidate();
        parent.repaint();
        input.requestFocusInWindow();
    }

    private JButton makeActionBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setForeground(TEXT_MUTED);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> SoundFX.click());
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setForeground(NEON_CYAN); }
            public void mouseExited(MouseEvent e)  {
                Color normal = (Color) b.getClientProperty("normalForeground");
                b.setForeground(normal == null ? TEXT_MUTED : normal);
            }
        });
        return b;
    }

    private JButton makeDeleteBtn(String text) {
        JButton b = makeActionBtn(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(NEON_PINK);
        b.putClientProperty("normalForeground", NEON_PINK);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(NEON_PINK.getRed(), NEON_PINK.getGreen(), NEON_PINK.getBlue(), 160)),
                new EmptyBorder(3, 10, 3, 10)
        ));
        b.setBorderPainted(true);
        b.setPreferredSize(new Dimension(76, 28));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private String htmlColor(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    // ── Tree builder ──────────────────────────────────────────────────────
    public static List<CommentData> buildTree(List<CommentData> flat) {
        java.util.Map<Integer, CommentData> map = new java.util.LinkedHashMap<>();
        List<CommentData> roots = new ArrayList<>();
        for (CommentData c : flat) map.put(c.id, c);
        for (CommentData c : flat) {
            if (c.parentId == 0 || !map.containsKey(c.parentId)) roots.add(c);
            else map.get(c.parentId).children.add(c);
        }
        return roots;
    }

    // ── Sample comments ────────────────────────────────────────────────────
    public static List<CommentData> getSampleComments() {
        List<CommentData> list = new ArrayList<>();
        list.add(new CommentData(1, 0, 0, "u/db_expert",  "BCNF matters when you have overlapping candidate keys. In practice, if a 3NF table has no overlapping keys, it is automatically in BCNF. For your DB course though, knowing the difference conceptually is enough.", "4h ago", 156));
        list.add(new CommentData(2, 1, 0, "u/hamza_dev",  "That makes a lot of sense! So it is mostly an edge case in real schema design?", "3h ago", 34));
        list.add(new CommentData(3, 2, 0, "u/db_expert",  "Exactly. You will rarely encounter it unless you have compound keys with partial dependencies. Good luck with CS-220!", "3h ago", 28));
        list.add(new CommentData(4, 0, 0, "u/ali_codes",  "Good question for your Clixky project too — make sure your schema hits at least 3NF for the grade!", "2h ago", 89));
        list.add(new CommentData(5, 4, 0, "u/aayan_a",    "Ha, our ER diagram already has 5 tables fully normalized. Ask me anything!", "1h ago", 12));
        list.add(new CommentData(6, 0, 0, "u/sql_nerd",   "One practical place BCNF shows up: scheduling tables with course/room/time dependencies. Worth knowing for interviews.", "5h ago", 44));
        list.add(new CommentData(7, 6, 0, "u/hamza_dev",  "Oh interesting, I have a data structures exam next week too. Thanks!", "4h ago", 8));
        return list;
    }
}
