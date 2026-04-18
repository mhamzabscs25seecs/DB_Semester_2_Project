package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import static ui.LoginScreen.*;
import static ui.DashboardScreen.*;

public class PostViewScreen extends JFrame {

    private PostData   post;
    private String     currentUser;
    private Runnable   onBack;
    private JLabel     scoreLabel;

    public PostViewScreen(PostData post, String currentUser, Runnable onBack) {
        this.post        = post;
        this.currentUser = currentUser;
        this.onBack      = onBack;
        buildUI();
    }

    private void buildUI() {
        setTitle("Clixky — " + post.title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(800, 540));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DEEP);
        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);

        setContentPane(root);
        setVisible(true);
    }

    // ── TOP BAR ───────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(new Color(6, 15, 6));
        bar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_COL),
            new EmptyBorder(0, 16, 0, 16)
        ));
        bar.setPreferredSize(new Dimension(0, 48));

        JLabel logo = new JLabel("CLIXKY");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logo.setForeground(GREEN_HI);

        JButton backBtn = makeSmallButton("← Back to Feed", BG_CARD, TEXT_MUTED, BORDER_COL);
        backBtn.addActionListener(e -> { if (onBack != null) onBack.run(); });

        JLabel userLabel = new JLabel(currentUser);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLabel.setForeground(GREEN_MID);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        right.setOpaque(false);
        right.add(backBtn);
        right.add(userLabel);

        bar.add(logo, BorderLayout.WEST);
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

    // ── MAIN (left): post card + comments ────────────────────────────────────

    private JScrollPane buildMain() {
        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setBackground(BG_DEEP);
        main.setBorder(new EmptyBorder(16, 16, 16, 12));

        main.add(buildPostCard(), BorderLayout.NORTH);
        main.add(buildReplyComposer(), BorderLayout.CENTER);

        // Comments take the south
        JScrollPane commentsScroll = CommentPanel.buildTree(
            CommentPanel.getSampleComments(), // TODO: replace with CommentDAO.getComments(post.id)
            currentUser,
            () -> { /* refresh callback */ }
        );
        commentsScroll.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COL));
        commentsScroll.setPreferredSize(new Dimension(0, 380));

        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(BG_DEEP);
        wrapper.add(main, BorderLayout.NORTH);
        wrapper.add(commentsScroll, BorderLayout.CENTER);

        JScrollPane outerScroll = new JScrollPane(wrapper);
        outerScroll.setBorder(null);
        outerScroll.setBackground(BG_DEEP);
        outerScroll.getViewport().setBackground(BG_DEEP);
        return outerScroll;
    }

    private JPanel buildPostCard() {
        JPanel card = new RoundPanel(10, BG_PANEL, BORDER_COL);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Vote column (left)
        JPanel voteCol = new JPanel();
        voteCol.setLayout(new BoxLayout(voteCol, BoxLayout.Y_AXIS));
        voteCol.setOpaque(false);
        voteCol.setPreferredSize(new Dimension(48, 0));

        JButton upBtn   = makeBigVoteBtn("▲", true);
        scoreLabel = new JLabel(String.valueOf(post.score));
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        scoreLabel.setForeground(GREEN_HI);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton downBtn = makeBigVoteBtn("▼", false);

        upBtn.addActionListener(e -> {
            post.score++;
            scoreLabel.setText(String.valueOf(post.score));
            scoreLabel.setForeground(GREEN_HI);
            // TODO: VoteDAO.vote(post.id, currentUser, "up")
        });
        downBtn.addActionListener(e -> {
            post.score--;
            scoreLabel.setText(String.valueOf(post.score));
            scoreLabel.setForeground(new Color(200, 110, 60));
            // TODO: VoteDAO.vote(post.id, currentUser, "down")
        });

        voteCol.add(Box.createVerticalGlue());
        voteCol.add(upBtn);
        voteCol.add(Box.createVerticalStrut(4));
        voteCol.add(scoreLabel);
        voteCol.add(Box.createVerticalStrut(4));
        voteCol.add(downBtn);
        voteCol.add(Box.createVerticalGlue());

        // Content (right)
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);

        // Breadcrumb
        JLabel breadcrumb = new JLabel(post.subreddit + "  ·  Posted by " + post.author + "  ·  " + post.time);
        breadcrumb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        breadcrumb.setForeground(GREEN_DIM);

        // Title
        JLabel title = new JLabel("<html><body style='width:580px; font-family:Segoe UI; font-size:15px; font-weight:bold; color:#9ede9e'>"
            + post.title + "</body></html>");

        // Body text (placeholder — replace with real post body from DB)
        JLabel body = new JLabel("<html><body style='width:580px; font-family:Segoe UI; font-size:13px; color:#5a8a5a; line-height:1.7'>"
            + "This is the full post body. In your app, load this from PostDAO.getPostById(" + post.id + "). "
            + "It can include multi-paragraph text, questions, or discussions that community members can engage with."
            + "</body></html>");

        // Action bar
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        JLabel commentsBtn = makeChip("💬  " + post.comments + " Comments");
        JLabel shareBtn    = makeChip("↗  Share");
        JLabel saveBtn     = makeChip("🔖  Save");

        actions.add(commentsBtn);
        actions.add(shareBtn);
        actions.add(saveBtn);

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

    private JPanel buildReplyComposer() {
        JPanel box = new RoundPanel(8, BG_PANEL, BORDER_COL);
        box.setLayout(new BorderLayout(0, 10));
        box.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel heading = new JLabel("Add a comment as  " + currentUser);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        heading.setForeground(GREEN_MID);

        JTextArea input = new JTextArea(3, 40);
        input.setBackground(BG_DEEP);
        input.setForeground(TEXT_MAIN);
        input.setCaretColor(GREEN_HI);
        input.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
        JButton submitBtn = makeSmallButton("Comment", BG_CARD, GREEN_HI,   new Color(42, 90, 42));

        clearBtn.addActionListener(e -> input.setText(""));
        submitBtn.addActionListener(e -> {
            String text = input.getText().trim();
            if (!text.isEmpty()) {
                // TODO: CommentDAO.addComment(post.id, 0, currentUser, text)
                input.setText("");
                JOptionPane.showMessageDialog(this, "Comment posted!", "Clixky", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnRow.add(clearBtn);
        btnRow.add(submitBtn);

        box.add(heading,    BorderLayout.NORTH);
        box.add(inputScroll, BorderLayout.CENTER);
        box.add(btnRow,     BorderLayout.SOUTH);
        return box;
    }

    // ── SIDEBAR ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(new Color(6, 15, 6));
        sidebar.setBorder(new CompoundBorder(
            new MatteBorder(0, 1, 0, 0, BORDER_COL),
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
        name.setFont(new Font("Segoe UI", Font.BOLD, 15));
        name.setForeground(GREEN_HI);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel desc = new JLabel("<html><body style='width:180px; color:#3a6a3a; font-family:Segoe UI; font-size:11px'>"
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

        JButton joinedBtn = makeButton("✓  Joined", BG_CARD, GREEN_HI, GREEN_DIM);
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
        header.setFont(new Font("Segoe UI", Font.BOLD, 9));
        header.setForeground(GREEN_DIM);
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
            l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            l.setForeground(TEXT_MUTED);
            l.setBorder(new MatteBorder(0, 0, 1, 0, new Color(15, 35, 15)));
            l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            l.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(15, 35, 15)),
                new EmptyBorder(7, 0, 7, 0)
            ));
            l.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { l.setForeground(GREEN_HI); }
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
        v.setFont(new Font("Segoe UI", Font.BOLD, 16));
        v.setForeground(GREEN_HI);
        v.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        l.setForeground(GREEN_DIM);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(v);
        p.add(l);
        return p;
    }

    private JLabel makeChip(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(TEXT_MUTED);
        l.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL, 1),
            new EmptyBorder(4, 10, 4, 10)
        ));
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { l.setForeground(GREEN_HI); }
            public void mouseExited(MouseEvent e)  { l.setForeground(TEXT_MUTED); }
        });
        return l;
    }

    private JButton makeBigVoteBtn(String text, boolean isUp) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        b.setForeground(isUp ? GREEN_MID : TEXT_MUTED);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setForeground(isUp ? GREEN_HI : new Color(220, 100, 50)); }
            public void mouseExited(MouseEvent e)  { b.setForeground(isUp ? GREEN_MID : TEXT_MUTED); }
        });
        return b;
    }

    // Quick test
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PostData samplePost = new PostData(
                2,
                "3NF vs BCNF — When does it actually matter in production?",
                "u/hamza_dev", "r/DatabaseDesign", "6h ago", 412, 47
            );
            new PostViewScreen(samplePost, "hamza_dev", () -> System.out.println("Back clicked"));
        });
    }
}
