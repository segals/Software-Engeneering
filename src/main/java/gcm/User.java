package gcm;

/**
 * User represents a single system user with a validated username (email) and password.
 * Validation happens at construction time — if anything is invalid, an
 * IllegalArgumentException is thrown with a descriptive message.
 *
 * Username rules:
 *   - Must be a valid email: part1@part2.part3
 *   - part1: letters, digits, and any of: . _ - + %
 *   - part2: must start with letter or digit, allows letters, digits, - and .
 *   - part3: letters only, at least 2 characters
 *   - Total length: max 50 characters
 *
 * Password rules:
 *   - Length: 8 to 12 characters (inclusive)
 *   - Must contain at least one letter, one digit, one special char from: !@#$%^&*)(
 *   - No other characters are allowed
 */
public class User {
    private String username;
    private String password;

    private static final String EMAIL_REGEX =
        "^[a-zA-Z0-9._\\-+%]+@[a-zA-Z0-9][a-zA-Z0-9.\\-]*\\.[a-zA-Z]{2,}$";

    /**
     * Creates a new User. Throws IllegalArgumentException if username or password is invalid.
     * @param username must be a valid email, max 50 chars
     * @param password must be 8-12 chars with letter, digit, and special char
     */
    public User(String username, String password) throws IllegalArgumentException {
        if (username.length() > 50) {
            throw new IllegalArgumentException("Username is too long, try something shorter");
        }
        if (!username.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Please enter a valid Email as username");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Your password is too short, add more characters");
        }
        if (password.length() > 12) {
            throw new IllegalArgumentException("Your password is too long, try a shorter one");
        }
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Please enter a valid password");
        }
        this.username = username;
        this.password = password;
    }

    /**
     * Validates password content:
     * - Must have at least one letter, one digit, one special char from: !@#$%^&*)(
     * - Any other character causes immediate rejection
     */
    private boolean isValidPassword(String pwd) {
        boolean hasLetter = false, hasDigit = false, hasSpecial = false;
        for (char c : pwd.toCharArray()) {
            if (Character.isLetter(c))             hasLetter = true;
            else if (Character.isDigit(c))         hasDigit = true;
            else if ("!@#$%^&*)(".indexOf(c) >= 0) hasSpecial = true;
            else return false;
        }
        return hasLetter && hasDigit && hasSpecial;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    @Override
    public String toString() {
        return username + " " + password;
    }
}
