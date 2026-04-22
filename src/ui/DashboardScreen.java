package ui;

import dao.PostDAO;
import dao.Session;
import dao.UserDAO;
import dao.VoteDAO;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import static ui.LoginScreen.*;

public class DashboardScreen extends JFrame {

    private String loggedInUser;
    private JPanel feedPanel;
    private JPanel sidebarList;
    private java.util.function.Consumer<PostData> onOpenPost;
    private Integer selectedCommunityId;
    private String selectedCommunityName;
    private String searchQuery = "";

    public DashboardScreen(String username, java.util.function.Consumer<PostData> onOpenPost) {
        this(username, onOpenPost, null, null);
    }

    public DashboardScreen(String username, java.util.function.Consumer<PostData> onOpenPost,
                           Integer selectedCommunityId, String selectedCommunityName) {
        this.loggedInUser = username;
        this.onOpenPost   = onOpenPost;
        this.selectedCommunityId = selectedCommunityId;
        this.selectedCommunityName = selectedCommunityName;
        buildUI();
    }

    private void buildUI() {
        setTitle("Clixky — Home");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        WindowState.apply(this, new Dimension(1100, 680), new Dimension(800, 500));
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

    // ── TOP BAR ────────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(BG_DEEP);
        bar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(NEON_PINK.getRed(), NEON_PINK.getGreen(), NEON_PINK.getBlue(), 80)),
            new EmptyBorder(0, 16, 0, 16)
        ));
        bar.setPreferredSize(new Dimension(0, 48));

        // Logo
        JLabel logo = new JLabel("CLIXKY");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(NEON_PINK);
        logo.setBorder(new EmptyBorder(0, 0, 0, 12));

        // Search bar
        JTextField search = makeTextField("Search posts and communities...");
        search.setMaximumSize(new Dimension(340, 34));
        search.setPreferredSize(new Dimension(300, 34));
        search.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL, 1),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        search.addActionListener(e -> search.transferFocus());
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateSearch(); }
            public void removeUpdate(DocumentEvent e) { updateSearch(); }
            public void changedUpdate(DocumentEvent e) { updateSearch(); }

            private void updateSearch() {
                searchQuery = getSearchText(search);
                refreshFeed();
            }
        });

        JPanel searchWrap = new JPanel(new GridBagLayout());
        searchWrap.setOpaque(false);
        searchWrap.add(search);

        // Right-side actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setOpaque(false);

        JButton newPostBtn = makeSmallButton("+ New Post", BG_CARD, NEON_CYAN, BORDER_COL);
        newPostBtn.addActionListener(e -> showCreatePostDialog());

        JButton browseBtn = makeSmallButton("Browse", BG_PANEL, TEXT_MUTED, BORDER_COL);
        JButton themeBtn = makeSmallButton(LIGHT_MODE ? "Dark" : "Light", BG_PANEL, NEON_PINK, BORDER_COL);
        themeBtn.addActionListener(e -> {
            setLightMode(!LIGHT_MODE);
            dispose();
            new DashboardScreen(loggedInUser, onOpenPost, selectedCommunityId, selectedCommunityName);
        });
        JButton soundBtn = makeSoundToggleButton();

        // User avatar circle
        JLabel avatar = new JLabel(loggedInUser.substring(0, 1).toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(NEON_PINK);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1, 1, getWidth()-2, getHeight()-2);
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(30, 30));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        avatar.setForeground(NEON_CYAN);
        avatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        avatar.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                SoundFX.click();
                showProfileDialog();
            }
            public void mouseEntered(MouseEvent e) { avatar.setForeground(NEON_PINK); }
            public void mouseExited(MouseEvent e)  { avatar.setForeground(NEON_CYAN); }
        });

        actions.add(newPostBtn);
        actions.add(browseBtn);
        actions.add(themeBtn);
        actions.add(soundBtn);
        actions.add(avatar);

        bar.add(logo,       BorderLayout.WEST);
        bar.add(searchWrap, BorderLayout.CENTER);
        bar.add(actions,    BorderLayout.EAST);
        clearFieldFocusOnBlankClick(bar);
        clearFieldFocusOnBlankClick(searchWrap);
        return bar;
    }

    private void showProfileDialog() {
        if (!Session.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "No user session found.", "Clixky", JOptionPane.ERROR_MESSAGE);
            return;
        }

        UserDAO.UserProfile profile = new UserDAO().getProfileById(Session.getCurrentUserId());
        if (profile == null) {
            JOptionPane.showMessageDialog(this, "Profile data could not be loaded.", "Clixky", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Clixky Profile", true);
        dialog.setSize(390, 430);
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

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COL);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        card.add(avatar);
        card.add(Box.createVerticalStrut(10));
        card.add(name);
        card.add(Box.createVerticalStrut(4));
        card.add(username);
        card.add(Box.createVerticalStrut(18));
        card.add(sep);
        card.add(Box.createVerticalStrut(16));
        card.add(profileRow("EMAIL", profile.getEmail()));
        card.add(profileRow("BIO", nullToFallback(profile.getBioText(), "No bio yet.")));
        card.add(profileRow("COUNTRY", nullToFallback(profile.getCountry(), "Not set")));
        card.add(profileRow("PHONE", nullToFallback(profile.getPhoneNo(), "Not set")));
        card.add(profileRow("BIRTH YEAR", String.valueOf(profile.getBirthYear())));
        card.add(profileRow("PRIVACY", profile.isPrivate() ? "Private" : "Public"));
        card.add(Box.createVerticalStrut(18));

        JButton edit = makeButton("EDIT PROFILE", BG_CARD, NEON_PINK, BORDER_COL);
        edit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        edit.addActionListener(e -> {
            dialog.dispose();
            dispose();
            new ProfileSetupScreen(() ->
                    new DashboardScreen(loggedInUser, onOpenPost, selectedCommunityId, selectedCommunityName)
            );
        });

        JButton close = makeButton("CLOSE", BG_CARD, NEON_CYAN, BORDER_COL);
        close.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        close.addActionListener(e -> dialog.dispose());
        card.add(edit);
        card.add(Box.createVerticalStrut(8));
        card.add(close);

        dialog.setContentPane(card);
        dialog.setVisible(true);
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

    // ── BODY ─────────────────────────────────────────────────────────────────

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
        sidebar.setBackground(BG_DEEP);
        sidebar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 0, 1, new Color(NEON_PINK.getRed(), NEON_PINK.getGreen(), NEON_PINK.getBlue(), 50)),
            new EmptyBorder(16, 0, 16, 0)
        ));
        sidebar.setPreferredSize(new Dimension(185, 0));

        sidebar.add(sidebarSection("MY COMMUNITIES"));
        sidebarList = new JPanel();
        sidebarList.setLayout(new BoxLayout(sidebarList, BoxLayout.Y_AXIS));
        sidebarList.setOpaque(false);

        sidebarList.add(makeSidebarItem("Home Feed", selectedCommunityId == null, NEON_CYAN, null, null));
        for (PostDAO.CommunityOption community : new PostDAO().getCommunities()) {
            boolean active = selectedCommunityId != null && selectedCommunityId == community.getCommunityId();
            sidebarList.add(makeSidebarItem(
                    "r/" + community.getCommunityName(),
                    active,
                    NEON_CYAN,
                    community.getCommunityId(),
                    community.getCommunityName()
            ));
        }
        sidebar.add(sidebarList);

        sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(sidebarSection("TRENDING"));
        sidebar.add(makeSidebarItem("r/AskCS",    false, GOLD));
        sidebar.add(makeSidebarItem("r/Projects", false, GOLD));

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JLabel sidebarSection(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(NEON_DIM);
        l.setBorder(new EmptyBorder(0, 14, 6, 14));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel makeSidebarItem(String name, boolean active) {
        return makeSidebarItem(name, active, NEON_CYAN, null, null);
    }

    private JPanel makeSidebarItem(String name, boolean active, Color dotColor) {
        return makeSidebarItem(name, active, dotColor, null, null);
    }

    private JPanel makeSidebarItem(String name, boolean active, Color dotColor,
                                   Integer communityId, String communityName) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        item.setOpaque(true);
        item.setBackground(active ? BG_CARD : BG_DEEP);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        if (active) {
            item.setBorder(new MatteBorder(0, 3, 0, 0, NEON_PINK));
        } else {
            item.setBorder(new EmptyBorder(0, 3, 0, 0));
        }
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel dot = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(active ? dotColor : NEON_DIM);
                g2.fillOval(0, 3, 7, 7);
            }
        };
        dot.setPreferredSize(new Dimension(8, 14));

        JLabel label = new JLabel(name);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(active ? NEON_CYAN : TEXT_MUTED);

        item.add(dot);
        item.add(label);

        item.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                SoundFX.click();
                dispose();
                new DashboardScreen(loggedInUser, onOpenPost, communityId, communityName);
            }
            public void mouseEntered(MouseEvent e) {
                if (!active) { item.setBackground(BG_PANEL); label.setForeground(NEON_CYAN); }
            }
            public void mouseExited(MouseEvent e) {
                if (!active) { item.setBackground(BG_DEEP); label.setForeground(TEXT_MUTED); }
            }
        });

        return item;
    }

    private String getSearchText(JTextField search) {
        String text = search.getText().trim();
        if (search.getForeground().equals(NEON_DIM)) {
            return "";
        }
        return text;
    }

    // ── FEED ──────────────────────────────────────────────────────────────────

    private JScrollPane buildFeed() {
        feedPanel = new JPanel();
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBackground(BG_DEEP);
        feedPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        refreshFeed();

        JScrollPane scroll = new JScrollPane(feedPanel);
        scroll.setBorder(null);
        scroll.setBackground(BG_DEEP);
        scroll.getViewport().setBackground(BG_DEEP);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(scroll);
        return scroll;
    }

    private void refreshFeed() {
        if (feedPanel == null) {
            return;
        }

        feedPanel.removeAll();

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel feedTitle = new JLabel("Home Feed");
        if (selectedCommunityName != null) {
            feedTitle.setText("r/" + selectedCommunityName);
        }
        if (!searchQuery.isBlank()) {
            feedTitle.setText("Search: " + searchQuery);
        }
        feedTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        feedTitle.setForeground(TEXT_MAIN);
        header.add(feedTitle);
        feedPanel.add(header);
        feedPanel.add(Box.createVerticalStrut(8));

        List<PostData> posts = loadFeedPosts();
        if (posts.isEmpty()) {
            JLabel empty = new JLabel(emptyFeedText());
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            empty.setForeground(TEXT_MUTED);
            empty.setBorder(new EmptyBorder(24, 12, 24, 12));
            feedPanel.add(empty);
        }

        for (PostData post : posts) {
            feedPanel.add(buildPostCard(post));
            feedPanel.add(Box.createVerticalStrut(8));
        }

        feedPanel.revalidate();
        feedPanel.repaint();
    }

    private String emptyFeedText() {
        if (!searchQuery.isBlank()) {
            return "No posts found for \"" + searchQuery + "\".";
        }

        return selectedCommunityName == null ? "No posts found." : "No posts found in r/" + selectedCommunityName + ".";
    }

    private JPanel buildPostCard(PostData post) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(BG_PANEL);
        card.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL, 1),
            new EmptyBorder(12, 14, 12, 14)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));

        // Vote column
        JPanel voteCol = new JPanel();
        voteCol.setLayout(new GridLayout(3, 1, 0, 0));
        voteCol.setOpaque(false);
        voteCol.setPreferredSize(new Dimension(42, 0));

        JButton upBtn   = makeVoteBtn("▲", true);
        JLabel  score   = new JLabel(String.valueOf(post.score));
        score.setFont(new Font("Segoe UI", Font.BOLD, 13));
        score.setForeground(post.score > 100 ? NEON_CYAN : TEXT_MUTED);
        score.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton downBtn = makeVoteBtn("▼", false);
        final int[] currentVote = {
                Session.isLoggedIn() ? new VoteDAO().getPostUserVote(post.id, Session.getCurrentUserId()) : 0
        };
        updatePostVoteButtons(upBtn, downBtn, score, currentVote[0]);

        upBtn.addActionListener(e -> {
            if (!Session.isLoggedIn()) {
                JOptionPane.showMessageDialog(this, "Please log in first.", "Clixky", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int newScore = new VoteDAO().votePost(post.id, Session.getCurrentUserId(), 1);
            currentVote[0] = currentVote[0] == 1 ? 0 : 1;
            post.score = newScore;
            score.setText(String.valueOf(newScore));
            updatePostVoteButtons(upBtn, downBtn, score, currentVote[0]);
        });
        downBtn.addActionListener(e -> {
            if (!Session.isLoggedIn()) {
                JOptionPane.showMessageDialog(this, "Please log in first.", "Clixky", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int newScore = new VoteDAO().votePost(post.id, Session.getCurrentUserId(), -1);
            currentVote[0] = currentVote[0] == -1 ? 0 : -1;
            post.score = newScore;
            score.setText(String.valueOf(newScore));
            updatePostVoteButtons(upBtn, downBtn, score, currentVote[0]);
        });

        voteCol.add(upBtn);
        voteCol.add(score);
        voteCol.add(downBtn);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel titleLabel = new JLabel("<html><body style='width:500px'>" + post.title + "</body></html>");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_MAIN);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel meta = new JLabel("by  " + post.author + "  ·  " + post.subreddit + "  ·  " + post.time + "  ·  " + post.comments + " comments");
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        meta.setForeground(NEON_DIM);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(Box.createVerticalGlue());
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(meta);
        content.add(Box.createVerticalGlue());

        card.add(voteCol,  BorderLayout.WEST);
        card.add(content,  BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (onOpenPost != null) {
                    dispose();
                    onOpenPost.accept(post);
                }
            }
            public void mouseEntered(MouseEvent e) {
                card.setBackground(BG_CARD);
                card.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(new Color(NEON_PINK.getRed(), NEON_PINK.getGreen(), NEON_PINK.getBlue(), 120), 1),
                    new EmptyBorder(12, 14, 12, 14)
                ));
            }
            public void mouseExited(MouseEvent e) {
                card.setBackground(BG_PANEL);
                card.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COL, 1),
                    new EmptyBorder(12, 14, 12, 14)
                ));
            }
        });

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

    private JButton makeVoteBtn(String text, boolean isUp) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setForeground(isUp ? NEON_MID : TEXT_MUTED);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setPreferredSize(new Dimension(28, 22));
        b.setToolTipText(isUp ? "Upvote" : "Downvote");
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

    private void showCreatePostDialog() {
        JDialog dlg = new JDialog(this, "New Post", true);
        dlg.setSize(460, 420);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_PANEL);
        p.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel heading = new JLabel("Create a Post");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        heading.setForeground(NEON_PINK);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField titleField = makeTextField("Post title");
        JComboBox<PostDAO.CommunityOption> communityBox = new JComboBox<>();
        for (PostDAO.CommunityOption community : new PostDAO().getCommunities()) {
            communityBox.addItem(community);
        }
        communityBox.setBackground(BG_DEEP);
        communityBox.setForeground(TEXT_MAIN);
        communityBox.setFont(FONT_INPUT);
        communityBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        communityBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea  bodyArea   = new JTextArea(5, 20);
        bodyArea.setBackground(BG_DEEP);
        bodyArea.setForeground(TEXT_MAIN);
        bodyArea.setCaretColor(NEON_CYAN);
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

        JButton submit = makeButton("SUBMIT POST", BG_CARD, NEON_CYAN, BORDER_COL);
        submit.addActionListener(e -> {
            String title = titleField.getText().trim();
            String body = bodyArea.getText().trim();
            PostDAO.CommunityOption community = (PostDAO.CommunityOption) communityBox.getSelectedItem();

            if (!Session.isLoggedIn()) {
                SoundFX.error();
                JOptionPane.showMessageDialog(this, "Please log in first.", "Clixky", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (title.isEmpty() || body.isEmpty() || community == null) {
                SoundFX.error();
                JOptionPane.showMessageDialog(this, "Please fill in title, community, and body.", "Clixky", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int postId = new PostDAO().createPost(Session.getCurrentUserId(), community.getCommunityId(), title, body);
            if (postId == 0) {
                SoundFX.error();
                JOptionPane.showMessageDialog(this, "Post could not be created.", "Clixky", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SoundFX.success();
            dlg.dispose();
            dispose();
            new DashboardScreen(loggedInUser, onOpenPost, selectedCommunityId, selectedCommunityName);
        });

        p.add(heading);
        p.add(Box.createVerticalStrut(18));
        p.add(makeLabel("TITLE"));
        p.add(Box.createVerticalStrut(6));
        p.add(titleField);
        p.add(Box.createVerticalStrut(14));
        p.add(makeLabel("COMMUNITY"));
        p.add(Box.createVerticalStrut(6));
        p.add(communityBox);
        p.add(Box.createVerticalStrut(14));
        p.add(makeLabel("BODY"));
        p.add(Box.createVerticalStrut(6));
        p.add(bodyScroll);
        p.add(Box.createVerticalStrut(20));
        p.add(submit);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private void styleScrollBar(JScrollPane sp) {
        JScrollBar vsb = sp.getVerticalScrollBar();
        vsb.setBackground(BG_DEEP);
        vsb.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor  = new Color(NEON_PINK.getRed(), NEON_PINK.getGreen(), NEON_PINK.getBlue(), 80);
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
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return b;
    }

    // ── Database feed ─────────────────────────────────────────────────────────

    private List<PostData> loadFeedPosts() {
        List<PostData> list = new ArrayList<>();
        PostDAO postDAO = new PostDAO();

        for (PostDAO.FeedPost post : postDAO.getFeedPosts(selectedCommunityId)) {
            PostData postData = new PostData(
                    post.getPostId(),
                    post.getTitle(),
                    post.getBody(),
                    "u/" + post.getAuthor(),
                    "r/" + post.getCommunity(),
                    post.getCreatedAt(),
                    post.getScore(),
                    post.getCommentCount()
            );
            if (matchesSearch(postData)) {
                list.add(postData);
            }
        }

        return list;
    }

    private boolean matchesSearch(PostData post) {
        if (searchQuery.isBlank()) {
            return true;
        }

        String q = searchQuery.toLowerCase();
        return post.title.toLowerCase().contains(q)
                || post.body.toLowerCase().contains(q)
                || post.author.toLowerCase().contains(q)
                || post.subreddit.toLowerCase().contains(q)
                || post.time.toLowerCase().contains(q);
    }

    // ── PostData DTO ──────────────────────────────────────────────────────────

    public static class PostData {
        public int    id, score, comments;
        public String title, body, author, subreddit, time;
        public PostData(int id, String title, String body, String author, String subreddit, String time, int score, int comments) {
            this.id = id; this.title = title; this.body = body; this.author = author;
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
