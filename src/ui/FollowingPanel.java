package ui;

import dao.UserFollowDAO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

import static ui.LoginScreen.*;

public class FollowingPanel extends JPanel {
    private final int profileUserId;
    private final UserFollowDAO followDAO = new UserFollowDAO();
    private final JTextField searchField = makeTextField("Search following...");
    private final JPanel listPanel = new JPanel();
    private final JLabel countLabel = new JLabel();

    public FollowingPanel(int profileUserId) {
        this.profileUserId = profileUserId;
        buildUI();
        refreshList("");
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("FOLLOWING");
        title.setFont(new Font("Segoe UI", Font.BOLD, 10));
        title.setForeground(NEON_MID);

        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        countLabel.setForeground(TEXT_MUTED);
        countLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(title, BorderLayout.WEST);
        header.add(countLabel, BorderLayout.EAST);

        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }

            private void refresh() {
                SwingUtilities.invokeLater(() -> refreshList(searchText()));
            }
        });

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(BG_DEEP);
        listPanel.setBorder(new EmptyBorder(6, 6, 6, 6));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COL, 1));
        scroll.getViewport().setBackground(BG_DEEP);
        scroll.setPreferredSize(new Dimension(0, 112));
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        add(header, BorderLayout.NORTH);
        add(searchField, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);
    }

    private String searchText() {
        return isPlaceholderText(searchField) ? "" : searchField.getText().trim();
    }

    private void refreshList(String query) {
        listPanel.removeAll();

        List<UserFollowDAO.FollowedUser> following = followDAO.getFollowingUsers(profileUserId, query);
        int total = followDAO.getFollowingCount(profileUserId);
        countLabel.setText(total + " total");

        if (following.isEmpty()) {
            JLabel empty = new JLabel(query == null || query.isBlank()
                    ? "Not following anyone yet."
                    : "No matching users.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            empty.setForeground(TEXT_MUTED);
            empty.setBorder(new EmptyBorder(8, 6, 8, 6));
            listPanel.add(empty);
        } else {
            for (UserFollowDAO.FollowedUser user : following) {
                listPanel.add(userRow(user));
                listPanel.add(Box.createVerticalStrut(6));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel userRow(UserFollowDAO.FollowedUser user) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(BG_PANEL);
        row.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL, 1),
                new EmptyBorder(7, 9, 7, 9)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JLabel avatar = new JLabel(user.getUsername().substring(0, 1).toUpperCase());
        avatar.setPreferredSize(new Dimension(28, 28));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        avatar.setForeground(NEON_CYAN);
        avatar.setBorder(BorderFactory.createLineBorder(NEON_PINK, 1));

        String display = user.getDisplayName() == null || user.getDisplayName().isBlank()
                ? user.getUsername()
                : user.getDisplayName();
        JLabel name = new JLabel(display + "  @" + user.getUsername());
        name.setFont(new Font("Segoe UI", Font.BOLD, 12));
        name.setForeground(TEXT_MAIN);

        JLabel meta = new JLabel(shortText(user.getBioText()));
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        meta.setForeground(TEXT_MUTED);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(name);
        text.add(Box.createVerticalStrut(2));
        text.add(meta);

        row.add(avatar, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private String shortText(String value) {
        if (value == null || value.isBlank()) {
            return "No bio yet.";
        }

        return value.length() > 54 ? value.substring(0, 51) + "..." : value;
    }
}
