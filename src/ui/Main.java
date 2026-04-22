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
                // Registration success -> profile setup, then dashboard
                String user = Session.getCurrentUsername();
                registerScreen.dispose();
                showProfileSetup(user);
            },
            () -> {
                // "Sign in" link clicked → back to Login
                registerScreen.dispose();
                showLogin();
            }
        );
    }

    // ── PROFILE SETUP ────────────────────────────────────────
    static ProfileSetupScreen profileSetupScreen;

    static void showProfileSetup(String username) {
        profileSetupScreen = new ProfileSetupScreen(
            () -> {
                profileSetupScreen.dispose();
                showDashboard(username);
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
