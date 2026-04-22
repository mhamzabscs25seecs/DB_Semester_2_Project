package ui;

import dao.Session;
import dao.UserDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static ui.LoginScreen.*;

public class ProfileSetupScreen extends JFrame {
    private final Runnable onComplete;
    private JTextField displayNameField;
    private JTextField countryField;
    private JTextField phoneField;
    private JTextField birthYearField;
    private JTextArea bioArea;
    private JCheckBox privateBox;

    public ProfileSetupScreen(Runnable onComplete) {
        this.onComplete = onComplete;
        buildUI();
    }

    private void buildUI() {
        setTitle("Clixky — Profile Settings");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(560, 640);
        setMinimumSize(new Dimension(460, 560));
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG_DEEP);
        clearFieldFocusOnBlankClick(root);

        JPanel card = new RoundPanel(16, BG_PANEL, BORDER_COL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(440, 540));
        card.setBorder(new EmptyBorder(28, 34, 28, 34));
        clearFieldFocusOnBlankClick(card);

        JLabel title = new JLabel("Profile Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(NEON_PINK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Set how your profile appears on Clixky");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        displayNameField = makeTextField("Display name");
        countryField = makeTextField("Country");
        phoneField = makeTextField("Phone number");
        birthYearField = makeTextField("Birth year");

        bioArea = new JTextArea(4, 24);
        bioArea.setBackground(BG_DEEP);
        bioArea.setForeground(TEXT_MAIN);
        bioArea.setCaretColor(NEON_CYAN);
        bioArea.setFont(FONT_INPUT);
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        bioArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL),
                new EmptyBorder(8, 10, 8, 10)
        ));
        JScrollPane bioScroll = new JScrollPane(bioArea);
        bioScroll.setBorder(null);
        bioScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        bioScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        privateBox = new JCheckBox("Private profile");
        privateBox.setOpaque(false);
        privateBox.setFont(FONT_SMALL);
        privateBox.setForeground(TEXT_MUTED);
        privateBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton saveBtn = makeButton("SAVE PROFILE", BG_CARD, NEON_CYAN, BORDER_COL);
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> saveProfile());

        JButton skipBtn = makeButton("SKIP FOR NOW", BG_CARD, TEXT_MUTED, BORDER_COL);
        skipBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        skipBtn.addActionListener(e -> finish());

        fillExistingProfile();

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(20));
        addField(card, "DISPLAY NAME", displayNameField);
        addField(card, "BIO", bioScroll);
        addField(card, "COUNTRY", countryField);
        addField(card, "PHONE", phoneField);
        addField(card, "BIRTH YEAR", birthYearField);
        card.add(privateBox);
        card.add(Box.createVerticalStrut(18));
        card.add(saveBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(skipBtn);

        root.add(card);
        setContentPane(root);
        setVisible(true);
    }

    private void addField(JPanel card, String label, JComponent field) {
        JLabel l = makeLabel(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(l);
        card.add(Box.createVerticalStrut(6));
        card.add(field);
        card.add(Box.createVerticalStrut(12));
    }

    private void fillExistingProfile() {
        UserDAO.UserProfile profile = new UserDAO().getProfileById(Session.getCurrentUserId());
        if (profile == null) {
            return;
        }

        setField(displayNameField, profile.getDisplayName());
        setField(countryField, profile.getCountry());
        setField(phoneField, profile.getPhoneNo());
        setField(birthYearField, String.valueOf(profile.getBirthYear()));
        bioArea.setText(profile.getBioText() == null ? "" : profile.getBioText());
        privateBox.setSelected(profile.isPrivate());
    }

    private void setField(JTextField field, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        field.setText(value);
        field.setForeground(TEXT_MAIN);
    }

    private void saveProfile() {
        String displayName = getFieldText(displayNameField);
        String country = getFieldText(countryField);
        String phone = getFieldText(phoneField);
        String birthYearText = getFieldText(birthYearField);
        String bio = bioArea.getText().trim();

        int birthYear;
        try {
            birthYear = Integer.parseInt(birthYearText);
        } catch (NumberFormatException e) {
            showCyberError(this, "Invalid Birth Year", "Please enter a valid birth year.");
            return;
        }

        if (displayName.isBlank()) {
            showCyberError(this, "Missing Name", "Please enter a display name.");
            return;
        }

        if (birthYear < 1900) {
            showCyberError(this, "Invalid Birth Year", "Birth year must be 1900 or later.");
            return;
        }

        boolean saved = new UserDAO().updateProfile(
                Session.getCurrentUserId(),
                displayName,
                bio,
                country,
                phone,
                birthYear,
                privateBox.isSelected()
        );

        if (!saved) {
            SoundFX.error();
            showCyberError(this, "Profile Error", "Profile could not be saved.");
            return;
        }

        SoundFX.success();
        finish();
    }

    private String getFieldText(JTextField field) {
        return field.getForeground().equals(NEON_DIM) ? "" : field.getText().trim();
    }

    private void finish() {
        dispose();
        if (onComplete != null) {
            onComplete.run();
        }
    }
}
