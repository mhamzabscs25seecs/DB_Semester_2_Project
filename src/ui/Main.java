package ui;

import ui.DashboardScreen.PostData;
import dao.Session;
import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Always start Swing on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> showLogin());
    }

    // ── LOGIN ────────────────────────────────────────────────
    static LoginScreen loginScreen;

    static void showLogin() {
        loginScreen = new LoginScreen(
            () -> {
                // On successful login → go to Dashboard
                String user = Session.getCurrentUsername();
                loginScreen.dispose();
                showDashboard(user);
            },
            () -> {
                // "Register" link clicked
                loginScreen.dispose();
                showRegister();
            }
        );
    }

    // ── REGISTER ─────────────────────────────────────────────
    static RegisterScreen registerScreen;

    static void showRegister() {
        registerScreen = new RegisterScreen(
            () -> {
                // Registration success → go to Dashboard
                String user = Session.getCurrentUsername();
                registerScreen.dispose();
                showDashboard(user);
            },
            () -> {
                // "Sign in" link clicked → back to Login
                registerScreen.dispose();
                showLogin();
            }
        );
    }

    // ── DASHBOARD ────────────────────────────────────────────
    static DashboardScreen dashboardScreen;

    static void showDashboard(String username) {
        dashboardScreen = new DashboardScreen(
            username,
            post -> {
                // Post card clicked → open PostViewScreen
                dashboardScreen.dispose();
                showPostView(post, username);
            }
        );
    }

    // ── POST VIEW ────────────────────────────────────────────
    static PostViewScreen postViewScreen;

    static void showPostView(PostData post, String username) {
        postViewScreen = new PostViewScreen(
            post,
            username,
            () -> {
                // Back button → return to Dashboard
                postViewScreen.dispose();
                showDashboard(username);
            }
        );
    }
}
