package gcm;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * MainApp is the JavaFX Application class — the entry point for the GUI.
 *
 * Responsibilities:
 * 1. Read Users.txt from resources and build an ArrayList of valid User objects
 * 2. Pass the users list to LoginController before the scene loads
 * 3. Load login.fxml and display it in a Stage titled "Users Login"
 * 4. Ensure that clicking X closes the window and terminates the program
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        ArrayList<User> users = loadUsers();

        // Pass users list to LoginController before FXML is loaded
        LoginController.users = users;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("Users Login");
        stage.setScene(scene);

        // Requirement 2.4: clicking X must terminate the program
        stage.setOnCloseRequest(e -> Platform.exit());

        stage.show();
    }

    /**
     * Reads Users.txt from resources line by line.
     * Valid users are added to the list; invalid lines are printed to stderr and skipped.
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
                if (parts.length < 2) {
                    System.err.println(trimmed + "\nmissing username or password\n");
                    continue;
                }

                try {
                    users.add(new User(parts[0], parts[1]));
                } catch (IllegalArgumentException e) {
                    System.err.println(trimmed + "\n" + e.getMessage() + "\n");
                }
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("Error reading Users.txt: " + e.getMessage());
        }
        return users;
    }
}
