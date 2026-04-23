package ui;

import dao.Session;
import dao.UserDAO;      // The Data access object we created for this
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class LoginScreen extends JFrame {

    // ── Shared Palette / Fonts ────────────────────────────────
    static boolean LIGHT_MODE = false;
    static Color BG_DEEP    = new Color(8,   10,  20);
    static Color BG_PANEL   = new Color(12,  16,  32);
    static Color BG_CARD    = new Color(18,  22,  45);
    static Color BORDER_COL = new Color(40,  45,  90);
    static Color NEON_PINK  = new Color(255,  60, 180);
    static Color NEON_CYAN  = new Color(0,   220, 255);
    static Color NEON_MID   = new Color(160,  60, 200);
    static Color NEON_DIM   = new Color(60,   40, 100);
    static Color GOLD       = new Color(255, 210,  80);
    static Color TEXT_MAIN  = new Color(200, 220, 255);
    static Color TEXT_MUTED = new Color(90,  100, 160);
    static Font  FONT_TITLE = new Font("Segoe UI", Font.BOLD,  32);
    static Font  FONT_LABEL = new Font("Segoe UI", Font.BOLD,  12);
    static Font  FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 16);
    static Font  FONT_BTN   = new Font("Segoe UI", Font.BOLD,  14);
    static Font  FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 13);
    private static float FONT_SCALE = 1.0f;
    private static boolean zoomShortcutsInstalled = false;

    static void setLightMode(boolean enabled) {
        LIGHT_MODE = enabled;

        if (enabled) {
            BG_DEEP    = new Color(235, 244, 255);
            BG_PANEL   = new Color(248, 251, 255);
            BG_CARD    = new Color(230, 241, 255);
            BORDER_COL = new Color(146, 177, 230);
            NEON_PINK  = new Color(214, 43, 152);
            NEON_CYAN  = new Color(21, 132, 204);
            NEON_MID   = new Color(126, 78, 190);
            NEON_DIM   = new Color(96, 107, 150);
            GOLD       = new Color(190, 132, 18);
            TEXT_MAIN  = new Color(24, 37, 70);
            TEXT_MUTED = new Color(78, 90, 124);
        } else {
            BG_DEEP    = new Color(8,   10,  20);
            BG_PANEL   = new Color(12,  16,  32);
            BG_CARD    = new Color(18,  22,  45);
            BORDER_COL = new Color(40,  45,  90);
            NEON_PINK  = new Color(255,  60, 180);
            NEON_CYAN  = new Color(0,   220, 255);
            NEON_MID   = new Color(160,  60, 200);
            NEON_DIM   = new Color(60,   40, 100);
            GOLD       = new Color(255, 210,  80);
            TEXT_MAIN  = new Color(200, 220, 255);
            TEXT_MUTED = new Color(90,  100, 160);
        }
    }

    static void installGlobalFontZoomShortcuts() {
        if (zoomShortcutsInstalled) {
            return;
        }

        zoomShortcutsInstalled = true;
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() != KeyEvent.KEY_PRESSED || !isZoomModifierDown(e)) {
                return false;
            }

            int key = e.getKeyCode();
            if (key == KeyEvent.VK_EQUALS || key == KeyEvent.VK_PLUS || key == KeyEvent.VK_ADD) {
                adjustFontScale(0.1f);
                return true;
            }
            if (key == KeyEvent.VK_MINUS || key == KeyEvent.VK_SUBTRACT) {
                adjustFontScale(-0.1f);
                return true;
            }
            if (key == KeyEvent.VK_0) {
                FONT_SCALE = 1.0f;
                applyFontScaleToOpenWindows();
                return true;
            }

            return false;
        });

        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof WindowEvent windowEvent
                    && windowEvent.getID() == WindowEvent.WINDOW_OPENED) {
                applyFontScale(windowEvent.getWindow());
            }
        }, AWTEvent.WINDOW_EVENT_MASK);
    }

    private static boolean isZoomModifierDown(KeyEvent e) {
        return e.isControlDown() || e.isMetaDown();
    }

    private static void adjustFontScale(float delta) {
        FONT_SCALE = Math.max(0.8f, Math.min(1.6f, FONT_SCALE + delta));
        applyFontScaleToOpenWindows();
    }

    private static void applyFontScaleToOpenWindows() {
        for (Window window : Window.getWindows()) {
            applyFontScale(window);
        }
    }

    private static void applyFontScale(Component component) {
        if (component == null) {
            return;
        }

        Font font = component.getFont();
        if (font != null) {
            Font baseFont = font;
            if (component instanceof JComponent jComponent) {
                Object stored = jComponent.getClientProperty("clixky.baseFont");
                if (stored instanceof Font storedFont) {
                    baseFont = storedFont;
                } else {
                    jComponent.putClientProperty("clixky.baseFont", font);
                }
            }
            component.setFont(baseFont.deriveFont(baseFont.getSize2D() * FONT_SCALE));
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyFontScale(child);
            }
        }

        component.invalidate();
        component.validate();
        component.repaint();
    }

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
        setSize(740, 600);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(460, 410));

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
        JPanel card = buildCard();
        clearFieldFocusOnBlankClick(root);
        clearFieldFocusOnBlankClick(card);
        root.add(card);
        root.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeLoginCard(card, root);
            }
        });
        setContentPane(root);
        resizeLoginCard(card, root);
        setVisible(true);
    }

    private JPanel buildCard() {
        JPanel card = new RoundPanel(16, BG_PANEL, BORDER_COL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(360, 480));
        card.setBorder(BorderFactory.createEmptyBorder(34, 34, 34, 34));

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
        stylePasswordField(passwordField, "Enter your password");
        passwordField.addActionListener(e -> handleLogin());

        // Sign In button
        JButton signInBtn = makeButton("SIGN  IN", BG_CARD, NEON_CYAN, BORDER_COL);
        signInBtn.addActionListener(e -> handleLogin());

        JButton themeBtn = makeButton(LIGHT_MODE ? "DARK MODE" : "LIGHT MODE", BG_CARD, NEON_PINK, BORDER_COL);
        themeBtn.addActionListener(e -> {
            setLightMode(!LIGHT_MODE);
            dispose();
            new LoginScreen(onLoginSuccess, onGoRegister);
        });

        JButton soundBtn = makeSoundToggleButton();

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
            public void mouseClicked(MouseEvent e) {
                SoundFX.click();
                if (onGoRegister != null) {
                    dispose();
                    onGoRegister.run();
                }
            }
            public void mouseEntered(MouseEvent e) { linkBtn.setForeground(NEON_PINK); }
            public void mouseExited(MouseEvent e)  { linkBtn.setForeground(NEON_CYAN); }
        });
        linkRow.add(linkText);
        linkRow.add(linkBtn);

        // Layout
        card.add(logo);
        card.add(Box.createVerticalStrut(4));
        card.add(tag);
        card.add(Box.createVerticalStrut(18));
        card.add(sep);
        card.add(Box.createVerticalStrut(22));
        card.add(uLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(14));
        card.add(pLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(24));
        card.add(signInBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(themeBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(soundBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(linkRow);

        return card;
    }

    private void resizeLoginCard(JPanel card, JComponent root) {
        int cardWidth = clamp((int) (root.getWidth() * 0.48), 280, 500);
        int cardHeight = clamp((int) (root.getHeight() * 0.80), 340, 580);
        Dimension size = new Dimension(cardWidth, cardHeight);
        card.setPreferredSize(size);
        card.setMinimumSize(size);
        card.revalidate();
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = getPasswordText(passwordField, "Enter your password").trim();

        if (user.isEmpty() || pass.isEmpty()) {
            SoundFX.error();
            shake(usernameField.getParent().getParent());
            showCyberError("Missing Data", "Please fill in all fields.");
            return;
        }

        UserDAO userDAO = new UserDAO();
        UserDAO.LoggedInUser loggedInUser = userDAO.login (user, pass);

        // If the user is not found, it is null
        if (loggedInUser == null) {
            SoundFX.error();
            showCyberError("Access Denied", "Invalid username or password.");
            return;
        }

        Session.login (loggedInUser);
        SoundFX.success();

        if (onLoginSuccess != null) {
            dispose();
            onLoginSuccess.run();
        }

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
        f.putClientProperty("clixky.placeholder", placeholder);
        f.setFont(FONT_INPUT);
        f.setBackground(BG_DEEP);
        f.setForeground(TEXT_MAIN);
        f.setCaretColor(NEON_CYAN);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COL, 1),
            BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
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
                    BorderFactory.createEmptyBorder(7, 12, 7, 12)
                ));
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setForeground(NEON_DIM);
                    f.setText(placeholder);
                }
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COL, 1),
                    BorderFactory.createEmptyBorder(7, 12, 7, 12)
                ));
            }
        });

        // Hover border
        f.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!f.hasFocus())
                    f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(NEON_MID, 1),
                        BorderFactory.createEmptyBorder(7, 12, 7, 12)
                    ));
            }
            public void mouseExited(MouseEvent e) {
                if (!f.hasFocus())
                    f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COL, 1),
                        BorderFactory.createEmptyBorder(7, 12, 7, 12)
                    ));
            }
        });
    }

    static void stylePasswordField(JPasswordField f, String placeholder) {
        char echoChar = f.getEchoChar();
        FocusListener[] originalFocusListeners = f.getFocusListeners();
        styleTextField(f, placeholder);
        for (FocusListener listener : f.getFocusListeners()) {
            boolean wasOriginal = false;
            for (FocusListener original : originalFocusListeners) {
                if (listener == original) {
                    wasOriginal = true;
                    break;
                }
            }
            if (!wasOriginal) {
                f.removeFocusListener(listener);
            }
        }
        f.setEchoChar((char) 0);

        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (new String(f.getPassword()).equals(placeholder)) {
                    f.setText("");
                    f.setForeground(TEXT_MAIN);
                    f.setEchoChar(echoChar);
                }
            }

            public void focusLost(FocusEvent e) {
                if (new String(f.getPassword()).isEmpty()) {
                    f.setEchoChar((char) 0);
                    f.setText(placeholder);
                    f.setForeground(NEON_DIM);
                }
            }
        });
    }

    static String getPasswordText(JPasswordField f, String placeholder) {
        String text = new String(f.getPassword());
        return isPlaceholderText(f) || text.equals(placeholder) && f.getForeground().equals(NEON_DIM) ? "" : text;
    }

    static boolean isPlaceholderText(JTextField field) {
        Object placeholder = field.getClientProperty("clixky.placeholder");
        return placeholder instanceof String text && field.getText().equals(text);
    }

    static void clearFieldFocusOnBlankClick(JComponent component) {
        component.setFocusable(true);
        component.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                component.requestFocusInWindow();
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
                    getModel().isPressed() ? BG_DEEP : (LIGHT_MODE ? new Color(216, 232, 255) : new Color(25, 15, 40)));
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
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> SoundFX.click());
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setForeground(NEON_PINK); }
            public void mouseExited(MouseEvent e)  { b.setForeground(fg); }
        });
        return b;
    }

    static JButton makeSoundToggleButton() {
        JButton b = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color iconColor = SoundFX.isEnabled() ? GOLD : TEXT_MUTED;
                if (getModel().isRollover()) {
                    iconColor = NEON_PINK;
                }

                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(iconColor.getRed(), iconColor.getGreen(), iconColor.getBlue(), 170));
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);

                int centerY = getHeight() / 2;
                int left = getWidth() / 2 - 12;
                Polygon speaker = new Polygon();
                speaker.addPoint(left, centerY - 5);
                speaker.addPoint(left + 5, centerY - 5);
                speaker.addPoint(left + 11, centerY - 10);
                speaker.addPoint(left + 11, centerY + 10);
                speaker.addPoint(left + 5, centerY + 5);
                speaker.addPoint(left, centerY + 5);
                g2.setColor(iconColor);
                g2.fillPolygon(speaker);

                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (SoundFX.isEnabled()) {
                    g2.drawArc(left + 12, centerY - 7, 8, 14, -45, 90);
                    g2.drawArc(left + 15, centerY - 10, 10, 20, -45, 90);
                } else {
                    g2.drawLine(left + 15, centerY - 8, left + 25, centerY + 8);
                    g2.drawLine(left + 25, centerY - 8, left + 15, centerY + 8);
                }
            }
        };
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setPreferredSize(new Dimension(42, 34));
        b.setMinimumSize(new Dimension(42, 34));
        b.setMaximumSize(new Dimension(42, 34));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setToolTipText(SoundFX.isEnabled() ? "Mute" : "Unmute");
        b.addActionListener(e -> {
            SoundFX.toggle();
            b.setToolTipText(SoundFX.isEnabled() ? "Mute" : "Unmute");
            b.repaint();
        });
        return b;
    }

    private void showCyberError(String title, String message) {
        showCyberError(this, title, message);
    }

    static void showCyberError(Component parent, String title, String message) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Clixky Alert", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(360, 210);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);
        dialog.setUndecorated(true);

        JPanel panel = new RoundPanel(14, BG_PANEL, NEON_PINK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 22, 28));

        JLabel icon = new JLabel("!");
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 30));
        icon.setForeground(NEON_PINK);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(NEON_PINK);

        JLabel messageLabel = new JLabel(message);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageLabel.setForeground(TEXT_MAIN);

        JButton okButton = makeButton("TRY AGAIN", BG_CARD, NEON_CYAN, BORDER_COL);
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.setMaximumSize(new Dimension(180, 38));
        okButton.addActionListener(e -> dialog.dispose());

        panel.add(icon);
        panel.add(Box.createVerticalStrut(8));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(messageLabel);
        panel.add(Box.createVerticalStrut(22));
        panel.add(okButton);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    static boolean showCyberConfirm(Component parent, String title, String message, String confirmText) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Clixky Confirm", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(390, 230);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);
        dialog.setUndecorated(true);

        boolean[] confirmed = {false};

        JPanel panel = new RoundPanel(14, BG_PANEL, NEON_PINK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 22, 28));

        JLabel icon = new JLabel("!");
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 30));
        icon.setForeground(NEON_PINK);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(NEON_PINK);

        JLabel messageLabel = new JLabel("<html><body style='width:300px; text-align:center; color:" + htmlColor(TEXT_MAIN)
                + "; font-family:Segoe UI; font-size:12px'>" + escapeHtml(message) + "</body></html>");
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);
        buttons.setMaximumSize(new Dimension(270, 38));
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton cancelButton = makeButton("CANCEL", BG_CARD, TEXT_MUTED, BORDER_COL);
        JButton confirmButton = makeButton(confirmText, BG_CARD, NEON_PINK, BORDER_COL);
        cancelButton.addActionListener(e -> dialog.dispose());
        confirmButton.addActionListener(e -> {
            confirmed[0] = true;
            dialog.dispose();
        });

        buttons.add(cancelButton);
        buttons.add(confirmButton);

        panel.add(icon);
        panel.add(Box.createVerticalStrut(8));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(messageLabel);
        panel.add(Box.createVerticalStrut(22));
        panel.add(buttons);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
        return confirmed[0];
    }

    static class ReportInput {
        private final String reason;
        private final String details;

        ReportInput(String reason, String details) {
            this.reason = reason;
            this.details = details;
        }

        String getReason() { return reason; }
        String getDetails() { return details; }
    }

    static ReportInput showReportDialog(Component parent, String targetName) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Report", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(430, 360);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);
        dialog.setUndecorated(true);

        ReportInput[] result = {null};

        JPanel panel = new RoundPanel(14, BG_PANEL, NEON_PINK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("Report " + targetName);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(NEON_PINK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> reasonBox = new JComboBox<>(new String[]{
                "Spam",
                "Harassment",
                "Inappropriate content",
                "Misinformation",
                "Other"
        });
        reasonBox.setBackground(BG_DEEP);
        reasonBox.setForeground(TEXT_MAIN);
        reasonBox.setFont(FONT_INPUT);
        reasonBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        reasonBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea detailsArea = new JTextArea(5, 24);
        detailsArea.setBackground(BG_DEEP);
        detailsArea.setForeground(TEXT_MAIN);
        detailsArea.setCaretColor(NEON_CYAN);
        detailsArea.setFont(FONT_INPUT);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(null);
        detailsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        detailsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        JButton cancel = makeButton("CANCEL", BG_CARD, TEXT_MUTED, BORDER_COL);
        JButton submit = makeButton("SUBMIT REPORT", BG_CARD, NEON_PINK, BORDER_COL);
        cancel.addActionListener(e -> dialog.dispose());
        submit.addActionListener(e -> {
            result[0] = new ReportInput((String) reasonBox.getSelectedItem(), detailsArea.getText().trim());
            dialog.dispose();
        });
        buttons.add(cancel);
        buttons.add(submit);

        panel.add(title);
        panel.add(Box.createVerticalStrut(18));
        panel.add(makeLabel("REASON"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(reasonBox);
        panel.add(Box.createVerticalStrut(14));
        panel.add(makeLabel("DETAILS"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(detailsScroll);
        panel.add(Box.createVerticalStrut(18));
        panel.add(buttons);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
        return result[0];
    }

    static String escapeHtml(String text) {
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

    static String htmlColor(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
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
