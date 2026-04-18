package ui;

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

    // Indent per nesting level in pixels
    private static final int INDENT_PX = 18;

    // ── Comment DTO ───────────────────────────────────────────────────────────
    public static class CommentData {
        public int    id, parentId, score;
        public String author, body, timestamp;
        public List<CommentData> children = new ArrayList<>();

        public CommentData(int id, int parentId, String author, String body, String timestamp, int score) {
            this.id = id; this.parentId = parentId; this.author = author;
            this.body = body; this.timestamp = timestamp; this.score = score;
        }
    }

    // ── Build full tree panel from a flat list ─────────────────────────────
    public static JScrollPane buildTree(List<CommentData> flat, String currentUser, Runnable onReply) {
        // Build tree structure
        List<CommentData> roots = buildTree(flat);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(BG_DEEP);
        container.setBorder(new EmptyBorder(8, 0, 8, 0));

        // Section header
        JLabel header = new JLabel(flat.size() + " Comments");
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(GREEN_HI);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(header);

        // Render each root comment (with its children recursively)
        for (CommentData root : roots) {
            container.add(new CommentPanel(root, 0, currentUser, onReply));
            container.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.setBackground(BG_DEEP);
        scroll.getViewport().setBackground(BG_DEEP);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // Style scrollbar
        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = BG_CARD;
                this.trackColor = BG_DEEP;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });

        return scroll;
    }

    // ── Single comment node ────────────────────────────────────────────────
    private final CommentData data;
    private final int depth;
    private final String currentUser;
    private final Runnable onReply;
    private boolean collapsed = false;
    private JPanel childrenPanel;

    public CommentPanel(CommentData data, int depth, String currentUser, Runnable onReply) {
        this.data = data;
        this.depth = depth;
        this.currentUser = currentUser;
        this.onReply = onReply;
        buildNode();
    }

    private void buildNode() {
        setLayout(new BorderLayout(0, 4));
        setOpaque(false);

        // Left indent bar (depth > 0 shows a colored thread line)
        if (depth > 0) {
            JPanel indentBar = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Thread line color cycles slightly per depth
                    Color[] lineColors = {
                        new Color(30, 80, 30),
                        new Color(25, 65, 60),
                        new Color(40, 55, 80),
                        new Color(60, 40, 70)
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

        // Main comment block
        JPanel block = buildCommentBlock();
        add(block, BorderLayout.CENTER);
    }

    private JPanel buildCommentBlock() {
        JPanel outer = new JPanel(new BorderLayout(0, 4));
        outer.setOpaque(false);

        // Comment card
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(depth == 0 ? new Color(8, 20, 8) : new Color(6, 15, 6));
        card.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(depth == 0 ? BORDER_COL : new Color(15, 35, 15), 1),
            new EmptyBorder(10, 12, 10, 12)
        ));

        // Author row
        JPanel authorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        authorRow.setOpaque(false);

        // Small avatar
        JLabel avatar = new JLabel(data.author.substring(0, Math.min(2, data.author.length())).toUpperCase()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(GREEN_DIM);
                g2.drawOval(0, 0, getWidth()-1, getHeight()-1);
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(22, 22));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 8));
        avatar.setForeground(GREEN_MID);

        JLabel authorLabel = new JLabel(data.author);
        authorLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        authorLabel.setForeground(data.author.equals(currentUser) ? GOLD : GREEN_MID);

        // Score
        JLabel scoreLabel = new JLabel("▲ " + data.score);
        scoreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        scoreLabel.setForeground(data.score > 50 ? GREEN_HI : TEXT_MUTED);

        JLabel timeLabel = new JLabel("· " + data.timestamp);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(GREEN_DIM);

        // Collapse toggle
        JLabel collapseBtn = new JLabel(data.children.isEmpty() ? "" : "[–]");
        collapseBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        collapseBtn.setForeground(GREEN_DIM);
        collapseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        collapseBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { toggleCollapse(collapseBtn); }
            public void mouseEntered(MouseEvent e) { collapseBtn.setForeground(GREEN_HI); }
            public void mouseExited(MouseEvent e)  { collapseBtn.setForeground(GREEN_DIM); }
        });

        authorRow.add(avatar);
        authorRow.add(authorLabel);
        authorRow.add(scoreLabel);
        authorRow.add(timeLabel);
        authorRow.add(Box.createHorizontalStrut(8));
        authorRow.add(collapseBtn);

        // Comment body
        JLabel bodyLabel = new JLabel("<html><body style='width:480px; color:#5a8a5a; font-family:Segoe UI; font-size:12px; line-height:1.6'>"
            + data.body + "</body></html>");
        bodyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Action buttons: Reply, Upvote, Downvote
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.setOpaque(false);

        JButton upBtn    = makeActionBtn("▲ Upvote");
        JButton downBtn  = makeActionBtn("▼");
        JButton replyBtn = makeActionBtn("↩ Reply");

        upBtn.addActionListener(e -> {
            data.score++;
            scoreLabel.setText("▲ " + data.score);
            scoreLabel.setForeground(GREEN_HI);
        });
        downBtn.addActionListener(e -> {
            data.score--;
            scoreLabel.setText("▲ " + data.score);
        });
        replyBtn.addActionListener(e -> showReplyComposer(outer));

        actions.add(upBtn);
        actions.add(downBtn);
        actions.add(replyBtn);

        card.add(authorRow, BorderLayout.NORTH);
        card.add(bodyLabel, BorderLayout.CENTER);
        card.add(actions,   BorderLayout.SOUTH);

        outer.add(card, BorderLayout.NORTH);

        // Children panel
        childrenPanel = new JPanel();
        childrenPanel.setLayout(new BoxLayout(childrenPanel, BoxLayout.Y_AXIS));
        childrenPanel.setOpaque(false);
        childrenPanel.setBorder(new EmptyBorder(4, 0, 0, 0));

        for (CommentData child : data.children) {
            childrenPanel.add(new CommentPanel(child, depth + 1, currentUser, onReply));
            childrenPanel.add(Box.createVerticalStrut(5));
        }

        if (!data.children.isEmpty()) {
            outer.add(childrenPanel, BorderLayout.CENTER);
        }

        return outer;
    }

    private void toggleCollapse(JLabel btn) {
        collapsed = !collapsed;
        childrenPanel.setVisible(!collapsed);
        btn.setText(collapsed ? "[+]" : "[–]");
        revalidate();
        repaint();
    }

    private void showReplyComposer(JPanel parent) {
        // Remove any existing reply box first
        for (Component c : parent.getComponents()) {
            if ("replyBox".equals(c.getName())) {
                parent.remove(c);
                parent.revalidate();
                parent.repaint();
                return; // toggle off
            }
        }

        JPanel replyBox = new JPanel(new BorderLayout(0, 8));
        replyBox.setName("replyBox");
        replyBox.setBackground(BG_PANEL);
        replyBox.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(GREEN_DIM, 1),
            new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel replyLabel = new JLabel("Replying to " + data.author);
        replyLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        replyLabel.setForeground(GOLD);

        JTextArea input = new JTextArea(3, 30);
        input.setBackground(BG_DEEP);
        input.setForeground(TEXT_MAIN);
        input.setCaretColor(GREEN_HI);
        input.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
        submit.setForeground(GREEN_HI);

        cancel.addActionListener(e -> {
            parent.remove(replyBox);
            parent.revalidate();
            parent.repaint();
        });
        submit.addActionListener(e -> {
            String text = input.getText().trim();
            if (!text.isEmpty()) {
                // TODO: CommentDAO.addComment(postId, data.id, currentUser, text)
                parent.remove(replyBox);
                parent.revalidate();
                parent.repaint();
                if (onReply != null) onReply.run();
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
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setForeground(TEXT_MUTED);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            final Color orig = b.getForeground();
            public void mouseEntered(MouseEvent e) { b.setForeground(GREEN_HI); }
            public void mouseExited(MouseEvent e)  { b.setForeground(orig); }
        });
        return b;
    }

    // ── Tree builder: flat list → nested tree ─────────────────────────────
    public static List<CommentData> buildTree(List<CommentData> flat) {
        java.util.Map<Integer, CommentData> map = new java.util.LinkedHashMap<>();
        List<CommentData> roots = new ArrayList<>();
        for (CommentData c : flat) map.put(c.id, c);
        for (CommentData c : flat) {
            if (c.parentId == 0 || !map.containsKey(c.parentId)) {
                roots.add(c);
            } else {
                map.get(c.parentId).children.add(c);
            }
        }
        return roots;
    }

    // ── Sample comments ────────────────────────────────────────────────────
    public static List<CommentData> getSampleComments() {
        List<CommentData> list = new ArrayList<>();
        list.add(new CommentData(1, 0, "u/db_expert",    "BCNF matters when you have overlapping candidate keys. In practice, if a 3NF table has no overlapping keys, it is automatically in BCNF. For your DB course though, knowing the difference conceptually is enough.", "4h ago", 156));
        list.add(new CommentData(2, 1, "u/hamza_dev",    "That makes a lot of sense! So it is mostly an edge case in real schema design?", "3h ago", 34));
        list.add(new CommentData(3, 2, "u/db_expert",    "Exactly. You will rarely encounter it unless you have compound keys with partial dependencies. Good luck with CS-220!", "3h ago", 28));
        list.add(new CommentData(4, 0, "u/ali_codes",    "Good question for your Clixky project too — make sure your schema hits at least 3NF for the grade!", "2h ago", 89));
        list.add(new CommentData(5, 4, "u/aayan_a",      "Ha, our ER diagram already has 5 tables fully normalized. Ask me anything!", "1h ago", 12));
        list.add(new CommentData(6, 0, "u/sql_nerd",     "One practical place BCNF shows up: scheduling tables with course/room/time dependencies. Worth knowing for interviews.", "5h ago", 44));
        list.add(new CommentData(7, 6, "u/hamza_dev",    "Oh interesting, I have a data structures exam next week too. Thanks!", "4h ago", 8));
        return list;
    }
}
