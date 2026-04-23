package ui;

import dao.Session;
import dao.UserDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static ui.LoginScreen.*;

public class WelcomeScreen extends JFrame {
    private final Runnable onSetupProfile;
    private final Runnable onContinue;
    private final boolean showProfileSetupOption;

    public WelcomeScreen(Runnable onSetupProfile, Runnable onContinue) {
        this(onSetupProfile, onContinue, true);
    }

    public WelcomeScreen(Runnable onSetupProfile, Runnable onContinue, boolean showProfileSetupOption) {
        this.onSetupProfile = onSetupProfile;
        this.onContinue = onContinue;
        this.showProfileSetupOption = showProfileSetupOption;
        buildUI();
    }

    private void buildUI() {
        setTitle("Clixky - Welcome");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(560, 440);
        setMinimumSize(new Dimension(460, 380));
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG_DEEP);

        JPanel card = new RoundPanel(16, BG_PANEL, BORDER_COL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(430, 320));
        card.setBorder(new EmptyBorder(32, 36, 30, 36));

        String displayName = loadDisplayName();

        JLabel logo = new JLabel("CLIXKY");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(NEON_PINK);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Welcome, " + displayName);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(NEON_CYAN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel(showProfileSetupOption
                ? "Your account is ready. Choose how you want to start."
                : "Good to see you again. Continue to your Clixky feed.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton setup = makeButton("SET UP PROFILE", BG_CARD, NEON_CYAN, BORDER_COL);
        setup.setAlignmentX(Component.CENTER_ALIGNMENT);
        setup.addActionListener(e -> {
            SoundFX.click();
            dispose();
            if (onSetupProfile != null) {
                onSetupProfile.run();
            }
        });

        JButton skip = makeButton("CONTINUE TO CLIXKY", BG_CARD, NEON_PINK, BORDER_COL);
        skip.setAlignmentX(Component.CENTER_ALIGNMENT);
        skip.addActionListener(e -> {
            SoundFX.click();
            dispose();
            if (onContinue != null) {
                onContinue.run();
            }
        });

        card.add(logo);
        card.add(Box.createVerticalStrut(18));
        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(sub);
        card.add(Box.createVerticalStrut(30));
        if (showProfileSetupOption) {
            card.add(setup);
            card.add(Box.createVerticalStrut(12));
        }
        card.add(skip);

        root.add(card);
        setContentPane(root);
        setVisible(true);
    }

    private String loadDisplayName() {
        UserDAO.UserProfile profile = new UserDAO().getProfileById(Session.getCurrentUserId());
        if (profile != null && profile.getDisplayName() != null && !profile.getDisplayName().isBlank()) {
            return profile.getDisplayName();
        }

        String username = Session.getCurrentUsername();
        return username == null || username.isBlank() ? "new member" : username;
    }
}
