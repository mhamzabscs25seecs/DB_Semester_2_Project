package ui;

import dao.MessageDAO;
import dao.Session;
import dao.UserFollowDAO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import static ui.LoginScreen.*;

public class ChatWindow extends JFrame {
    private static ChatWindow openWindow;

    private final MessageDAO messageDAO = new MessageDAO();
    private final DefaultListModel<MessageDAO.ChatUser> userModel = new DefaultListModel<>();
    private final JList<MessageDAO.ChatUser> userList = new JList<>(userModel);
    private final JPanel messagesPanel = new JPanel();
    private final JTextArea composer = new JTextArea(3, 30);
    private final JLabel conversationTitle = new JLabel("Select a conversation");
    private final JButton followButton = DashboardScreen.makeSmallButton("Follow", BG_CARD, NEON_PINK, BORDER_COL);
    private final JLabel listStatus = new JLabel(" ");
    private final JTextField searchField = makeTextField("Search users...");
    private MessageDAO.ChatUser selectedUser;
    private String currentSearchQuery = "";
    private javax.swing.Timer refreshTimer;

    public static void showChat() {
        if (!Session.isLoggedIn()) {
            JOptionPane.showMessageDialog(null, "Please log in first.", "Clixky", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (openWindow == null || !openWindow.isDisplayable()) {
            openWindow = new ChatWindow();
        }

        openWindow.setVisible(true);
        openWindow.toFront();
        openWindow.requestFocus();
    }

    private ChatWindow() {
        buildUI();
        loadUsers();
        loadConversation();
        refreshTimer = new javax.swing.Timer(2500, e -> refreshCurrentConversation());
        refreshTimer.start();
    }

    private void buildUI() {
        setTitle("Clixky Messages");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(820, 560);
        setMinimumSize(new Dimension(680, 460));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DEEP);
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);
        setContentPane(root);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                if (refreshTimer != null) {
                    refreshTimer.stop();
                }
                openWindow = null;
            }
        });
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel("Messages");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(NEON_PINK);

        JButton close = DashboardScreen.makeSmallButton("Close", BG_CARD, TEXT_MUTED, BORDER_COL);
        close.addActionListener(e -> dispose());

        header.add(title, BorderLayout.WEST);
        header.add(close, BorderLayout.EAST);
        return header;
    }

    private JSplitPane buildBody() {
        JPanel left = buildConversationList();
        JPanel right = buildConversationPane();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(250);
        split.setResizeWeight(0);
        split.setBorder(BorderFactory.createLineBorder(BORDER_COL, 1));
        split.setBackground(BG_DEEP);
        return split;
    }

    private JPanel buildConversationList() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        searchField.setPreferredSize(new Dimension(0, 34));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateSearch(); }
            public void removeUpdate(DocumentEvent e) { updateSearch(); }
            public void changedUpdate(DocumentEvent e) { updateSearch(); }

            private void updateSearch() {
                SwingUtilities.invokeLater(() -> {
                    currentSearchQuery = getSearchText();
                    loadUsers();
                });
            }
        });

        listStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        listStatus.setForeground(TEXT_MUTED);
        listStatus.setBorder(new EmptyBorder(0, 2, 0, 0));

        JPanel searchBox = new JPanel(new BorderLayout(0, 6));
        searchBox.setOpaque(false);
        searchBox.add(searchField, BorderLayout.NORTH);
        searchBox.add(listStatus, BorderLayout.SOUTH);

        userList.setBackground(BG_PANEL);
        userList.setForeground(TEXT_MAIN);
        userList.setSelectionBackground(BG_CARD);
        userList.setSelectionForeground(NEON_CYAN);
        userList.setFixedCellHeight(58);
        userList.setCellRenderer(new ChatUserRenderer());
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedUser = userList.getSelectedValue();
                loadConversation();
            }
        });

        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBorder(BorderFactory.createLineBorder(BORDER_COL, 1));
        userScroll.getViewport().setBackground(BG_PANEL);

        panel.add(searchBox, BorderLayout.NORTH);
        panel.add(userScroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildConversationPane() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_DEEP);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel conversationHeader = new JPanel(new BorderLayout(8, 0));
        conversationHeader.setOpaque(false);

        conversationTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        conversationTitle.setForeground(NEON_CYAN);
        followButton.setVisible(false);
        followButton.addActionListener(e -> toggleSelectedFollow());
        conversationHeader.add(conversationTitle, BorderLayout.WEST);
        conversationHeader.add(followButton, BorderLayout.EAST);

        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(BG_DEEP);

        JScrollPane messagesScroll = new JScrollPane(messagesPanel);
        messagesScroll.setBorder(BorderFactory.createLineBorder(BORDER_COL, 1));
        messagesScroll.getViewport().setBackground(BG_DEEP);
        messagesScroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(conversationHeader, BorderLayout.NORTH);
        panel.add(messagesScroll, BorderLayout.CENTER);
        panel.add(buildComposer(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildComposer() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);

        composer.setBackground(BG_PANEL);
        composer.setForeground(TEXT_MAIN);
        composer.setCaretColor(NEON_CYAN);
        composer.setLineWrap(true);
        composer.setWrapStyleWord(true);
        composer.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        composer.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JButton send = DashboardScreen.makeSmallButton("Send", BG_CARD, NEON_CYAN, BORDER_COL);
        send.addActionListener(e -> sendMessage());

        composer.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "sendMessage");
        composer.getActionMap().put("sendMessage", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        panel.add(new JScrollPane(composer), BorderLayout.CENTER);
        panel.add(send, BorderLayout.EAST);
        return panel;
    }

    private void loadUsers() {
        if (!Session.isLoggedIn()) {
            return;
        }

        int selectedId = selectedUser == null ? -1 : selectedUser.getUserId();
        List<MessageDAO.ChatUser> users = filterUsers(
                messageDAO.searchChatUsers(Session.getCurrentUserId(), ""),
                currentSearchQuery
        );

        userModel.clear();
        for (MessageDAO.ChatUser user : users) {
            userModel.addElement(user);
            if (user.getUserId() == selectedId) {
                selectedUser = user;
            }
        }

        listStatus.setText(statusText(users.size(), currentSearchQuery));

        if (selectedId != -1 && containsUser(users, selectedId)) {
            selectUserById(selectedId);
        } else if (selectedId != -1 && !currentSearchQuery.isBlank()) {
            userList.clearSelection();
        }
    }

    private String getSearchText() {
        return isPlaceholderText(searchField) ? "" : searchField.getText().trim();
    }

    private List<MessageDAO.ChatUser> filterUsers(List<MessageDAO.ChatUser> users, String query) {
        if (query == null || query.isBlank()) {
            return users;
        }

        String normalizedQuery = normalize(query);
        return users.stream()
                .filter(user -> normalize(user.getUsername()).contains(normalizedQuery)
                        || normalize(user.getDisplayName()).contains(normalizedQuery)
                        || normalize(user.getLastMessage()).contains(normalizedQuery))
                .toList();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private boolean containsUser(List<MessageDAO.ChatUser> users, int userId) {
        for (MessageDAO.ChatUser user : users) {
            if (user.getUserId() == userId) {
                return true;
            }
        }
        return false;
    }

    private String statusText(int resultCount, String query) {
        if (query == null || query.isBlank()) {
            return resultCount + " users";
        }
        return resultCount == 0 ? "No users match \"" + query + "\"" : resultCount + " matching users";
    }

    private void selectUserById(int userId) {
        for (int i = 0; i < userModel.size(); i++) {
            if (userModel.get(i).getUserId() == userId) {
                userList.setSelectedIndex(i);
                return;
            }
        }
    }

    private void loadConversation() {
        messagesPanel.removeAll();

        if (selectedUser == null) {
            conversationTitle.setText("Select a conversation");
            followButton.setVisible(false);
            messagesPanel.add(emptyLabel("Search a user or choose a conversation."));
            messagesPanel.revalidate();
            messagesPanel.repaint();
            return;
        }

        conversationTitle.setText("u/" + selectedUser.getUsername());
        updateFollowButton();
        messageDAO.markConversationRead(Session.getCurrentUserId(), selectedUser.getUserId());
        List<MessageDAO.ChatMessage> messages = messageDAO.getConversation(Session.getCurrentUserId(), selectedUser.getUserId());

        if (messages.isEmpty()) {
            messagesPanel.add(emptyLabel("No messages yet."));
        } else {
            for (MessageDAO.ChatMessage message : messages) {
                messagesPanel.add(messageBubble(message));
                messagesPanel.add(Box.createVerticalStrut(8));
            }
        }

        messagesPanel.revalidate();
        messagesPanel.repaint();
        SwingUtilities.invokeLater(() -> {
            Container parent = messagesPanel.getParent();
            if (parent instanceof JViewport viewport) {
                JScrollBar bar = ((JScrollPane) viewport.getParent()).getVerticalScrollBar();
                bar.setValue(bar.getMaximum());
            }
        });
    }

    private void updateFollowButton() {
        if (selectedUser == null) {
            followButton.setVisible(false);
            return;
        }

        UserFollowDAO followDAO = new UserFollowDAO();
        boolean following = followDAO.isFollowing(Session.getCurrentUserId(), selectedUser.getUserId());
        followButton.setText(following ? "Unfollow" : "Follow");
        followButton.setForeground(following ? TEXT_MUTED : NEON_PINK);
        followButton.setVisible(true);
    }

    private void toggleSelectedFollow() {
        if (selectedUser == null) {
            return;
        }

        UserFollowDAO followDAO = new UserFollowDAO();
        boolean ok = followDAO.toggleFollow(Session.getCurrentUserId(), selectedUser.getUserId());
        if (!ok) {
            SoundFX.error();
            JOptionPane.showMessageDialog(this, "Follow action failed.", "Clixky", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SoundFX.success();
        updateFollowButton();
    }

    private void refreshCurrentConversation() {
        if (!isVisible() || !Session.isLoggedIn()) {
            return;
        }

        loadUsers();
        if (selectedUser != null) {
            loadConversation();
        }
    }

    private void sendMessage() {
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this, "Select a user first.", "Clixky", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String body = composer.getText().trim();
        if (body.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Message cannot be empty.", "Clixky", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (body.length() > 1000) {
            JOptionPane.showMessageDialog(this, "Message must be 1000 characters or fewer.", "Clixky", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean ok = messageDAO.sendMessage(Session.getCurrentUserId(), selectedUser.getUserId(), body);
        if (!ok) {
            SoundFX.error();
            JOptionPane.showMessageDialog(this, "Message could not be sent.", "Clixky", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SoundFX.success();
        composer.setText("");
        loadUsers();
        selectUserById(selectedUser.getUserId());
        loadConversation();
    }

    private JLabel emptyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(TEXT_MUTED);
        label.setBorder(new EmptyBorder(16, 16, 16, 16));
        return label;
    }

    private JPanel messageBubble(MessageDAO.ChatMessage message) {
        boolean mine = message.getSenderId() == Session.getCurrentUserId();
        JPanel row = new JPanel(new FlowLayout(mine ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel bubble = new JLabel("<html><body style='width:320px; font-family:Segoe UI; font-size:12px; color:"
                + htmlColor(mine ? BG_DEEP : TEXT_MAIN) + "'>"
                + "<b>" + escapeHtml(mine ? "You" : message.getSenderUsername()) + "</b><br>"
                + escapeHtml(message.getBody()) + "<br>"
                + "<span style='font-size:9px; color:" + htmlColor(mine ? BG_DEEP : NEON_DIM) + "'>"
                + escapeHtml(message.getSentAt()) + "</span>"
                + "</body></html>");
        bubble.setOpaque(true);
        bubble.setBackground(mine ? NEON_CYAN : BG_PANEL);
        bubble.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(mine ? NEON_CYAN : BORDER_COL, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));

        row.add(bubble);
        return row;
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

    private class ChatUserRenderer extends JPanel implements ListCellRenderer<MessageDAO.ChatUser> {
        private final JLabel name = new JLabel();
        private final JLabel meta = new JLabel();

        ChatUserRenderer() {
            setLayout(new BorderLayout(0, 4));
            setBorder(new EmptyBorder(8, 8, 8, 8));
            name.setFont(new Font("Segoe UI", Font.BOLD, 13));
            meta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            add(name, BorderLayout.NORTH);
            add(meta, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends MessageDAO.ChatUser> list,
                                                      MessageDAO.ChatUser value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            setBackground(isSelected ? BG_CARD : BG_PANEL);
            String display = value.getDisplayName() == null || value.getDisplayName().isBlank()
                    ? value.getUsername()
                    : value.getDisplayName();
            name.setText((value.getUnreadCount() > 0 ? "(" + value.getUnreadCount() + ") " : "") + display);
            name.setForeground(value.getUnreadCount() > 0 ? NEON_PINK : TEXT_MAIN);

            String last = value.getLastMessage() == null || value.getLastMessage().isBlank()
                    ? "u/" + value.getUsername()
                    : value.getLastMessage();
            if (last.length() > 34) {
                last = last.substring(0, 31) + "...";
            }
            meta.setText(last);
            meta.setForeground(TEXT_MUTED);
            return this;
        }
    }
}
