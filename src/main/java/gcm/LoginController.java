package gcm;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * LoginController manages the login screen (login.fxml).
 *
 * Receives the list of valid users from MainApp via the static field `users`.
 * On success: loads welcome.fxml and replaces the current scene on the same Stage.
 * On failure: displays an inline red error message (no popup, no Alert).
 */
public class LoginController {

    /**
     * Valid users loaded from Users.txt.
     * Set by MainApp before FXML is loaded.
     */
    public static ArrayList<User> users = new ArrayList<>();

    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Label errorLabel;

    /**
     * Called when the user clicks the login button.
     * Searches the users list for a matching username + password pair.
     *
     * On match: switches to the welcome screen on the same Stage.
     * On no match: shows inline red error text, no popup.
     */
    @FXML
    private void handleLogin() {
        String enteredUsername = username.getText().trim();
        String enteredPassword = password.getText().trim();

        User loggedInUser = null;
        for (User u : users) {
            if (u.getUsername().equals(enteredUsername) &&
                u.getPassword().equals(enteredPassword)) {
                loggedInUser = u;
                break;
            }
        }

        if (loggedInUser != null) {
            try {
                WelcomeController.loggedInUsername = loggedInUser.getUsername();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("welcome.fxml"));
                Scene welcomeScene = new Scene(loader.load());

                Stage stage = (Stage) username.getScene().getWindow();
                stage.setScene(welcomeScene);

                // Requirement 2.4: X on the welcome window must also terminate the program
                stage.setOnCloseRequest(e -> Platform.exit());

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Inline error — no popup
            errorLabel.setText("user or password do not match");
            errorLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
