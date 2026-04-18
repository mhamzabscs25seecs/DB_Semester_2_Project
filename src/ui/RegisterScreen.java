package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static ui.LoginScreen.*;

public class RegisterScreen extends JFrame {

    private JTextField  firstNameField, lastNameField, usernameField, emailField;
    private JPasswordField passwordField, confirmField;
    private Runnable onRegisterSuccess;
    private Runnable onGoLogin;

    public RegisterScreen(Runnable onRegisterSuccess, Runnable onGoLogin) {
        this.onRegisterSuccess = onRegisterSuccess;
        this.onGoLogin = onGoLogin;
        buildUI();
    }

    private void buildUI() {
        setTitle("Clixky — Create Account");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(520, 660);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BG_DEEP);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(false);
        root.add(buildCard());
        setContentPane(root);
        setVisible(true);
    }

    private JPanel buildCard() {
        JPanel card = new RoundPanel(16, BG_PANEL, BORDER_COL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(420, 580));
        card.setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 36));

        // Header
        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(GREEN_HI);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Join Clixky and start exploring communities");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 60));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Name row (side by side)
        JPanel nameRow = new JPanel(new GridLayout(1, 2, 12, 0));
        nameRow.setOpaque(false);
        nameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel firstCol = new JPanel();
        firstCol.setLayout(new BoxLayout(firstCol, BoxLayout.Y_AXIS));
        firstCol.setOpaque(false);
        firstCol.add(makeLabel("FIRST NAME"));
        firstCol.add(Box.createVerticalStrut(6));
        firstNameField = makeTextField("First name");
        firstCol.add(firstNameField);

        JPanel lastCol = new JPanel();
        lastCol.setLayout(new BoxLayout(lastCol, BoxLayout.Y_AXIS));
        lastCol.setOpaque(false);
        lastCol.add(makeLabel("LAST NAME"));
        lastCol.add(Box.createVerticalStrut(6));
        lastNameField = makeTextField("Last name");
        lastCol.add(lastNameField);

        nameRow.add(firstCol);
        nameRow.add(lastCol);

        // Username
        usernameField = makeTextField("Choose a username");
        emailField    = makeTextField("your@email.com");

        // Password fields side by side
        JPanel passRow = new JPanel(new GridLayout(1, 2, 12, 0));
        passRow.setOpaque(false);
        passRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        passRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel p1Col = new JPanel();
        p1Col.setLayout(new BoxLayout(p1Col, BoxLayout.Y_AXIS));
        p1Col.setOpaque(false);
        p1Col.add(makeLabel("PASSWORD"));
        p1Col.add(Box.createVerticalStrut(6));
        passwordField = new JPasswordField();
        styleTextField(passwordField, "Min 8 characters");
        p1Col.add(passwordField);

        JPanel p2Col = new JPanel();
        p2Col.setLayout(new BoxLayout(p2Col, BoxLayout.Y_AXIS));
        p2Col.setOpaque(false);
        p2Col.add(makeLabel("CONFIRM PASSWORD"));
        p2Col.add(Box.createVerticalStrut(6));
        confirmField = new JPasswordField();
        styleTextField(confirmField, "Repeat password");
        p2Col.add(confirmField);

        passRow.add(p1Col);
        passRow.add(p2Col);

        // Terms notice
        JLabel terms = new JLabel("By registering you agree to the Clixky Terms of Use.");
        terms.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        terms.setForeground(GREEN_DIM);
        terms.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Register button
        JButton registerBtn = makeButton("CREATE  ACCOUNT", BG_CARD, GREEN_HI, new Color(45, 100, 45));
        registerBtn.addActionListener(e -> handleRegister());

        // Login link
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);
        JLabel linkText = new JLabel("Already have an account?");
        linkText.setFont(FONT_SMALL);
        linkText.setForeground(TEXT_MUTED);
        JLabel linkBtn = new JLabel("Sign in →");
        linkBtn.setFont(FONT_SMALL);
        linkBtn.setForeground(GREEN_HI);
        linkBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { if (onGoLogin != null) onGoLogin.run(); }
            public void mouseEntered(MouseEvent e) { linkBtn.setForeground(GOLD); }
            public void mouseExited(MouseEvent e)  { linkBtn.setForeground(GREEN_HI); }
        });
        linkRow.add(linkText);
        linkRow.add(linkBtn);

        // Assemble
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(18));
        card.add(sep);
        card.add(Box.createVerticalStrut(22));
        card.add(nameRow);
        card.add(Box.createVerticalStrut(14));
        card.add(makeLabel("USERNAME"));
        card.add(Box.createVerticalStrut(6));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(14));
        card.add(makeLabel("EMAIL ADDRESS"));
        card.add(Box.createVerticalStrut(6));
        card.add(emailField);
        card.add(Box.createVerticalStrut(14));
        card.add(passRow);
        card.add(Box.createVerticalStrut(16));
        card.add(terms);
        card.add(Box.createVerticalStrut(22));
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(14));
        card.add(linkRow);

        return card;
    }

    private void handleRegister() {
        String first    = getText(firstNameField);
        String last     = getText(lastNameField);
        String username = getText(usernameField);
        String email    = getText(emailField);
        String pass     = new String(passwordField.getPassword());
        String confirm  = new String(confirmField.getPassword());

        if (first.isEmpty() || last.isEmpty() || username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Clixky", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Clixky", JOptionPane.ERROR_MESSAGE);
            shake(confirmField);
            return;
        }
        if (pass.length() < 8) {
            JOptionPane.showMessageDialog(this, "Password must be at least 8 characters.", "Clixky", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!email.contains("@")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email.", "Clixky", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // TODO: Call UserDAO.register(first + " " + last, username, email, pass)
        // SHA-256 hash the password before saving!
        if (onRegisterSuccess != null) onRegisterSuccess.run();
    }

    // Returns field text, ignoring placeholder text
    private String getText(JTextField f) {
        String t = f.getText().trim();
        Color fg = f.getForeground();
        if (fg.equals(GREEN_DIM)) return ""; // placeholder color = empty
        return t;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new RegisterScreen(
                () -> System.out.println("Registered!"),
                () -> System.out.println("Go to login")
            )
        );
    }
}
