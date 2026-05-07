package gcm;

import javafx.application.Application;

/**
 * Launcher is the actual program entry point (contains main()).
 * It exists separately from MainApp because in the Java module system,
 * the class extending Application cannot have the main() method directly.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
