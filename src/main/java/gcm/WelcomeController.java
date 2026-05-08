package gcm;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * WelcomeController manages the welcome screen (welcome.fxml).
 * Shown after a successful login.
 */
public class WelcomeController {

    public static String loggedInUsername = "";

    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        welcomeLabel.setText("You are signed in as\n" + loggedInUsername);
    }

    @FXML
    private void handleLogout() {
        try {
            loggedInUsername = "";

            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Scene loginScene = new Scene(loader.load());

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(loginScene);
            stage.setOnCloseRequest(e -> Platform.exit());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
