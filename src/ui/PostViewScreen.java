package ui;

import dao.CommentDAO;
import dao.Session;
import dao.VoteDAO;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList; // Added import for ArrayList
import java.util.List;
import static ui.LoginScreen.*;
import static ui.DashboardScreen.*;

public class PostViewScreen extends JFrame {

    private PostData   post;
    private String     currentUser;
    private Runnable   onBack;
    private JLabel     scoreLabel;
    private JScrollPane commentsScroll;

    public PostViewScreen(PostData post, String currentUser, Runnable onBack) {
        this.post        = post;
        this.currentUser = currentUser;
        this.onBack      = onBack;
        buildUI();
    }

    private void buildUI() {
        setTitle("Clixky — " + post.title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        WindowState.apply(this, new Dimension(1100, 700), new Dimension(800, 540));
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DEEP);
        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildBody(),   BorderLayout.CENTER);

        setContentPane(root);
        setVisible(true);
    }

    @Override
    public void dispose() {
        WindowState.remember(this);
        super.dispose();
    }

    // ── TOP BAR ───────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(BG_DEEP);
        bar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(NEON_PINK.getRed(), NEON_PINK.getGreen(), NEON_PINK.getBlue(), 80)),
            new EmptyBorder(0, 16, 0, 16)
        ));
        bar.setPreferredSize(new Dimension(0, 48));

        JLabel logo = new JLabel("CLIXKY");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(NEON_PINK);

        JButton backBtn = makeSmallButton("← Back to Feed", BG_CARD, TEXT_MUTED, BORDER_COL);
        backBtn.addActionListener(e -> {
            if (onBack != null) {
                dispose();
                onBack.run();
            }
        });

        JButton themeBtn = makeSmallButton(LIGHT_MODE ? "Dark" : "Light", BG_PANEL, NEON_PINK, BORDER_COL);
        themeBtn.addActionListener(e -> {
            setLightMode(!LIGHT_MODE);
            dispose();
            new PostViewScreen(post, currentUser, onBack);
        });
        JButton soundBtn = makeSoundToggleButton();

        JLabel userLabel = new JLabel(currentUser);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(NEON_MID);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        right.setOpaque(false);
        right.add(backBtn);
        right.add(themeBtn);
        right.add(soundBtn);
        right.add(userLabel);

        bar.add(logo,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── BODY ─────────────────────────────────────────────────────────────────

    private JSplitPane buildBody() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildMain(), buildSidebar());
        split.setDividerLocation(760);
        split.setDividerSize(1);
        split.setBorder(null);
        split.setBackground(BG_DEEP);
        split.setResizeWeight(0.75);
        split.setContinuousLayout(true);
        return split;
    }

    // ── MAIN ─────────────────────────────────────────────────────────────────

    private JScrollPane buildMain() {
        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setBackground(BG_DEEP);
        main.setBorder(new EmptyBorder(16, 16, 16, 12));

        main.add(buildPostCard(),      BorderLayout.NORTH);
        main.add(buildReplyComposer(), BorderLayout.CENTER);

        CommentDAO commentDAO = new CommentDAO();
        commentsScroll = CommentPanel.buildTree(loadComments(), currentUser, (parentCommentId, text) -> {
            int commentId = commentDAO.addComment(post.id, Session.getCurrentUserId(), text, parentCommentId);
            if (commentId == 0) {
                return false;
            }

            post.comments++;
            SwingUtilities.invokeLater(() -> {
                dispose();
                new PostViewScreen(post, currentUser, onBack);
            });
            return true;
        }, commentId -> {
            boolean deleted = commentDAO.deleteComment(commentId, Session.getCurrentUserId());
            if (!deleted) {
                return false;
            }

            post.comments = commentDAO.getCommentsForPost(post.id).size();
            SwingUtilities.invokeLater(() -> {
                dispose();
                new PostViewScreen(post, currentUser, onBack);
            });
            return true;
        });
        commentsScroll.setBorder(new MatteBorder(1, 0, 0, 0,
            new Color(NEON_PINK.getRed(), NEON_PINK.getGreen(), NEON_PINK.getBlue(), 60)));
        commentsScroll.setPreferredSize(new Dimension(0, 380));

        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(BG_DEEP);
        wrapper.add(main,           BorderLayout.NORTH);
        wrapper.add(commentsScroll, BorderLayout.CENTER);

        JScrollPane outerScroll = new JScrollPane(wrapper);
        outerScroll.setBorder(null);
        outerScroll.setBackground(BG_DEEP);
        outerScroll.getViewport().setBackground(BG_DEEP);
        return outerScroll;
    }

    private List<CommentPanel.CommentData> loadComments() {
        List<CommentPanel.CommentData> comments = new ArrayList<>();
        CommentDAO commentDAO = new CommentDAO();

        for (CommentDAO.CommentRecord comment : commentDAO.getCommentsForPost(post.id)) {
            comments.add(new CommentPanel.CommentData(
                    comment.getCommentId(),
                    comment.getParentCommentId(),
                    "u/" + comment.getAuthor(),
                    escapeHtml(comment.getBody()),
                    comment.getCommentedAt(),
                    comment.getScore()
            ));
        }

        return comments;
    }

    private JPanel buildPostCard() {
        JPanel card = new RoundPanel(10, BG_PANEL, BORDER_COL);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Vote column
        JPanel voteCol = new JPanel();
        voteCol.setLayout(new BoxLayout(voteCol, BoxLayout.Y_AXIS));
        voteCol.setOpaque(false);
        voteCol.setPreferredSize(new Dimension(48, 0));

        JButton upBtn   = makeBigVoteBtn("▲", true);
        scoreLabel = new JLabel(String.valueOf(post.score));
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        scoreLabel.setForeground(NEON_CYAN);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton downBtn = makeBigVoteBtn("▼", false);
        final int[] currentVote = {
                Session.isLoggedIn() ? new VoteDAO().getPostUserVote(post.id, Session.getCurrentUserId()) : 0
        };
        updatePostVoteButtons(upBtn, downBtn, scoreLabel, currentVote[0]);

        upBtn.addActionListener(e -> {
            if (!Session.isLoggedIn()) {
                JOptionPane.showMessageDialog(this, "Please log in first.", "Clixky", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int newScore = new VoteDAO().votePost(post.id, Session.getCurrentUserId(), 1);
            currentVote[0] = currentVote[0] == 1 ? 0 : 1;
            post.score = newScore;
            scoreLabel.setText(String.valueOf(newScore));
            updatePostVoteButtons(upBtn, downBtn, scoreLabel, currentVote[0]);
        });
        downBtn.addActionListener(e -> {
            if (!Session.isLoggedIn()) {
                JOptionPane.showMessageDialog(this, "Please log in first.", "Clixky", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int newScore = new VoteDAO().votePost(post.id, Session.getCurrentUserId(), -1);
            currentVote[0] = currentVote[0] == -1 ? 0 : -1;
            post.score = newScore;
            scoreLabel.setText(String.valueOf(newScore));
            updatePostVoteButtons(upBtn, downBtn, scoreLabel, currentVote[0]);
        });

        voteCol.add(Box.createVerticalGlue());
        voteCol.add(upBtn);
        voteCol.add(Box.createVerticalStrut(4));
        voteCol.add(scoreLabel);
        voteCol.add(Box.createVerticalStrut(4));
        voteCol.add(downBtn);
        voteCol.add(Box.createVerticalGlue());

        // Content
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);

        JLabel breadcrumb = new JLabel(post.subreddit + "  ·  Posted by " + post.author + "  ·  " + post.time);
        breadcrumb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        breadcrumb.setForeground(NEON_DIM);

        JLabel title = new JLabel("<html><body style='width:580px; font-family:Segoe UI; font-size:17px; font-weight:bold; color:" + htmlColor(TEXT_MAIN) + "'>"
            + post.title + "</body></html>");

        JLabel body = new JLabel("<html><body style='width:580px; font-family:Segoe UI; font-size:15px; color:" + htmlColor(TEXT_MUTED) + "; line-height:1.7'>"
            + escapeHtml(post.body)
            + "</body></html>");

        // Action bar
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(makeChip("💬  " + post.comments + " Comments", this::toggleComments));
        actions.add(makeChip("↗  Share"));
        actions.add(makeChip("🔖  Save"));

        content.add(breadcrumb, BorderLayout.NORTH);
        content.add(title,      BorderLayout.CENTER);
        content.add(body,       BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(actions, BorderLayout.WEST);
        content.add(bottom, BorderLayout.EAST);

        card.add(voteCol,  BorderLayout.WEST);
        card.add(content,  BorderLayout.CENTER);
        return card;
    }

    private void updatePostVoteButtons(JButton upBtn, JButton downBtn, JLabel score, int currentVote) {
        Color upColor = currentVote == 1 ? NEON_CYAN : NEON_MID;
        Color downColor = currentVote == -1 ? NEON_PINK : TEXT_MUTED;
        upBtn.putClientProperty("normalForeground", upColor);
        downBtn.putClientProperty("normalForeground", downColor);
        upBtn.setForeground(upColor);
        downBtn.setForeground(downColor);
        score.setForeground(currentVote == 1 ? NEON_CYAN : currentVote == -1 ? NEON_PINK : TEXT_MUTED);
    }

    private JPanel buildReplyComposer() {
        JPanel box = new RoundPanel(8, BG_PANEL, BORDER_COL);
        box.setLayout(new BorderLayout(0, 10));
        box.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel heading = new JLabel("Add a comment as  " + currentUser);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(NEON_MID);

        JTextArea input = new JTextArea(3, 40);
        input.setBackground(BG_DEEP);
        input.setForeground(TEXT_MAIN);
        input.setCaretColor(NEON_CYAN);
        input.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        input.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL),
            new EmptyBorder(8, 10, 8, 10)
        ));
        input.setLineWrap(true);
        input.setWrapStyleWord(true);

        JScrollPane inputScroll = new JScrollPane(input);
        inputScroll.setBorder(null);
        inputScroll.getViewport().setBackground(BG_DEEP);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);

        JButton clearBtn  = makeSmallButton("Clear",   BG_DEEP, TEXT_MUTED, BORDER_COL);
        JButton submitBtn = makeSmallButton("Comment", BG_CARD, NEON_CYAN,  BORDER_COL);

        clearBtn.addActionListener(e -> input.setText(""));
        submitBtn.addActionListener(e -> {
            String text = input.getText().trim();
            if (!text.isEmpty()) {
                if (!Session.isLoggedIn()) {
                    SoundFX.error();
                    JOptionPane.showMessageDialog(this, "Please log in first.", "Clixky", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int commentId = new CommentDAO().addComment(post.id, Session.getCurrentUserId(), text, null);
                if (commentId == 0) {
                    SoundFX.error();
                    JOptionPane.showMessageDialog(this, "Comment could not be saved.", "Clixky", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                SoundFX.success();
                input.setText("");
                post.comments++;
                dispose();
                new PostViewScreen(post, currentUser, onBack);
            }
        });

        btnRow.add(clearBtn);
        btnRow.add(submitBtn);

        box.add(heading,     BorderLayout.NORTH);
        box.add(inputScroll, BorderLayout.CENTER);
        box.add(btnRow,      BorderLayout.SOUTH);
        return box;
    }

    // ── SIDEBAR ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_DEEP);
        sidebar.setBorder(new CompoundBorder(
            new MatteBorder(0, 1, 0, 0, new Color(NEON_PINK.getRed(), NEON_PINK.getGreen(), NEON_PINK.getBlue(), 50)),
            new EmptyBorder(16, 14, 16, 14)
        ));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        inner.add(buildCommunityCard());
        inner.add(Box.createVerticalStrut(16));
        inner.add(buildTopPostsSection());

        sidebar.add(inner, BorderLayout.NORTH);
        return sidebar;
    }

    private JPanel buildCommunityCard() {
        JPanel card = new RoundPanel(8, BG_PANEL, BORDER_COL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel name = new JLabel(post.subreddit);
        name.setFont(new Font("Segoe UI", Font.BOLD, 17));
        name.setForeground(NEON_PINK);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel desc = new JLabel("<html><body style='width:180px; color:#5a6090; font-family:Segoe UI; font-size:11px'>"
            + "A community for sharing ideas, asking questions, and connecting with others who share your interests.</body></html>");
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COL);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JPanel stats = new JPanel(new GridLayout(1, 2, 8, 0));
        stats.setOpaque(false);
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.add(makeStat("24.8k", "Members"));
        stats.add(makeStat("142",   "Online Now"));

        JButton joinedBtn = makeButton("✓  Joined", BG_CARD, NEON_CYAN, NEON_DIM);
        joinedBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        card.add(name);
        card.add(Box.createVerticalStrut(8));
        card.add(desc);
        card.add(Box.createVerticalStrut(12));
        card.add(sep);
        card.add(Box.createVerticalStrut(10));
        card.add(stats);
        card.add(Box.createVerticalStrut(12));
        card.add(joinedBtn);
        return card;
    }

    private JPanel buildTopPostsSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);

        JLabel header = new JLabel("TOP TODAY");
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setForeground(NEON_DIM);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        section.add(header);

        String[] tops = {
            "When to use NoSQL vs SQL",
            "Indexing strategies for large tables",
            "ER diagram best practices 2025",
            "How to design a voting system"
        };

        for (String t : tops) {
            JLabel l = new JLabel("<html><body style='width:180px'>" + t + "</body></html>");
            l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            l.setForeground(TEXT_MUTED);
            l.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(18, 20, 45)),
                new EmptyBorder(7, 0, 7, 0)
            ));
            l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            l.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { l.setForeground(NEON_CYAN); }
                public void mouseExited(MouseEvent e)  { l.setForeground(TEXT_MUTED); }
            });
            section.add(l);
        }
        return section;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private JPanel makeStat(String value, String label) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 18));
        v.setForeground(NEON_CYAN);
        v.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(NEON_DIM);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(v);
        p.add(l);
        return p;
    }

    private JLabel makeChip(String text) {
        return makeChip(text, null);
    }

    private JLabel makeChip(String text, Runnable onClick) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_MUTED);
        l.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL, 1),
            new EmptyBorder(4, 10, 4, 10)
        ));
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                SoundFX.click();
                if (onClick != null) {
                    onClick.run();
                }
            }
            public void mouseEntered(MouseEvent e) { l.setForeground(NEON_CYAN); }
            public void mouseExited(MouseEvent e)  { l.setForeground(TEXT_MUTED); }
        });
        return l;
    }

    private void toggleComments() {
        if (commentsScroll == null) {
            return;
        }

        commentsScroll.setVisible(!commentsScroll.isVisible());
        commentsScroll.getParent().revalidate();
        commentsScroll.getParent().repaint();
    }

    private JButton makeBigVoteBtn(String text, boolean isUp) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        b.setForeground(isUp ? NEON_MID : TEXT_MUTED);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> SoundFX.click());
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setForeground(isUp ? NEON_CYAN : NEON_PINK); }
            public void mouseExited(MouseEvent e)  {
                Color normal = (Color) b.getClientProperty("normalForeground");
                b.setForeground(normal == null ? (isUp ? NEON_MID : TEXT_MUTED) : normal);
            }
        });
        return b;
    }

    private String escapeHtml(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\n", "<br>");
    }

    private String htmlColor(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PostData samplePost = new PostData(
                2, "3NF vs BCNF — When does it actually matter in production?",
                "BCNF can matter when dependencies overlap in a schema.",
                "u/hamza_dev", "r/DatabaseDesign", "6h ago", 412, 47
            );
            new PostViewScreen(samplePost, "hamza_dev", () -> System.out.println("Back clicked"));
        });
    }
}
