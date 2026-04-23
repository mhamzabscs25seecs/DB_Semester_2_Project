package ui;

import dao.PostDAO;
import dao.Session;
import dao.UserDAO;
import dao.VoteDAO;
import dao.CommunityDAO;
import dao.ReportDAO;

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

        JButton newCommunityBtn = makeSmallButton("+ New Community", BG_CARD, NEON_PINK, BORDER_COL);
        newCommunityBtn.addActionListener(e -> showCreateCommunityDialog());

        JButton browseBtn = makeSmallButton("Browse", BG_CARD, NEON_CYAN, BORDER_COL);
        browseBtn.addActionListener(e -> showBrowseCommunitiesDialog());
        JButton adminBtn = makeSmallButton("Admin", BG_CARD, GOLD, BORDER_COL);
        adminBtn.addActionListener(e -> showAdminDialog());
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

        actions.add(newCommunityBtn);
        actions.add(newPostBtn);
        actions.add(browseBtn);
        if (Session.isAdmin()) {
            actions.add(adminBtn);
        }
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

    private JComponent buildBody() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildSidebar(), buildFeed());
        split.setResizeWeight(0);
        split.setDividerLocation(280);
        split.setOneTouchExpandable(true);
        split.setContinuousLayout(true);
        split.setBorder(null);
        split.setBackground(BG_DEEP);
        split.setDividerSize(7);
        split.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(BG_DEEP);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.setColor(new Color(NEON_PINK.getRed(), NEON_PINK.getGreen(), NEON_PINK.getBlue(), 65));
                        int x = getWidth() / 2;
                        g.drawLine(x, 0, x, getHeight());
                    }
                };
            }
        });
        return split;
    }

    // ── SIDEBAR ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_DEEP);
        sidebar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 0, 1, new Color(NEON_PINK.getRed(), NEON_PINK.getGreen(), NEON_PINK.getBlue(), 50)),
            new EmptyBorder(18, 0, 18, 0)
        ));
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setMinimumSize(new Dimension(230, 0));

        sidebar.add(sidebarSection("MY COMMUNITIES"));
        sidebarList = new JPanel();
        sidebarList.setLayout(new BoxLayout(sidebarList, BoxLayout.Y_AXIS));
        sidebarList.setOpaque(false);

        sidebarList.add(makeSidebarItem("Home Feed", selectedCommunityId == null, NEON_CYAN, null, null));

        java.util.List<CommunityDAO.CommunitySummary> communities =
                new CommunityDAO().getCommunitySummaries(Session.getCurrentUserId());

        boolean hasJoined = false;
        for (CommunityDAO.CommunitySummary community : communities) {
            if (!community.isJoined()) {
                continue;
            }

            hasJoined = true;
            boolean active = selectedCommunityId != null && selectedCommunityId == community.getCommunityId();
            sidebarList.add(makeSidebarItem(
                    "r/" + community.getCommunityName(),
                    active,
                    NEON_CYAN,
                    community.getCommunityId(),
                    community.getCommunityName()
            ));
        }

        if (!hasJoined) {
            sidebarList.add(sidebarHint("No joined communities yet."));
        }
        sidebar.add(sidebarList);

        JPanel suggestedList = new JPanel();
        suggestedList.setLayout(new BoxLayout(suggestedList, BoxLayout.Y_AXIS));
        suggestedList.setOpaque(false);

        boolean hasSuggested = false;
        for (CommunityDAO.CommunitySummary community :
                communities) {
            if (community.isJoined()) {
                continue;
            }

            hasSuggested = true;
            boolean active = selectedCommunityId != null && selectedCommunityId == community.getCommunityId();
            suggestedList.add(makeSidebarItem(
                    "r/" + community.getCommunityName(),
                    active,
                    GOLD,
                    community.getCommunityId(),
                    community.getCommunityName()
            ));
        }

        if (hasSuggested) {
            sidebar.add(Box.createVerticalStrut(16));
            sidebar.add(sidebarSection("SUGGESTED COMMUNITIES"));
            sidebar.add(suggestedList);
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JLabel sidebarHint(String text) {
        JLabel hint = new JLabel(text);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hint.setForeground(TEXT_MUTED);
        hint.setBorder(new EmptyBorder(4, 20, 10, 14));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return hint;
    }

    private JLabel sidebarSection(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(NEON_DIM);
        l.setBorder(new EmptyBorder(0, 20, 10, 16));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
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
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setOpaque(true);
        item.setBackground(active ? BG_CARD : BG_DEEP);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        if (active) {
            item.setBorder(new CompoundBorder(
                    new MatteBorder(0, 3, 0, 0, NEON_PINK),
                    new EmptyBorder(0, 16, 0, 12)
            ));
        } else {
            item.setBorder(new EmptyBorder(0, 19, 0, 12));
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
        dot.setPreferredSize(new Dimension(10, 18));

        JLabel label = new JLabel(name);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        label.setForeground(active ? NEON_CYAN : TEXT_MUTED);
        label.setToolTipText(name);

        item.add(dot, BorderLayout.WEST);
        item.add(label, BorderLayout.CENTER);

        MouseAdapter openCommunity = new MouseAdapter() {
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
        };
        item.addMouseListener(openCommunity);
        dot.addMouseListener(openCommunity);
        label.addMouseListener(openCommunity);

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

        List<CommunityDAO.CommunitySummary> communities = loadCommunitySearchResults();
        if (!communities.isEmpty()) {
            JLabel communityHeader = new JLabel("Communities");
            communityHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
            communityHeader.setForeground(NEON_PINK);
            communityHeader.setBorder(new EmptyBorder(6, 0, 6, 0));
            feedPanel.add(communityHeader);

            for (CommunityDAO.CommunitySummary community : communities) {
                feedPanel.add(buildSearchCommunityCard(community));
                feedPanel.add(Box.createVerticalStrut(8));
            }
            feedPanel.add(Box.createVerticalStrut(6));
        }

        List<PostData> posts = loadFeedPosts();
        if (!posts.isEmpty()) {
            JLabel postHeader = new JLabel(searchQuery.isBlank() ? "Posts" : "Matching Posts");
            postHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
            postHeader.setForeground(NEON_PINK);
            postHeader.setBorder(new EmptyBorder(6, 0, 6, 0));
            feedPanel.add(postHeader);
        }

        if (posts.isEmpty() && communities.isEmpty()) {
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
            return "No posts or communities found for \"" + searchQuery + "\".";
        }

        return selectedCommunityName == null ? "No posts found." : "No posts found in r/" + selectedCommunityName + ".";
    }

    private JPanel buildSearchCommunityCard(CommunityDAO.CommunitySummary community) {
        JPanel card = new JPanel(new BorderLayout(18, 0));
        card.setBackground(BG_PANEL);
        card.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL, 1),
                new EmptyBorder(16, 18, 16, 18)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 124));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel name = new JLabel("r/" + community.getCommunityName());
        name.setFont(new Font("Segoe UI", Font.BOLD, 18));
        name.setForeground(NEON_CYAN);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        String description = community.getDescription();
        if (description == null || description.isBlank()) {
            description = "No description added yet.";
        }

        JLabel desc = new JLabel("<html><body style='width:620px; font-family:Segoe UI; font-size:13px; color:"
                + htmlColor(TEXT_MUTED) + "; line-height:1.45'>" + escapeHtml(description) + "</body></html>");
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(name);
        text.add(Box.createVerticalStrut(7));
        text.add(desc);

        JPanel info = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        info.setOpaque(false);
        info.add(makeMetric(String.valueOf(community.getMemberCount()), "Members"));
        info.add(makeMetric(String.valueOf(community.getPostCount()), "Posts"));
        info.add(makeMetric(community.isJoined() ? "Joined" : "Open", "Status"));

        JButton open = makeSmallButton("View Community", BG_CARD, NEON_CYAN, BORDER_COL);
        open.addActionListener(e -> openCommunity(community));

        JPanel actionCol = new JPanel(new BorderLayout(0, 12));
        actionCol.setOpaque(false);
        actionCol.add(info, BorderLayout.CENTER);
        actionCol.add(open, BorderLayout.SOUTH);

        card.add(text, BorderLayout.CENTER);
        card.add(actionCol, BorderLayout.EAST);
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                openCommunity(community);
            }
            public void mouseEntered(MouseEvent e) {
                card.setBackground(BG_CARD);
            }
            public void mouseExited(MouseEvent e) {
                card.setBackground(BG_PANEL);
            }
        });

        return card;
    }

    private JPanel makeMetric(String value, String label) {
        JPanel metric = new JPanel();
        metric.setLayout(new BoxLayout(metric, BoxLayout.Y_AXIS));
        metric.setOpaque(false);
        metric.setPreferredSize(new Dimension(72, 44));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        valueLabel.setForeground(NEON_CYAN);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        labelText.setForeground(NEON_DIM);
        labelText.setAlignmentX(Component.CENTER_ALIGNMENT);

        metric.add(valueLabel);
        metric.add(Box.createVerticalStrut(2));
        metric.add(labelText);
        return metric;
    }

    private void openCommunity(CommunityDAO.CommunitySummary community) {
        SoundFX.click();
        dispose();
        new DashboardScreen(loggedInUser, onOpenPost, community.getCommunityId(), community.getCommunityName());
    }

    private JPanel buildPostCard(PostData post) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
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

        JButton viewPost = makeSmallButton(searchQuery.isBlank() ? "View" : "View Post", BG_CARD, NEON_CYAN, BORDER_COL);
        viewPost.addActionListener(e -> openPost(post));
        JPanel actionWrap = new JPanel(new GridBagLayout());
        actionWrap.setOpaque(false);
        actionWrap.setPreferredSize(new Dimension(searchQuery.isBlank() ? 92 : 120, 0));
        actionWrap.add(viewPost);
        card.add(actionWrap, BorderLayout.EAST);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                SoundFX.click();
                openPost(post);
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

    private void openPost(PostData post) {
        if (onOpenPost != null) {
            dispose();
            onOpenPost.accept(post);
        }
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
        registerEscapeToClose(dlg);

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

    private void showCreateCommunityDialog() {
        JDialog dlg = new JDialog(this, "New Community", true);
        dlg.setSize(440, 320);
        dlg.setLocationRelativeTo(this);
        registerEscapeToClose(dlg);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_PANEL);
        p.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel heading = new JLabel("Create Community");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        heading.setForeground(NEON_PINK);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nameField = makeTextField("Community name");
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea descriptionArea = new JTextArea(4, 20);
        descriptionArea.setBackground(BG_DEEP);
        descriptionArea.setForeground(TEXT_MAIN);
        descriptionArea.setCaretColor(NEON_CYAN);
        descriptionArea.setFont(FONT_INPUT);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(null);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton create = makeButton("CREATE COMMUNITY", BG_CARD, NEON_CYAN, BORDER_COL);
        create.addActionListener(e -> {
            String name = getFieldText(nameField);
            String description = descriptionArea.getText().trim();

            if (!Session.isLoggedIn()) {
                SoundFX.error();
                showCyberError(this, "Login Required", "Please log in first.");
                return;
            }

            if (name.isEmpty()) {
                SoundFX.error();
                showCyberError(this, "Missing Name", "Please enter a community name.");
                return;
            }

            if (name.length() > 50) {
                SoundFX.error();
                showCyberError(this, "Name Too Long", "Community name must be 50 characters or less.");
                return;
            }

            if (description.length() > 300) {
                SoundFX.error();
                showCyberError(this, "Description Too Long", "Description must be 300 characters or less.");
                return;
            }

            CommunityDAO communityDAO = new CommunityDAO();
            if (communityDAO.communityNameExists(name)) {
                SoundFX.error();
                showCyberError(this, "Community Exists", "A community with this name already exists.");
                return;
            }

            int communityId = communityDAO.createCommunity(
                    Session.getCurrentUserId(),
                    name,
                    description
            );

            if (communityId == 0) {
                SoundFX.error();
                showCyberError(this, "Create Failed", "Community could not be created.");
                return;
            }

            SoundFX.success();
            dlg.dispose();
            dispose();
            new DashboardScreen(loggedInUser, onOpenPost, communityId, name);
        });

        p.add(heading);
        p.add(Box.createVerticalStrut(18));
        p.add(makeLabel("COMMUNITY NAME"));
        p.add(Box.createVerticalStrut(6));
        p.add(nameField);
        p.add(Box.createVerticalStrut(14));
        p.add(makeLabel("DESCRIPTION"));
        p.add(Box.createVerticalStrut(6));
        p.add(descScroll);
        p.add(Box.createVerticalStrut(20));
        p.add(create);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private void showBrowseCommunitiesDialog() {
        if (!Session.isLoggedIn()) {
            SoundFX.error();
            showCyberError(this, "Login Required", "Please log in first.");
            return;
        }

        JDialog dlg = new JDialog(this, "Browse Communities", true);
        dlg.setSize(820, 640);
        dlg.setMinimumSize(new Dimension(720, 560));
        dlg.setLocationRelativeTo(this);
        registerEscapeToClose(dlg);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(BG_PANEL);
        root.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel heading = new JLabel("Browse Communities");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(NEON_PINK);

        JLabel subheading = new JLabel("Find communities by topic, see what they are about, and join the ones you want in your sidebar.");
        subheading.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subheading.setForeground(TEXT_MUTED);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(heading);
        header.add(Box.createVerticalStrut(4));
        header.add(subheading);
        header.add(Box.createVerticalStrut(14));

        JTextField search = makeTextField("Search communities...");
        search.setMaximumSize(new Dimension(360, 34));
        search.setPreferredSize(new Dimension(320, 34));
        search.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(search);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(BG_DEEP);
        list.setBorder(new EmptyBorder(10, 10, 10, 10));

        refreshBrowseCommunityList(dlg, list, "");
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }

            private void refresh() {
                refreshBrowseCommunityList(dlg, list, getSearchText(search));
            }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COL));
        scroll.getViewport().setBackground(BG_DEEP);
        styleScrollBar(scroll);

        JButton close = makeSmallButton("Close", BG_CARD, TEXT_MUTED, BORDER_COL);
        close.addActionListener(e -> dlg.dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(close);

        root.add(header, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private void refreshBrowseCommunityList(JDialog dialog, JPanel list, String query) {
        list.removeAll();
        int shown = 0;

        for (CommunityDAO.CommunitySummary community :
                new CommunityDAO().getCommunitySummaries(Session.getCurrentUserId())) {
            String description = community.getDescription() == null ? "" : community.getDescription();
            String createdAt = community.getCreatedAt() == null ? "" : community.getCreatedAt();
            if (!matchesBrowseCommunitySearch(query, community.getCommunityName(), description, createdAt)) {
                continue;
            }

            list.add(makeCommunityBrowseCard(dialog, community));
            list.add(Box.createVerticalStrut(10));
            shown++;
        }

        if (shown == 0) {
            JLabel empty = new JLabel("No communities match your search.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            empty.setForeground(TEXT_MUTED);
            empty.setBorder(new EmptyBorder(18, 12, 18, 12));
            list.add(empty);
        }

        list.revalidate();
        list.repaint();
    }

    private boolean matchesBrowseCommunitySearch(String query, String name, String description, String createdAt) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String q = query.toLowerCase();
        return name.toLowerCase().contains(q)
                || description.toLowerCase().contains(q)
                || createdAt.toLowerCase().contains(q);
    }

    private JPanel makeCommunityBrowseCard(JDialog dialog, CommunityDAO.CommunitySummary community) {
        JPanel card = new RoundPanel(8, BG_PANEL, BORDER_COL);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));

        JLabel name = new JLabel("r/" + community.getCommunityName());
        name.setFont(new Font("Segoe UI", Font.BOLD, 16));
        name.setForeground(NEON_CYAN);

        String description = community.getDescription();
        if (description == null || description.isBlank()) {
            description = "No description added yet.";
        }

        JLabel desc = new JLabel("<html><body style='width:330px; font-family:Segoe UI; font-size:12px; color:"
                + htmlColor(TEXT_MUTED) + "'>" + escapeHtml(description) + "</body></html>");

        JLabel stats = new JLabel(community.getMemberCount() + " members  ·  "
                + community.getPostCount() + " posts  ·  Created " + community.getCreatedAt());
        stats.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        stats.setForeground(NEON_DIM);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(name);
        text.add(Box.createVerticalStrut(6));
        text.add(desc);
        text.add(Box.createVerticalStrut(8));
        text.add(stats);

        JButton open = makeSmallButton("Open", BG_CARD, NEON_CYAN, BORDER_COL);
        open.addActionListener(e -> {
            SoundFX.click();
            dialog.dispose();
            dispose();
            new DashboardScreen(loggedInUser, onOpenPost, community.getCommunityId(), community.getCommunityName());
        });

        JButton membership = makeSmallButton(community.isJoined() ? "Joined" : "Join",
                community.isJoined() ? BG_DEEP : BG_CARD,
                community.isJoined() ? NEON_PINK : NEON_CYAN,
                BORDER_COL);
        membership.addActionListener(e -> {
            CommunityDAO dao = new CommunityDAO();
            boolean ok = community.isJoined()
                    ? dao.leaveCommunity(Session.getCurrentUserId(), community.getCommunityId())
                    : dao.joinCommunity(Session.getCurrentUserId(), community.getCommunityId());

            if (!ok) {
                SoundFX.error();
                showCyberError(this, "Update Failed", "Community membership could not be updated.");
                return;
            }

            SoundFX.success();
            dialog.dispose();
            showBrowseCommunitiesDialog();
        });

        JButton report = makeSmallButton("Report", BG_DEEP, TEXT_MUTED, BORDER_COL);
        report.addActionListener(e -> reportCommunity(community));

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.add(open);
        actions.add(Box.createVerticalStrut(8));
        actions.add(membership);
        actions.add(Box.createVerticalStrut(8));
        actions.add(report);

        card.add(text, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);
        return card;
    }

    private void reportCommunity(CommunityDAO.CommunitySummary community) {
        if (!Session.isLoggedIn()) {
            SoundFX.error();
            showCyberError(this, "Login Required", "Please log in first.");
            return;
        }

        ReportInput report = showReportDialog(this, "Community");
        if (report == null) {
            return;
        }

        boolean ok = new ReportDAO().reportCommunity(
                Session.getCurrentUserId(),
                community.getCommunityId(),
                report.getReason(),
                report.getDetails()
        );
        if (!ok) {
            SoundFX.error();
            showCyberError(this, "Report Failed", "Community report could not be submitted.");
            return;
        }

        SoundFX.success();
        JOptionPane.showMessageDialog(this, "Report submitted.", "Clixky", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAdminDialog() {
        if (!Session.isAdmin()) {
            SoundFX.error();
            showCyberError(this, "Access Denied", "Only admins can open this panel.");
            return;
        }

        JDialog dlg = new JDialog(this, "Clixky Admin", true);
        dlg.setSize(820, 620);
        dlg.setLocationRelativeTo(this);
        registerEscapeToClose(dlg);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(BG_PANEL);
        root.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel heading = new JLabel("Admin Management");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        heading.setForeground(GOLD);

        JLabel subheading = new JLabel("Manage users, communities, and posts. Deletes cascade through the database rules.");
        subheading.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subheading.setForeground(TEXT_MUTED);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(heading);
        header.add(Box.createVerticalStrut(4));
        header.add(subheading);
        header.add(Box.createVerticalStrut(14));

        JTextField adminSearch = makeTextField("Search users, communities, posts...");
        adminSearch.setMaximumSize(new Dimension(360, 34));
        adminSearch.setPreferredSize(new Dimension(320, 34));
        adminSearch.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(adminSearch);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG_PANEL);
        tabs.setForeground(TEXT_MAIN);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        refreshAdminTabs(dlg, tabs, "");
        adminSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }

            private void refresh() {
                int selectedIndex = Math.max(0, tabs.getSelectedIndex());
                refreshAdminTabs(dlg, tabs, getSearchText(adminSearch));
                tabs.setSelectedIndex(Math.min(selectedIndex, tabs.getTabCount() - 1));
            }
        });

        JButton close = makeSmallButton("Close", BG_CARD, TEXT_MUTED, BORDER_COL);
        close.addActionListener(e -> dlg.dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(close);

        root.add(header, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private void refreshAdminTabs(JDialog dialog, JTabbedPane tabs, String query) {
        tabs.removeAll();
        tabs.addTab("Users", buildAdminUsersPanel(dialog, query));
        tabs.addTab("Communities", buildAdminCommunitiesPanel(dialog, query));
        tabs.addTab("Posts", buildAdminPostsPanel(dialog, query));
        tabs.addTab("Reports", buildAdminReportsPanel(query));
    }

    private JScrollPane buildAdminUsersPanel(JDialog dialog, String query) {
        JPanel list = adminListPanel();
        int shown = 0;
        for (UserDAO.AdminUserRow user : new UserDAO().getAdminUserRows()) {
            String title = "@" + user.getUsername() + "  [" + user.getRole() + "]";
            String detail = user.getEmail() + "  |  " + user.getPostCount() + " posts  |  "
                    + user.getCommentCount() + " comments  |  "
                    + user.getCommunityCount() + " communities";
            if (!matchesAdminSearch(query, title, detail)) {
                continue;
            }

            JPanel row = adminRow(
                    title,
                    detail,
                    user.getUserId() == Session.getCurrentUserId() || "admin".equalsIgnoreCase(user.getRole())
                            ? "Protected"
                            : "Delete User",
                    user.getUserId() == Session.getCurrentUserId() || "admin".equalsIgnoreCase(user.getRole()),
                    () -> {
                        if (confirmAdminDelete("Delete user @" + user.getUsername()
                                + "?\nTheir profile, posts, comments, votes, and memberships will be removed.")) {
                            boolean deleted = new UserDAO().deleteUserByAdmin(
                                    Session.getCurrentUserId(),
                                    user.getUserId()
                            );
                            afterAdminDelete(dialog, deleted, "User deleted.", "User could not be deleted.");
                        }
                    }
            );
            list.add(row);
            list.add(Box.createVerticalStrut(10));
            shown++;
        }
        addEmptyAdminHint(list, shown, "No users match your search.");
        return adminScroll(list);
    }

    private JScrollPane buildAdminCommunitiesPanel(JDialog dialog, String query) {
        JPanel list = adminListPanel();
        int shown = 0;
        for (CommunityDAO.CommunitySummary community :
                new CommunityDAO().getCommunitySummaries(Session.getCurrentUserId())) {
            String title = "r/" + community.getCommunityName();
            String detail = nullToFallback(community.getDescription(), "No description.") + "  |  "
                    + community.getMemberCount() + " members  |  "
                    + community.getPostCount() + " posts";
            if (!matchesAdminSearch(query, title, detail)) {
                continue;
            }

            JPanel row = adminRow(
                    title,
                    detail,
                    "Delete Community",
                    false,
                    () -> {
                        if (confirmAdminDelete("Delete r/" + community.getCommunityName()
                                + "?\nAll posts, comments, memberships, and votes in this community will be removed.")) {
                            boolean deleted = new CommunityDAO().deleteCommunityByAdmin(
                                    Session.getCurrentUserId(),
                                    community.getCommunityId()
                            );
                            afterAdminDelete(dialog, deleted, "Community deleted.", "Community could not be deleted.");
                        }
                    }
            );
            list.add(row);
            list.add(Box.createVerticalStrut(10));
            shown++;
        }
        addEmptyAdminHint(list, shown, "No communities match your search.");
        return adminScroll(list);
    }

    private JScrollPane buildAdminPostsPanel(JDialog dialog, String query) {
        JPanel list = adminListPanel();
        int shown = 0;
        for (PostDAO.AdminPostRow post : new PostDAO().getAdminPostRows()) {
            String title = post.getTitle();
            String detail = "u/" + post.getAuthor() + "  |  r/" + post.getCommunity() + "  |  "
                    + post.getCreatedAt() + "  |  score " + post.getScore() + "  |  "
                    + post.getCommentCount() + " comments";
            if (!matchesAdminSearch(query, title, detail)) {
                continue;
            }

            JPanel row = adminRow(
                    title,
                    detail,
                    "Delete Post",
                    false,
                    () -> {
                        if (confirmAdminDelete("Delete post \"" + post.getTitle()
                                + "\"?\nIts comments and votes will also be removed.")) {
                            boolean deleted = new PostDAO().deletePostByAdmin(
                                    Session.getCurrentUserId(),
                                    post.getPostId()
                            );
                            afterAdminDelete(dialog, deleted, "Post deleted.", "Post could not be deleted.");
                        }
                    }
            );
            list.add(row);
            list.add(Box.createVerticalStrut(10));
            shown++;
        }
        addEmptyAdminHint(list, shown, "No posts match your search.");
        return adminScroll(list);
    }

    private JScrollPane buildAdminReportsPanel(String query) {
        JPanel list = adminListPanel();
        int shown = 0;
        for (ReportDAO.AdminReportRow report : new ReportDAO().getAdminReportRows()) {
            String title = "#" + report.getReportId() + "  " + report.getTargetType()
                    + " report  [" + report.getStatus() + "]";
            String detail = "reported by u/" + report.getReporter()
                    + "  |  target " + report.getTargetId()
                    + "  |  " + nullToFallback(report.getTargetTitle(), "Target no longer exists")
                    + "  |  reason: " + report.getReason()
                    + "  |  " + report.getCreatedAt();
            if (report.getDetails() != null && !report.getDetails().isBlank()) {
                detail += "  |  " + report.getDetails();
            }
            if (!matchesAdminSearch(query, title, detail)) {
                continue;
            }

            list.add(adminInfoRow(title, detail));
            list.add(Box.createVerticalStrut(10));
            shown++;
        }
        addEmptyAdminHint(list, shown, "No reports match your search.");
        return adminScroll(list);
    }

    private boolean matchesAdminSearch(String query, String title, String detail) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String q = query.toLowerCase();
        return (title != null && title.toLowerCase().contains(q))
                || (detail != null && detail.toLowerCase().contains(q));
    }

    private void addEmptyAdminHint(JPanel list, int shown, String message) {
        if (shown > 0) {
            return;
        }

        JLabel empty = new JLabel(message);
        empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        empty.setForeground(TEXT_MUTED);
        empty.setBorder(new EmptyBorder(18, 12, 18, 12));
        list.add(empty);
    }

    private JPanel adminListPanel() {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(BG_DEEP);
        list.setBorder(new EmptyBorder(12, 12, 12, 12));
        return list;
    }

    private JScrollPane adminScroll(JPanel list) {
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COL));
        scroll.getViewport().setBackground(BG_DEEP);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(scroll);
        return scroll;
    }

    private JPanel adminRow(String title, String detail, String actionText, boolean disabled, Runnable action) {
        JPanel row = new RoundPanel(8, BG_PANEL, BORDER_COL);
        row.setLayout(new BorderLayout(14, 0));
        row.setBorder(new EmptyBorder(14, 16, 14, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));

        JLabel titleLabel = new JLabel("<html><body style='width:500px; font-family:Segoe UI; font-size:14px; font-weight:bold; color:"
                + htmlColor(TEXT_MAIN) + "'>" + escapeHtml(title) + "</body></html>");

        JLabel detailLabel = new JLabel("<html><body style='width:520px; font-family:Segoe UI; font-size:12px; color:"
                + htmlColor(TEXT_MUTED) + "'>" + escapeHtml(detail) + "</body></html>");

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(6));
        text.add(detailLabel);

        JButton actionBtn = makeSmallButton(actionText, disabled ? BG_DEEP : BG_CARD,
                disabled ? TEXT_MUTED : NEON_PINK, BORDER_COL);
        actionBtn.setEnabled(!disabled);
        actionBtn.addActionListener(e -> action.run());

        row.add(text, BorderLayout.CENTER);
        row.add(actionBtn, BorderLayout.EAST);
        return row;
    }

    private JPanel adminInfoRow(String title, String detail) {
        JPanel row = new RoundPanel(8, BG_PANEL, BORDER_COL);
        row.setLayout(new BorderLayout(14, 0));
        row.setBorder(new EmptyBorder(14, 16, 14, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));

        JLabel titleLabel = new JLabel("<html><body style='width:620px; font-family:Segoe UI; font-size:14px; font-weight:bold; color:"
                + htmlColor(TEXT_MAIN) + "'>" + escapeHtml(title) + "</body></html>");

        JLabel detailLabel = new JLabel("<html><body style='width:640px; font-family:Segoe UI; font-size:12px; color:"
                + htmlColor(TEXT_MUTED) + "'>" + escapeHtml(detail) + "</body></html>");

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(6));
        text.add(detailLabel);

        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private boolean confirmAdminDelete(String message) {
        return showCyberConfirm(
                this,
                "Confirm Delete",
                message,
                "DELETE"
        );
    }

    private void afterAdminDelete(JDialog dialog, boolean success, String successMessage, String failureMessage) {
        if (!success) {
            SoundFX.error();
            showCyberError(this, "Admin Action Failed", failureMessage);
            return;
        }

        SoundFX.success();
        JOptionPane.showMessageDialog(this, successMessage, "Clixky Admin", JOptionPane.INFORMATION_MESSAGE);
        dialog.dispose();
        dispose();
        new DashboardScreen(loggedInUser, onOpenPost, selectedCommunityId, selectedCommunityName);
    }

    private String getFieldText(JTextField field) {
        return field.getForeground().equals(NEON_DIM) ? "" : field.getText().trim();
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

    private void registerEscapeToClose(JDialog dialog) {
        JRootPane rootPane = dialog.getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                "closeDialog"
        );
        rootPane.getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
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
        int width = Math.max(100, text.length() * 9 + 28);
        b.setMaximumSize(new Dimension(width + 10, 32));
        b.setPreferredSize(new Dimension(width, 32));
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
                    post.getAuthorId(),
                    post.getCommunityId(),
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

    private List<CommunityDAO.CommunitySummary> loadCommunitySearchResults() {
        List<CommunityDAO.CommunitySummary> results = new ArrayList<>();
        if (searchQuery.isBlank()) {
            return results;
        }

        String q = searchQuery.toLowerCase();
        for (CommunityDAO.CommunitySummary community :
                new CommunityDAO().getCommunitySummaries(Session.getCurrentUserId())) {
            String description = community.getDescription() == null ? "" : community.getDescription();
            if (community.getCommunityName().toLowerCase().contains(q)
                    || description.toLowerCase().contains(q)) {
                results.add(community);
            }
        }

        return results;
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
        public int    id, authorId, communityId, score, comments;
        public String title, body, author, subreddit, time;
        public PostData(int id, int authorId, int communityId, String title, String body, String author, String subreddit, String time, int score, int comments) {
            this.id = id; this.authorId = authorId; this.communityId = communityId;
            this.title = title; this.body = body; this.author = author;
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
