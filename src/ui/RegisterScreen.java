package stopwatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static stopwatch.LoginScreen.*;

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
        setSize(520, 680);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(420, 600));

        JPanel root = new JPanel(new GridBagLayout()) {
        	@Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_DEEP);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Dual neon glows
                int cx = getWidth() * 3 / 4, cy = getHeight() / 4;
                
                // Corrected: Removed "java.awt.geom."
                RadialGradientPaint glow1 = new RadialGradientPaint(cx, cy, 260,
                    new float[]{0f, 1f}, new Color[]{new Color(255, 60, 180, 30), new Color(0,0,0,0)});
                g2.setPaint(glow1);
                g2.fillOval(cx-260, cy-260, 520, 520);
                
                // Corrected: Removed "java.awt.geom."
                RadialGradientPaint glow2 = new RadialGradientPaint(getWidth()/4, getHeight()*3/4, 220,
                    new float[]{0f, 1f}, new Color[]{new Color(0, 200, 255, 22), new Color(0,0,0,0)});
                g2.setPaint(glow2);
                g2.fillOval(getWidth()/4-220, getHeight()*3/4-220, 440, 440);
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
        card.setPreferredSize(new Dimension(430, 600));
        card.setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 36));

        // Header
        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(NEON_PINK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Join Clixky and start exploring communities");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 60));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Name row
        JPanel nameRow = new JPanel(new GridLayout(1, 2, 12, 0));
        nameRow.setOpaque(false);
        nameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel firstCol = new JPanel();
        firstCol.setLayout(new BoxLayout(firstCol, BoxLayout.Y_AXIS));
        firstCol.setOpaque(false);
        JLabel fNameLabel = makeLabel("FIRST NAME");
        fNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        firstCol.add(fNameLabel);
        firstCol.add(Box.createVerticalStrut(6));
        firstNameField = makeTextField("First name");
        firstNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        firstCol.add(firstNameField);

        JPanel lastCol = new JPanel();
        lastCol.setLayout(new BoxLayout(lastCol, BoxLayout.Y_AXIS));
        lastCol.setOpaque(false);
        JLabel lNameLabel = makeLabel("LAST NAME");
        lNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        lastCol.add(lNameLabel);
        lastCol.add(Box.createVerticalStrut(6));
        lastNameField = makeTextField("Last name");
        lastNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        lastCol.add(lastNameField);

        nameRow.add(firstCol);
        nameRow.add(lastCol);

        usernameField = makeTextField("Choose a username");
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        emailField = makeTextField("your@email.com");
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password row
        JPanel passRow = new JPanel(new GridLayout(1, 2, 12, 0));
        passRow.setOpaque(false);
        passRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        passRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel p1Col = new JPanel();
        p1Col.setLayout(new BoxLayout(p1Col, BoxLayout.Y_AXIS));
        p1Col.setOpaque(false);
        JLabel passLabel = makeLabel("PASSWORD");
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        p1Col.add(passLabel);
        p1Col.add(Box.createVerticalStrut(6));
        passwordField = new JPasswordField();
        styleTextField(passwordField, "Min 8 characters");
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        p1Col.add(passwordField);

        JPanel p2Col = new JPanel();
        p2Col.setLayout(new BoxLayout(p2Col, BoxLayout.Y_AXIS));
        p2Col.setOpaque(false);
        JLabel confPassLabel = makeLabel("CONFIRM PASSWORD");
        confPassLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        p2Col.add(confPassLabel);
        p2Col.add(Box.createVerticalStrut(6));
        confirmField = new JPasswordField();
        styleTextField(confirmField, "Repeat password");
        confirmField.setAlignmentX(Component.LEFT_ALIGNMENT);
        p2Col.add(confirmField);

        passRow.add(p1Col);
        passRow.add(p2Col);

        JLabel terms = new JLabel("By registering you agree to the Clixky Terms of Use.");
        terms.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        terms.setForeground(NEON_DIM);
        terms.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton registerBtn = makeButton("CREATE  ACCOUNT", BG_CARD, NEON_CYAN, BORDER_COL);
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.addActionListener(e -> handleRegister());

        // Login link
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);
        linkRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel linkText = new JLabel("Already have an account?");
        linkText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        linkText.setForeground(TEXT_MUTED);
        JLabel linkBtn = new JLabel("Sign in →");
        linkBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        linkBtn.setForeground(NEON_CYAN);
        linkBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { if (onGoLogin != null) onGoLogin.run(); }
            public void mouseEntered(MouseEvent e) { linkBtn.setForeground(NEON_PINK); }
            public void mouseExited(MouseEvent e)  { linkBtn.setForeground(NEON_CYAN); }
        });
        linkRow.add(linkText);
        linkRow.add(linkBtn);

        // Assembly
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(18));
        card.add(sep);
        card.add(Box.createVerticalStrut(22));
        card.add(nameRow);
        card.add(Box.createVerticalStrut(14));
        
        JLabel userLabel = makeLabel("USERNAME");
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(userLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(14));
        
        JLabel emailLabel = makeLabel("EMAIL ADDRESS");
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(emailLabel);
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
        // TODO: UserDAO.register(first + " " + last, username, email, pass)
        if (onRegisterSuccess != null) onRegisterSuccess.run();
    }

    private String getText(JTextField f) {
        String t = f.getText().trim();
        Color fg = f.getForeground();
        if (fg.equals(NEON_DIM)) return "";
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