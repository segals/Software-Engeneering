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
 * Receives the list of valid users from MainApp via the static field `users`.
 * On success: loads welcome.fxml and replaces the current scene on the same Stage.
 * On failure: shows an inline error. After 3 failures for the same username the
 * account is locked for 10 minutes, with a live countdown shown in the error label.
 */
public class LoginController {

    public static ArrayList<User> users = new ArrayList<>();

    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCKOUT_MS = 10 * 60 * 1000L;

    // Keyed by entered username so each account is tracked independently
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

        // Check if this username is currently locked out
        Long lockedUntil = lockoutUntil.get(enteredUsername);
        if (lockedUntil != null && System.currentTimeMillis() < lockedUntil) {
            startCountdown(enteredUsername, lockedUntil);
            return;
        }

        // Find matching user
        User loggedInUser = null;
        for (User u : users) {
            if (u.getUsername().equals(enteredUsername) &&
                u.getPassword().equals(enteredPassword)) {
                loggedInUser = u;
                break;
            }
        }

        if (loggedInUser != null) {
            failedAttempts.remove(enteredUsername);
            lockoutUntil.remove(enteredUsername);
            stopCountdown();

            try {
                WelcomeController.loggedInUsername = loggedInUser.getUsername();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("welcome.fxml"));
                Scene welcomeScene = new Scene(loader.load());

                Stage stage = (Stage) username.getScene().getWindow();
                stage.setScene(welcomeScene);
                stage.setOnCloseRequest(e -> Platform.exit());

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            int attempts = failedAttempts.getOrDefault(enteredUsername, 0) + 1;
            failedAttempts.put(enteredUsername, attempts);

            if (attempts >= MAX_ATTEMPTS) {
                long until = System.currentTimeMillis() + LOCKOUT_MS;
                lockoutUntil.put(enteredUsername, until);
                startCountdown(enteredUsername, until);
            } else {
                int remaining = MAX_ATTEMPTS - attempts;
                showError("Incorrect username or password. " + remaining + " attempt" + (remaining == 1 ? "" : "s") + " remaining.");
            }
        }
    }

    private void startCountdown(String lockedUsername, long until) {
        stopCountdown();
        loginButton.setDisable(true);

        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            long remaining = until - System.currentTimeMillis();
            if (remaining <= 0) {
                lockoutUntil.remove(lockedUsername);
                failedAttempts.remove(lockedUsername);
                stopCountdown();
                loginButton.setDisable(false);
                showError("Account unlocked. You may try again.");
            } else {
                long mins = remaining / 60_000;
                long secs = (remaining % 60_000) / 1000;
                showLockout(String.format("Too many failed attempts. Try again in %d:%02d.", mins, secs));
            }
        }));
        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();

        // Show the message immediately without waiting for the first tick
        long remaining = until - System.currentTimeMillis();
        long mins = remaining / 60_000;
        long secs = (remaining % 60_000) / 1000;
        showLockout(String.format("Too many failed attempts. Try again in %d:%02d.", mins, secs));
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
    }

    private void showLockout(String msg) {
        errorLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #f77f00;");
        errorLabel.setText(msg);
    }
}
