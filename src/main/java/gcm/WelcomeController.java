package gcm;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * WelcomeController manages the welcome screen (welcome.fxml).
 * Shown after a successful login.
 *
 * The logged-in username is passed from LoginController via the static field
 * `loggedInUsername`, which must be set BEFORE welcome.fxml is loaded.
 */
public class WelcomeController {

    /** Username of the successfully logged-in user. Set by LoginController. */
    public static String loggedInUsername = "";

    @FXML private Label welcomeLabel;

    /**
     * Called automatically by JavaFX after welcome.fxml is loaded and all
     * @FXML fields are injected. Sets the welcome message with the full username.
     */
    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + loggedInUsername + "!");
    }
}
