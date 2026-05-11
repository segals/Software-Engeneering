package gcm;

/**
 * Thread b — checks whether a user (whose credentials are correct) is currently locked.
 * Invokes onFree if the account is available, or onLocked if it is blocked.
 */
public class LockCheckThread extends Thread {

    private final String username;
    private final Runnable onFree;
    private final Runnable onLocked;

    public LockCheckThread(String username, Runnable onFree, Runnable onLocked) {
        this.username = username;
        this.onFree = onFree;
        this.onLocked = onLocked;
    }

    @Override
    public void run() {
        if (LoginController.isLocked(username)) {
            onLocked.run();
        } else {
            onFree.run();
        }
    }
}
