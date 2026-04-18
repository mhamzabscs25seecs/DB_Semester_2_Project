package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import static ui.LoginScreen.*;

public class DashboardScreen extends JFrame {

    private String loggedInUser;
    private JPanel feedPanel;
    private JPanel sidebarList;

    // Callback when user clicks a post
    private java.util.function.Consumer<PostData> onOpenPost;

    public DashboardScreen(String username, java.util.function.Consumer<PostData> onOpenPost) {
        this.loggedInUser = username;
        this.onOpenPost   = onOpenPost;
        buildUI();
    }

    private void buildUI() {
        setTitle("Clixky — Home");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 680);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DEEP);
        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);

        setContentPane(root);
        setVisible(true);
    }

    // ── TOP BAR ────────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(new Color(6, 15, 6));
        bar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_COL),
            new EmptyBorder(0, 16, 0, 16)
        ));
        bar.setPreferredSize(new Dimension(0, 48));

        // Logo
        JLabel logo = new JLabel("CLIXKY");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logo.setForeground(GREEN_HI);
        logo.setBorder(new EmptyBorder(0, 0, 0, 12));

        // Search bar
        JTextField search = makeTextField("Search posts and communities...");
        search.setMaximumSize(new Dimension(340, 34));
        search.setPreferredSize(new Dimension(300, 34));
        search.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL, 1),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));

        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 7));
        searchWrap.setOpaque(false);
        searchWrap.add(search);

        // Right-side actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setOpaque(false);

        JButton newPostBtn = makeSmallButton("+ New Post", BG_CARD, GREEN_HI, new Color(42, 90, 42));
        newPostBtn.addActionListener(e -> showCreatePostDialog());

        JButton browseBtn = makeSmallButton("Browse", BG_PANEL, TEXT_MUTED, BORDER_COL);

        // User avatar circle
        JLabel avatar = new JLabel(loggedInUser.substring(0, 1).toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(42, 90, 42));
                g2.drawOval(0, 0, getWidth()-1, getHeight()-1);
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(30, 30));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        avatar.setForeground(GREEN_HI);

        actions.add(newPostBtn);
        actions.add(browseBtn);
        actions.add(avatar);

        bar.add(logo, BorderLayout.WEST);
        bar.add(searchWrap, BorderLayout.CENTER);
        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    // ── BODY (SIDEBAR + FEED) ─────────────────────────────────────────────────

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(BG_DEEP);
        body.add(buildSidebar(), BorderLayout.WEST);
        body.add(buildFeed(),    BorderLayout.CENTER);
        return body;
    }

    // ── SIDEBAR ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(6, 15, 6));
        sidebar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 0, 1, BORDER_COL),
            new EmptyBorder(16, 0, 16, 0)
        ));
        sidebar.setPreferredSize(new Dimension(185, 0));

        sidebar.add(sidebarSection("MY COMMUNITIES"));
        sidebarList = new JPanel();
        sidebarList.setLayout(new BoxLayout(sidebarList, BoxLayout.Y_AXIS));
        sidebarList.setOpaque(false);

        String[] joined = {"r/JavaProgramming", "r/DatabaseDesign", "r/NUCES_FAST", "r/OpenSource", "r/TechNews"};
        for (int i = 0; i < joined.length; i++) {
            sidebarList.add(makeSidebarItem(joined[i], i == 0));
        }
        sidebar.add(sidebarList);

        sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(sidebarSection("TRENDING"));
        sidebar.add(makeSidebarItem("r/AskCS",   false, GOLD));
        sidebar.add(makeSidebarItem("r/Projects", false, GOLD));

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JLabel sidebarSection(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 9));
        l.setForeground(GREEN_DIM);
        l.setBorder(new EmptyBorder(0, 14, 6, 14));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel makeSidebarItem(String name, boolean active) {
        return makeSidebarItem(name, active, GREEN_HI);
    }

    private JPanel makeSidebarItem(String name, boolean active, Color dotColor) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        item.setOpaque(true);
        item.setBackground(active ? BG_CARD : new Color(6, 15, 6));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        if (active) {
            item.setBorder(new MatteBorder(0, 3, 0, 0, GREEN_MID));
        } else {
            item.setBorder(new EmptyBorder(0, 3, 0, 0));
        }
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Dot indicator
        JLabel dot = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(active ? dotColor : GREEN_DIM);
                g2.fillOval(0, 3, 7, 7);
            }
        };
        dot.setPreferredSize(new Dimension(8, 14));

        JLabel label = new JLabel(name);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(active ? GREEN_HI : TEXT_MUTED);

        item.add(dot);
        item.add(label);

        // Hover
        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!active) item.setBackground(BG_PANEL);
            }
            public void mouseExited(MouseEvent e) {
                if (!active) item.setBackground(new Color(6, 15, 6));
            }
            public void mouseClicked(MouseEvent e) {
                filterFeedBy(name);
            }
        });
        return item;
    }

    // ── FEED ──────────────────────────────────────────────────────────────────

    private JScrollPane buildFeed() {
        feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBackground(BG_DEEP);
        feedPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        loadPosts(getSamplePosts());

        JScrollPane scroll = new JScrollPane(feedPanel);
        scroll.setBackground(BG_DEEP);
        scroll.getViewport().setBackground(BG_DEEP);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(scroll);
        return scroll;
    }

    private void loadPosts(List<PostData> posts) {
        feedPanel.removeAll();
        for (PostData p : posts) {
            feedPanel.add(makePostCard(p));
            feedPanel.add(Box.createVerticalStrut(8));
        }
        feedPanel.revalidate();
        feedPanel.repaint();
    }

    private void filterFeedBy(String subreddit) {
        List<PostData> all = getSamplePosts();
        List<PostData> filtered = new ArrayList<>();
        for (PostData p : all) {
            if (p.subreddit.equals(subreddit)) filtered.add(p);
        }
        loadPosts(filtered.isEmpty() ? all : filtered);
    }

    private JPanel makePostCard(PostData post) {
        JPanel card = new RoundPanel(8, BG_PANEL, BORDER_COL);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(12, 12, 12, 14));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Vote column
        JPanel voteCol = new JPanel();
        voteCol.setLayout(new BoxLayout(voteCol, BoxLayout.Y_AXIS));
        voteCol.setOpaque(false);
        voteCol.setPreferredSize(new Dimension(38, 0));

        JButton upBtn   = makeVoteBtn("▲", true);
        JLabel  score   = new JLabel(String.valueOf(post.score));
        score.setFont(new Font("Segoe UI", Font.BOLD, 11));
        score.setForeground(post.score > 100 ? GREEN_HI : TEXT_MUTED);
        score.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton downBtn = makeVoteBtn("▼", false);

        upBtn.addActionListener(e -> {
            post.score++;
            score.setText(String.valueOf(post.score));
            score.setForeground(GREEN_HI);
            upBtn.setForeground(GREEN_HI);
        });
        downBtn.addActionListener(e -> {
            post.score--;
            score.setText(String.valueOf(post.score));
            downBtn.setForeground(new Color(200, 100, 60));
        });

        voteCol.add(Box.createVerticalGlue());
        voteCol.add(upBtn);
        voteCol.add(Box.createVerticalStrut(2));
        voteCol.add(score);
        voteCol.add(Box.createVerticalStrut(2));
        voteCol.add(downBtn);
        voteCol.add(Box.createVerticalGlue());

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel titleLabel = new JLabel("<html><body style='width:500px'>" + post.title + "</body></html>");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(158, 222, 158));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel meta = new JLabel("by  " + post.author + "  ·  " + post.subreddit + "  ·  " + post.time + "  ·  " + post.comments + " comments");
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        meta.setForeground(GREEN_DIM);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(Box.createVerticalGlue());
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(meta);
        content.add(Box.createVerticalGlue());

        card.add(voteCol,  BorderLayout.WEST);
        card.add(content,  BorderLayout.CENTER);

        // Click to open post
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (onOpenPost != null) onOpenPost.accept(post);
            }
            public void mouseEntered(MouseEvent e) { card.setBackground(BG_CARD); }
            public void mouseExited(MouseEvent e)  { card.setBackground(BG_PANEL); }
        });

        return card;
    }

    private JButton makeVoteBtn(String text, boolean isUp) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setForeground(isUp ? GREEN_MID : TEXT_MUTED);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setPreferredSize(new Dimension(24, 20));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setForeground(isUp ? GREEN_HI : new Color(220, 120, 60));
            }
            public void mouseExited(MouseEvent e) {
                b.setForeground(isUp ? GREEN_MID : TEXT_MUTED);
            }
        });
        return b;
    }

    private void showCreatePostDialog() {
        JDialog dlg = new JDialog(this, "New Post", true);
        dlg.setSize(460, 360);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_PANEL);
        p.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel heading = new JLabel("Create a Post");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        heading.setForeground(GREEN_HI);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField titleField = makeTextField("Post title");
        JTextArea  bodyArea   = new JTextArea(5, 20);
        bodyArea.setBackground(BG_DEEP);
        bodyArea.setForeground(TEXT_MAIN);
        bodyArea.setCaretColor(GREEN_HI);
        bodyArea.setFont(FONT_INPUT);
        bodyArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        bodyScroll.setBorder(null);
        bodyScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        bodyScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton submit = makeButton("SUBMIT POST", BG_CARD, GREEN_HI, new Color(42, 90, 42));
        submit.addActionListener(e -> {
            // TODO: PostDAO.createPost(...)
            dlg.dispose();
        });

        p.add(heading);
        p.add(Box.createVerticalStrut(18));
        p.add(makeLabel("TITLE"));
        p.add(Box.createVerticalStrut(6));
        p.add(titleField);
        p.add(Box.createVerticalStrut(14));
        p.add(makeLabel("BODY"));
        p.add(Box.createVerticalStrut(6));
        p.add(bodyScroll);
        p.add(Box.createVerticalStrut(20));
        p.add(submit);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    // ── Scroll bar style ──────────────────────────────────────────────────────

    private void styleScrollBar(JScrollPane sp) {
        JScrollBar vsb = sp.getVerticalScrollBar();
        vsb.setBackground(BG_DEEP);
        vsb.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor  = BG_CARD;
                this.trackColor  = BG_DEEP;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
            }
        });
    }

    static JButton makeSmallButton(String text, Color bg, Color fg, Color border) {
        JButton b = makeButton(text, bg, fg, border);
        b.setMaximumSize(new Dimension(120, 32));
        b.setPreferredSize(new Dimension(100, 32));
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        return b;
    }

    // ── Sample data (replace with DB calls) ───────────────────────────────────

    public static List<PostData> getSamplePosts() {
        List<PostData> list = new ArrayList<>();
        list.add(new PostData(1, "Best practices for JDBC connection pooling in Java?", "u/ali_codes", "r/JavaProgramming", "4h ago", 847, 23));
        list.add(new PostData(2, "3NF vs BCNF — When does it actually matter in production?", "u/hamza_dev", "r/DatabaseDesign", "6h ago", 412, 47));
        list.add(new PostData(3, "CS-220 semester project ideas — share yours!", "u/aayan_a", "r/NUCES_FAST", "1d ago", 189, 91));
        list.add(new PostData(4, "SQLite vs PostgreSQL for learning relational DB concepts", "u/db_learner", "r/DatabaseDesign", "2d ago", 56, 18));
        list.add(new PostData(5, "How I built a full-stack Java app in 7 days", "u/hamza_dev", "r/JavaProgramming", "3d ago", 203, 34));
        list.add(new PostData(6, "Open source Java GUI libraries comparison 2025", "u/code_wizard", "r/OpenSource", "4d ago", 98, 12));
        return list;
    }

    // ── PostData DTO ─────────────────────────────────────────────────────────

    public static class PostData {
        public int    id, score, comments;
        public String title, author, subreddit, time;
        public PostData(int id, String title, String author, String subreddit, String time, int score, int comments) {
            this.id = id; this.title = title; this.author = author;
            this.subreddit = subreddit; this.time = time;
            this.score = score; this.comments = comments;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new DashboardScreen("hamza_dev", post -> System.out.println("Opened: " + post.title))
        );
    }
}
