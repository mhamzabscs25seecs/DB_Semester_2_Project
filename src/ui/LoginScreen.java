package stopwatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class LoginScreen extends JFrame {

    // ── Cyberpunk Palette ─────────────────────────────────────
    static final Color BG_DEEP    = new Color(8,   10,  20);
    static final Color BG_PANEL   = new Color(12,  16,  32);
    static final Color BG_CARD    = new Color(18,  22,  45);
    static final Color BORDER_COL = new Color(40,  45,  90);
    static final Color NEON_PINK  = new Color(255,  60, 180);
    static final Color NEON_CYAN  = new Color(0,   220, 255);
    static final Color NEON_MID   = new Color(160,  60, 200);
    static final Color NEON_DIM   = new Color(60,   40, 100);
    static final Color GOLD       = new Color(255, 210,  80);
    static final Color TEXT_MAIN  = new Color(200, 220, 255);
    static final Color TEXT_MUTED = new Color(90,  100, 160);
    static final Font  FONT_TITLE = new Font("Segoe UI", Font.BOLD,  28);
    static final Font  FONT_LABEL = new Font("Segoe UI", Font.BOLD,  11);
    static final Font  FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font  FONT_BTN   = new Font("Segoe UI", Font.BOLD,  13);
    static final Font  FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    private JTextField     usernameField;
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
        setSize(500, 620);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(400, 540));

        JPanel root = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Deep dark background
                g2.setColor(BG_DEEP);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Pink glow top-left
                int cx = getWidth() / 4, cy = getHeight() / 4;
                RadialGradientPaint glow1 = new RadialGradientPaint(cx, cy, 280,
                    new float[]{0f, 1f},
                    new Color[]{new Color(255, 60, 180, 35), new Color(0, 0, 0, 0)});
                g2.setPaint(glow1);
                g2.fillOval(cx - 280, cy - 280, 560, 560);
                // Cyan glow bottom-right
                int cx2 = getWidth() * 3 / 4, cy2 = getHeight() * 3 / 4;
                RadialGradientPaint glow2 = new RadialGradientPaint(cx2, cy2, 240,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0, 220, 255, 25), new Color(0, 0, 0, 0)});
                g2.setPaint(glow2);
                g2.fillOval(cx2 - 240, cy2 - 240, 480, 480);
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
        card.setPreferredSize(new Dimension(360, 480));
        card.setBorder(BorderFactory.createEmptyBorder(36, 36, 36, 36));

        // Logo
        JLabel logo = new JLabel("CLIXKY");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        logo.setForeground(NEON_PINK);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tagline
        JLabel tag = new JLabel("A RELATIONAL COMMUNITY PLATFORM");
        tag.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        tag.setForeground(NEON_DIM);
        tag.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Neon divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(NEON_PINK.getRed(), NEON_PINK.getGreen(), NEON_PINK.getBlue(), 80));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username
        JLabel uLabel = makeLabel("USERNAME");
        usernameField = makeTextField("Enter your username");

        // Password
        JLabel pLabel = makeLabel("PASSWORD");
        passwordField = new JPasswordField();
        styleTextField(passwordField, "Enter your password");

        // Sign In button
        JButton signInBtn = makeButton("SIGN  IN", BG_CARD, NEON_CYAN, BORDER_COL);
        signInBtn.addActionListener(e -> handleLogin());

        // Register link
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);
        linkRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel linkText = new JLabel("Don't have an account?");
        linkText.setFont(FONT_SMALL);
        linkText.setForeground(TEXT_MUTED);
        JLabel linkBtn = new JLabel("Register →");
        linkBtn.setFont(FONT_SMALL);
        linkBtn.setForeground(NEON_CYAN);
        linkBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkBtn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { if (onGoRegister != null) onGoRegister.run(); }
            public void mouseEntered(MouseEvent e) { linkBtn.setForeground(NEON_PINK); }
            public void mouseExited(MouseEvent e)  { linkBtn.setForeground(NEON_CYAN); }
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
        l.setForeground(NEON_MID);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20)); 
        l.setAlignmentX(Component.CENTER_ALIGNMENT); 
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
        f.setCaretColor(NEON_CYAN);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL, 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        f.setAlignmentX(Component.CENTER_ALIGNMENT); 

        // Placeholder logic
        f.setText(placeholder);
        f.setForeground(NEON_DIM);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) {
                    f.setText("");
                    f.setForeground(TEXT_MAIN);
                }
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(NEON_PINK, 1),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(placeholder);
                    f.setForeground(NEON_DIM);
                }
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COL, 1),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));
            }
        });

        // Hover border
        f.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!f.hasFocus())
                    f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(NEON_MID, 1),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)
                    ));
            }
            public void mouseExited(MouseEvent e) {
                if (!f.hasFocus())
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
                // Gradient fill
                GradientPaint gp = new GradientPaint(0, 0,
                    getModel().isPressed() ? BG_CARD.brighter() : BG_CARD,
                    0, getHeight(),
                    getModel().isPressed() ? BG_DEEP : new Color(25, 15, 40));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // Neon border glow
                g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 180));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
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
            public void mouseEntered(MouseEvent e) { b.setForeground(NEON_PINK); }
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
            // Neon border with slight glow
            g2.setColor(new Color(NEON_PINK.getRed(), NEON_PINK.getGreen(), NEON_PINK.getBlue(), 60));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius * 2, radius * 2);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new LoginScreen(
                () -> System.out.println("Login success!"),
                () -> System.out.println("Go to register")
            )
        );
    }
}