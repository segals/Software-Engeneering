package gcm;

/**
 * Thread a — records a failed login attempt for a given username.
 * If the failure count reaches the configured maximum, the account is locked.
 * Invokes onLocked or onNotLocked on the calling thread once finished.
 */
public class FailedAttemptThread extends Thread {

    private final String username;
    private final Runnable onLocked;
    private final Runnable onNotLocked;

    public FailedAttemptThread(String username, Runnable onLocked, Runnable onNotLocked) {
        this.username = username;
        this.onLocked = onLocked;
        this.onNotLocked = onNotLocked;
    }

    @Override
    public void run() {
        LoginController.recordFailedAttempt(username);
        if (LoginController.isLocked(username)) {
            onLocked.run();
        } else {
            onNotLocked.run();
        }
    }
}
