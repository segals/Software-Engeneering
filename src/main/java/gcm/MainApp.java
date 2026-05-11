package gcm;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Optional;

/**
 * MainApp is the JavaFX Application class — the entry point for the GUI.
 *
 * Before opening the login screen it prompts the user for:
 *   n — maximum number of failed login attempts before lockout
 *   t — lockout duration in seconds
 *
 * Both values are stored on LoginController so the login logic can use them.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Collect n and t from the user before any login screen is shown
        int[] params = showParameterDialog();
        if (params == null) {
            Platform.exit();
            return;
        }
        LoginController.maxAttempts    = params[0];
        LoginController.lockoutSeconds = params[1];

        ArrayList<User> users = loadUsers();
        LoginController.users = users;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("Users Login");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> Platform.exit());
        stage.show();
    }

    /**
     * Shows a dialog asking for n (max attempts) and t (lockout seconds).
     * Loops until the user provides valid positive integers or cancels.
     * Returns null if the user cancels (application should exit).
     */
    private int[] showParameterDialog() {
        while (true) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Login Security Setup");
            dialog.setHeaderText("Set login security parameters");

            GridPane grid = new GridPane();
            grid.setHgap(12);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField nField = new TextField("3");
            TextField tField = new TextField("60");

            grid.add(new Label("Max failed attempts (n):"), 0, 0);
            grid.add(nField, 1, 0);
            grid.add(new Label("Lockout duration in seconds (t):"), 0, 1);
            grid.add(tField, 1, 1);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isEmpty() || result.get() == ButtonType.CANCEL) {
                return null;
            }

            try {
                int n = Integer.parseInt(nField.getText().trim());
                int t = Integer.parseInt(tField.getText().trim());
                if (n > 0 && t > 0) return new int[]{n, t};
            } catch (NumberFormatException ignored) {}

            new Alert(Alert.AlertType.WARNING,
                "Both values must be positive integers. Please try again.",
                ButtonType.OK).showAndWait();
        }
    }

    /**
     * Reads Users.txt from resources line by line.
     * Valid users are added to the list; invalid lines are silently skipped.
     */
    private ArrayList<User> loadUsers() {
        ArrayList<User> users = new ArrayList<>();
        try {
            InputStream is = getClass().getResourceAsStream("Users.txt");
            if (is == null) {
                System.err.println("ERROR: Users.txt not found in resources!");
                return users;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                String[] parts = trimmed.split("\\s+");
                if (parts.length < 2) continue;
                try {
                    users.add(new User(parts[0], parts[1]));
                } catch (IllegalArgumentException e) {
                    // skip invalid entries silently
                }
            }
            reader.close();
        } catch (Exception e) {
            // ignore file read errors
        }
        return users;
    }
}
