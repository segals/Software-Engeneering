package gcm;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * LoginController manages the login screen (login.fxml).
 *
 * maxAttempts and lockoutSeconds are set by MainApp from the startup dialog.
 *
 * Wrong credentials  → FailedAttemptThread (Thread a) records the failure and
 *                       locks the account when maxAttempts is reached.
 * Correct credentials → LockCheckThread (Thread b) verifies the account is not
 *                       locked before opening the Welcome screen.
 *
 * After a lockout the countdown resets the attempt counter so the user gets
 * another maxAttempts tries (repeating cycle).
 */
public class LoginController {

    // Populated by MainApp before the scene is shown
    public static ArrayList<User> users = new ArrayList<>();

    // Configured at startup via the parameter dialog
    public static int  maxAttempts    = 3;
    public static long lockoutSeconds = 60;

    // Guarded by LoginController.class monitor for thread safety
    private static final Map<String, Integer> failedAttempts = new HashMap<>();
    private static final Map<String, Long>    lockoutUntil   = new HashMap<>();

    @FXML private TextField     username;
    @FXML private PasswordField password;
    @FXML private Label         errorLabel;
    @FXML private Button        loginButton;

    private Timeline countdownTimer;

    @FXML
    private void handleLogin() {
        String enteredUsername = username.getText().trim();
        String enteredPassword = password.getText().trim();

        // Quick UI check: if still in lockout window, refresh the countdown display
        Long lockedUntil = lockoutUntil.get(enteredUsername);
        if (lockedUntil != null && System.currentTimeMillis() < lockedUntil) {
            startCountdown(enteredUsername, lockedUntil);
            return;
        }

        User loggedInUser = null;
        for (User u : users) {
            if (u.getUsername().equals(enteredUsername) &&
                    u.getPassword().equals(enteredPassword)) {
                loggedInUser = u;
                break;
            }
        }

        if (loggedInUser != null) {
            final User finalUser = loggedInUser;
            // Thread b: verify account is not locked before granting access
            new LockCheckThread(enteredUsername,
                () -> Platform.runLater(() -> navigateToWelcome(finalUser)),
                () -> Platform.runLater(() -> {
                    Long until = lockoutUntil.get(enteredUsername);
                    if (until != null) startCountdown(enteredUsername, until);
                })
            ).start();
        } else {
            // Thread a: record the failed attempt; lock if max attempts reached
            new FailedAttemptThread(enteredUsername,
                () -> Platform.runLater(() -> {
                    Long until = lockoutUntil.get(enteredUsername);
                    if (until != null) startCountdown(enteredUsername, until);
                }),
                () -> Platform.runLater(() -> {
                    int attempts = failedAttempts.getOrDefault(enteredUsername, 0);
                    int remaining = maxAttempts - attempts;
                    showError("Incorrect username or password. " +
                        remaining + " attempt" + (remaining == 1 ? "" : "s") + " remaining.");
                })
            ).start();
        }
    }

    // ── Thread-safe state management ────────────────────────────────────────

    public static synchronized void recordFailedAttempt(String username) {
        Long prev = lockoutUntil.get(username);
        if (prev != null && System.currentTimeMillis() >= prev) {
            failedAttempts.remove(username);
            lockoutUntil.remove(username);
        }
        int attempts = failedAttempts.getOrDefault(username, 0) + 1;
        failedAttempts.put(username, attempts);
        if (attempts >= maxAttempts) {
            long until = System.currentTimeMillis() + lockoutSeconds * 1000L;
            lockoutUntil.put(username, until);
        }
    }

    public static synchronized boolean isLocked(String username) {
        Long until = lockoutUntil.get(username);
        return until != null && System.currentTimeMillis() < until;
    }

    public static synchronized void unlock(String username) {
        lockoutUntil.remove(username);
        failedAttempts.remove(username);
    }

    // ── UI helpers ───────────────────────────────────────────────────────────

    private void navigateToWelcome(User user) {
        unlock(user.getUsername());
        stopCountdown();
        try {
            WelcomeController.loggedInUsername = user.getUsername();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("welcome.fxml"));
            Scene welcomeScene = new Scene(loader.load());
            Stage stage = (Stage) username.getScene().getWindow();
            stage.setScene(welcomeScene);
            stage.setOnCloseRequest(e -> Platform.exit());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Disables the login button and ticks the error label every second with the
     * remaining lockout time. When it expires the button is re-enabled and the
     * attempt counter is reset so the cycle starts fresh.
     */
    private void startCountdown(String lockedUsername, long until) {
        stopCountdown();
        loginButton.setDisable(true);

        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            long remaining = until - System.currentTimeMillis();
            if (remaining <= 0) {
                unlock(lockedUsername);
                stopCountdown();
                loginButton.setDisable(false);
                showError("Account unlocked. You may try again.");
            } else {
                long mins = remaining / 60_000;
                long secs = (remaining % 60_000) / 1000;
                showLockout(String.format(
                    "Too many failed attempts. Try again in %d:%02d.", mins, secs));
            }
        }));
        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();

        // Show immediately without waiting for the first tick
        long remaining = until - System.currentTimeMillis();
        long mins = remaining / 60_000;
        long secs = (remaining % 60_000) / 1000;
        showLockout(String.format(
            "Too many failed attempts. Try again in %d:%02d.", mins, secs));
    }

    private void stopCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
    }

    private void showError(String msg) {
        errorLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #ef233c;");
        errorLabel.setText(msg);
        resizeToContent();
    }

    private void showLockout(String msg) {
        errorLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #f77f00;");
        errorLabel.setText(msg);
        resizeToContent();
    }

    private void resizeToContent() {
        Platform.runLater(() -> {
            javafx.stage.Stage s = (javafx.stage.Stage) errorLabel.getScene().getWindow();
            s.sizeToScene();
        });
    }
}
