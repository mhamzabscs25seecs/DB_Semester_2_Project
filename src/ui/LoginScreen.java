package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class LoginScreen extends JFrame {

    // ── Palette ──────────────────────────────────────────────
    static final Color BG_DEEP    = new Color(4,  15,  4);
    static final Color BG_PANEL   = new Color(10, 26, 10);
    static final Color BG_CARD    = new Color(15, 42, 15);
    static final Color BORDER_COL = new Color(25, 58, 25);
    static final Color GREEN_HI   = new Color(94, 176, 94);
    static final Color GREEN_MID  = new Color(60, 120, 60);
    static final Color GREEN_DIM  = new Color(35,  80, 35);
    static final Color GOLD       = new Color(200, 168, 75);
    static final Color TEXT_MAIN  = new Color(160, 220, 160);
    static final Color TEXT_MUTED = new Color(74, 138, 74);
    static final Font  FONT_TITLE = new Font("Segoe UI", Font.BOLD,  28);
    static final Font  FONT_LABEL = new Font("Segoe UI", Font.BOLD,  11);
    static final Font  FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font  FONT_BTN   = new Font("Segoe UI", Font.BOLD,  13);
    static final Font  FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    private JTextField  usernameField;
    private JPasswordField passwordField;
    private Runnable onLoginSuccess;
    private Runnable onGoRegister;

    public LoginScreen(Runnable onLoginSuccess, Runnable onGoRegister) {
        this.onLoginSuccess = onLoginSuccess;
        this.onGoRegister   = onGoRegister;
        buildUI();
    }

    private void buildUI() {
        setTitle("Clixky — Sign In");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // Root panel with custom paint (radial glow)
        JPanel root = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Deep background
                g2.setColor(BG_DEEP);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Radial glow in center
                int cx = getWidth() / 2, cy = getHeight() / 2;
                RadialGradientPaint glow = new RadialGradientPaint(
                    cx, cy, 260,
                    new float[]{0f, 1f},
                    new Color[]{new Color(20, 70, 20, 60), new Color(0, 0, 0, 0)}
                );
                g2.setPaint(glow);
                g2.fillOval(cx - 260, cy - 260, 520, 520);
            }
        };
        root.setOpaque(false);

        JPanel card = buildCard();
        root.add(card);
        setContentPane(root);
        setVisible(true);
    }

    private JPanel buildCard() {
        JPanel card = new RoundPanel(16, BG_PANEL, BORDER_COL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(360, 460));
        card.setBorder(BorderFactory.createEmptyBorder(36, 36, 36, 36));

        // Logo
        JLabel logo = new JLabel("CLIXKY");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        logo.setForeground(GREEN_HI);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tagline
        JLabel tag = new JLabel("A RELATIONAL COMMUNITY PLATFORM");
        tag.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        tag.setForeground(GREEN_DIM);
        tag.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Gold divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 80));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Username
        JLabel uLabel = makeLabel("USERNAME");
        usernameField = makeTextField("Enter your username");

        // Password
        JLabel pLabel = makeLabel("PASSWORD");
        passwordField = new JPasswordField();
        styleTextField(passwordField, "Enter your password");

        // Sign In button
        JButton signInBtn = makeButton("SIGN  IN", BG_CARD, GREEN_HI, BORDER_COL);
        signInBtn.addActionListener(e -> handleLogin());

        // Register link
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);
        JLabel linkText = new JLabel("Don't have an account?");
        linkText.setFont(FONT_SMALL);
        linkText.setForeground(TEXT_MUTED);
        JLabel linkBtn = new JLabel("Register →");
        linkBtn.setFont(FONT_SMALL);
        linkBtn.setForeground(GREEN_HI);
        linkBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { if (onGoRegister != null) onGoRegister.run(); }
            public void mouseEntered(MouseEvent e) { linkBtn.setForeground(GOLD); }
            public void mouseExited(MouseEvent e)  { linkBtn.setForeground(GREEN_HI); }
        });
        linkRow.add(linkText);
        linkRow.add(linkBtn);

        // Layout
        card.add(logo);
        card.add(Box.createVerticalStrut(4));
        card.add(tag);
        card.add(Box.createVerticalStrut(20));
        card.add(sep);
        card.add(Box.createVerticalStrut(24));
        card.add(uLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(16));
        card.add(pLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(28));
        card.add(signInBtn);
        card.add(Box.createVerticalStrut(16));
        card.add(linkRow);

        return card;
    }

    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        if (user.isEmpty() || pass.isEmpty()) {
            shake(usernameField.getParent().getParent());
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Clixky", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // TODO: Replace with real UserDAO.login(user, pass) check
        if (onLoginSuccess != null) onLoginSuccess.run();
    }

    // ── Helpers ──────────────────────────────────────────────

    static JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(GREEN_MID);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    static JTextField makeTextField(String placeholder) {
        JTextField f = new JTextField();
        styleTextField(f, placeholder);
        return f;
    }

    static void styleTextField(JTextField f, String placeholder) {
        f.setFont(FONT_INPUT);
        f.setBackground(BG_DEEP);
        f.setForeground(TEXT_MAIN);
        f.setCaretColor(GREEN_HI);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL, 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Placeholder logic
        f.setText(placeholder);
        f.setForeground(GREEN_DIM);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) {
                    f.setText("");
                    f.setForeground(TEXT_MAIN);
                }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(placeholder);
                    f.setForeground(GREEN_DIM);
                }
            }
        });

        // Hover border
        f.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(GREEN_MID, 1),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));
            }
            public void mouseExited(MouseEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COL, 1),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));
            }
        });
    }

    static JButton makeButton(String text, Color bg, Color fg, Color border) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                super.paintComponent(g);
            }
        };
        b.setFont(FONT_BTN);
        b.setForeground(fg);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setForeground(GOLD); }
            public void mouseExited(MouseEvent e)  { b.setForeground(fg); }
        });
        return b;
    }

    static void shake(Component c) {
        Point orig = c.getLocation();
        Timer t = new Timer(30, null);
        int[] steps = {-8, 8, -6, 6, -4, 4, 0};
        int[] idx = {0};
        t.addActionListener(e -> {
            if (idx[0] < steps.length) {
                c.setLocation(orig.x + steps[idx[0]++], orig.y);
            } else {
                c.setLocation(orig);
                t.stop();
            }
        });
        t.start();
    }

    // ── RoundPanel helper ─────────────────────────────────────
    static class RoundPanel extends JPanel {
        private final int radius;
        private final Color bg, border;
        RoundPanel(int r, Color bg, Color border) {
            this.radius = r; this.bg = bg; this.border = border;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius * 2, radius * 2);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius * 2, radius * 2);
        }
    }

    // Quick test
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new LoginScreen(
                () -> System.out.println("Login success!"),
                () -> System.out.println("Go to register")
            )
        );
    }
}
