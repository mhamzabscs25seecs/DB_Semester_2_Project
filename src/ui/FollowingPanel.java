package ui;

import dao.UserFollowDAO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.function.BiFunction;

import static ui.LoginScreen.*;

public class FollowingPanel extends JPanel {
    private final int profileUserId;
    private final boolean privateProfile;
    private final UserFollowDAO followDAO = new UserFollowDAO();

    public FollowingPanel(int profileUserId, boolean privateProfile) {
        this.profileUserId = profileUserId;
        this.privateProfile = privateProfile;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        if (!canViewFollowLists()) {
            add(privateNotice());
            return;
        }

        add(statRow(
                "FOLLOWING",
                followDAO.getFollowingCount(profileUserId),
                "Following",
                followDAO::getFollowingUsers
        ));
        add(Box.createVerticalStrut(8));
        add(statRow(
                "FOLLOWERS",
                followDAO.getFollowerCount(profileUserId),
                "Followers",
                followDAO::getFollowerUsers
        ));
    }

    private boolean canViewFollowLists() {
        if (!privateProfile || profileUserId == dao.Session.getCurrentUserId()) {
            return true;
        }

        int currentUserId = dao.Session.getCurrentUserId();
        return followDAO.isFollowing(currentUserId, profileUserId)
                && followDAO.isFollowing(profileUserId, currentUserId);
    }

    private JPanel privateNotice() {
        JPanel notice = new JPanel(new BorderLayout(8, 0));
        notice.setOpaque(false);
        notice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel label = new JLabel("FOLLOWERS / FOLLOWING:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(NEON_MID);

        JLabel value = new JLabel("Hidden on private profile");
        value.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        value.setForeground(TEXT_MUTED);

        notice.add(label, BorderLayout.WEST);
        notice.add(value, BorderLayout.CENTER);
        return notice;
    }

    private JPanel statRow(String label, int count, String dialogTitle,
                           BiFunction<Integer, String, List<UserFollowDAO.FollowedUser>> loader) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel key = new JLabel(label + ":");
        key.setFont(new Font("Segoe UI", Font.BOLD, 10));
        key.setForeground(NEON_MID);

        JLabel value = new JLabel(count + " users");
        value.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        value.setForeground(TEXT_MAIN);

        JButton view = DashboardScreen.makeSmallButton("View", BG_CARD, NEON_CYAN, BORDER_COL);
        view.setPreferredSize(new Dimension(78, 30));
        view.addActionListener(e -> showUsersDialog(dialogTitle, loader));

        JPanel text = new JPanel(new BorderLayout(8, 0));
        text.setOpaque(false);
        text.add(key, BorderLayout.WEST);
        text.add(value, BorderLayout.CENTER);

        row.add(text, BorderLayout.CENTER);
        row.add(view, BorderLayout.EAST);
        return row;
    }

    private void showUsersDialog(String title,
                                 BiFunction<Integer, String, List<UserFollowDAO.FollowedUser>> loader) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(430, 430);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(BG_PANEL);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        heading.setForeground(NEON_PINK);

        JTextField search = makeTextField("Search users...");
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(BG_DEEP);
        list.setBorder(new EmptyBorder(8, 8, 8, 8));

        Runnable refresh = () -> refreshList(list, loader, searchText(search));
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { SwingUtilities.invokeLater(refresh); }
            public void removeUpdate(DocumentEvent e) { SwingUtilities.invokeLater(refresh); }
            public void changedUpdate(DocumentEvent e) { SwingUtilities.invokeLater(refresh); }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COL));
        scroll.getViewport().setBackground(BG_DEEP);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        JButton close = DashboardScreen.makeSmallButton("Close", BG_CARD, TEXT_MUTED, BORDER_COL);
        close.addActionListener(e -> dialog.dispose());

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        search.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(heading);
        top.add(Box.createVerticalStrut(12));
        top.add(search);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(close);

        root.add(top, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        refresh.run();
        dialog.setVisible(true);
    }

    private String searchText(JTextField search) {
        return isPlaceholderText(search) ? "" : search.getText().trim();
    }

    private void refreshList(JPanel list,
                             BiFunction<Integer, String, List<UserFollowDAO.FollowedUser>> loader,
                             String query) {
        list.removeAll();

        List<UserFollowDAO.FollowedUser> users = loader.apply(profileUserId, query);
        if (users.isEmpty()) {
            JLabel empty = new JLabel(query == null || query.isBlank()
                    ? "No users to show."
                    : "No matching users.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            empty.setForeground(TEXT_MUTED);
            empty.setBorder(new EmptyBorder(10, 8, 10, 8));
            list.add(empty);
        } else {
            for (UserFollowDAO.FollowedUser user : users) {
                list.add(userRow(user));
                list.add(Box.createVerticalStrut(8));
            }
        }

        list.revalidate();
        list.repaint();
    }

    private JPanel userRow(UserFollowDAO.FollowedUser user) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(BG_PANEL);
        row.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JLabel avatar = new JLabel(user.getUsername().substring(0, 1).toUpperCase());
        avatar.setPreferredSize(new Dimension(30, 30));
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
        text.add(Box.createVerticalStrut(3));
        text.add(meta);

        row.add(avatar, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private String shortText(String value) {
        if (value == null || value.isBlank()) {
            return "No bio yet.";
        }

        return value.length() > 58 ? value.substring(0, 55) + "..." : value;
    }
}
