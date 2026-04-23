package ui;

import ui.DashboardScreen.PostData;
import dao.Session;
import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Always start Swing on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginScreen.installGlobalFontZoomShortcuts();
            showLogin();
        });
    }

    // ── LOGIN ────────────────────────────────────────────────
    static LoginScreen loginScreen;

    static void showLogin() {
        loginScreen = new LoginScreen(
            () -> {
                // On successful login -> greeting, then dashboard
                String user = Session.getCurrentUsername();
                loginScreen.dispose();
                showLoginGreeting(user);
            },
            () -> {
                // "Register" link clicked
                loginScreen.dispose();
                showRegister();
            }
        );
    }

    static void showLoginGreeting(String username) {
        welcomeScreen = new WelcomeScreen(
            null,
            () -> {
                welcomeScreen.dispose();
                showDashboard(username);
            },
            false
        );
    }

    // ── REGISTER ─────────────────────────────────────────────
    static RegisterScreen registerScreen;

    static void showRegister() {
        registerScreen = new RegisterScreen(
            () -> {
                // Registration success -> welcome choice
                String user = Session.getCurrentUsername();
                registerScreen.dispose();
                showWelcome(user);
            },
            () -> {
                // "Sign in" link clicked → back to Login
                registerScreen.dispose();
                showLogin();
            }
        );
    }

    // ── WELCOME ──────────────────────────────────────────────
    static WelcomeScreen welcomeScreen;

    static void showWelcome(String username) {
        welcomeScreen = new WelcomeScreen(
            () -> {
                welcomeScreen.dispose();
                showProfileSetup(username);
            },
            () -> {
                welcomeScreen.dispose();
                showDashboard(username);
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
