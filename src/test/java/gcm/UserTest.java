package gcm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the User class.
 *
 * Covers all validation rules for username (email) and password,
 * boundary conditions, every allowed special character,
 * and all 9 lines from Users.txt (4 valid, 5 invalid).
 */
class UserTest {

    // ─── Helper: a known-good password and email ───────────────────────────────

    private static final String GOOD_EMAIL = "user@example.com";
    private static final String GOOD_PASS  = "Pass1!ab"; // 8 chars: letter+digit+special

    // ─── Valid users ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Valid email + valid password creates user without exception")
    void valid_basicUser() {
        assertDoesNotThrow(() -> new User(GOOD_EMAIL, GOOD_PASS));
    }

    @Test
    @DisplayName("getUsername() returns the correct username")
    void valid_getUsername() throws Exception {
        User u = new User(GOOD_EMAIL, GOOD_PASS);
        assertEquals(GOOD_EMAIL, u.getUsername());
    }

    @Test
    @DisplayName("getPassword() returns the correct password")
    void valid_getPassword() throws Exception {
        User u = new User(GOOD_EMAIL, GOOD_PASS);
        assertEquals(GOOD_PASS, u.getPassword());
    }

    @Test
    @DisplayName("toString() returns only the username (not 'username password')")
    void valid_toString() throws Exception {
        User u = new User(GOOD_EMAIL, GOOD_PASS);
        assertEquals(GOOD_EMAIL, u.toString());
    }

    // ─── Username: length boundary ────────────────────────────────────────────

    @Test
    @DisplayName("Username exactly 50 chars → valid")
    void username_exactly50chars() {
        // 38 a's + "@" + "example.com" = 38+1+7+1+3 = 50
        String email = "a".repeat(38) + "@example.com";
        assertEquals(50, email.length());
        assertDoesNotThrow(() -> new User(email, GOOD_PASS));
    }

    @Test
    @DisplayName("Username 51 chars → throws 'too long'")
    void username_51chars() {
        // 39 a's + "@" + "example.com" = 51
        String email = "a".repeat(39) + "@example.com";
        assertEquals(51, email.length());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new User(email, GOOD_PASS));
        assertTrue(ex.getMessage().toLowerCase().contains("too long"));
    }

    // ─── Username: format ─────────────────────────────────────────────────────

    @Test
    @DisplayName("No '@' sign → invalid email")
    void username_noAtSign() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("notanemail.com", GOOD_PASS));
    }

    @Test
    @DisplayName("No domain extension → invalid email")
    void username_noDomainExtension() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("user@nodot", GOOD_PASS));
    }

    @Test
    @DisplayName("1-char extension → invalid (minimum is 2)")
    void username_oneCharExtension() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("user@example.c", GOOD_PASS));
    }

    @Test
    @DisplayName("2-char extension → valid")
    void username_twoCharExtension() {
        assertDoesNotThrow(() -> new User("user@example.co", GOOD_PASS));
    }

    @Test
    @DisplayName("Empty username → invalid")
    void username_empty() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("", GOOD_PASS));
    }

    @Test
    @DisplayName("Space in username → invalid")
    void username_withSpace() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("user @example.com", GOOD_PASS));
    }

    @Test
    @DisplayName("Invalid char '~' in local part → invalid")
    void username_invalidCharTilde() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("user~name@example.com", GOOD_PASS));
    }

    @Test
    @DisplayName("Valid special chars in local part (. _ - + %) → valid")
    void username_validSpecialCharsInLocalPart() {
        assertDoesNotThrow(() -> new User("a.b_c-d+e%f@example.com", GOOD_PASS));
    }

    @Test
    @DisplayName("Digit at start of local part → valid")
    void username_digitAtStart() {
        assertDoesNotThrow(() -> new User("1user@example.com", GOOD_PASS));
    }

    @Test
    @DisplayName("Multiple subdomains in domain part → valid")
    void username_multipleSubdomains() {
        assertDoesNotThrow(() -> new User("user@mail.example.com", GOOD_PASS));
    }

    // ─── Password: length boundary ────────────────────────────────────────────

    @Test
    @DisplayName("Password exactly 8 chars → valid")
    void password_exactly8chars() {
        // "Pass1!ab" = 8 chars
        assertDoesNotThrow(() -> new User(GOOD_EMAIL, "Pass1!ab"));
    }

    @Test
    @DisplayName("Password exactly 12 chars → valid")
    void password_exactly12chars() {
        // "Pass1!abcdef" = 12 chars
        assertDoesNotThrow(() -> new User(GOOD_EMAIL, "Pass1!abcdef"));
    }

    @Test
    @DisplayName("Password 7 chars → throws 'too short'")
    void password_7chars() {
        // "Pass1!a" = 7 chars
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new User(GOOD_EMAIL, "Pass1!a"));
        assertTrue(ex.getMessage().toLowerCase().contains("too short"));
    }

    @Test
    @DisplayName("Password 13 chars → throws 'too long'")
    void password_13chars() {
        // "Pass1!abcdefg" = 13 chars
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new User(GOOD_EMAIL, "Pass1!abcdefg"));
        assertTrue(ex.getMessage().toLowerCase().contains("too long"));
    }

    @Test
    @DisplayName("Empty password → throws 'too short'")
    void password_empty() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new User(GOOD_EMAIL, ""));
        assertTrue(ex.getMessage().toLowerCase().contains("too short"));
    }

    // ─── Password: required character types ───────────────────────────────────

    @Test
    @DisplayName("Password with no letter → invalid")
    void password_noLetter() {
        // "12345!@#" = digits + specials, no letters
        assertThrows(IllegalArgumentException.class,
                () -> new User(GOOD_EMAIL, "12345!@#"));
    }

    @Test
    @DisplayName("Password with no digit → invalid")
    void password_noDigit() {
        // "abcde!@#" = letters + specials, no digits
        assertThrows(IllegalArgumentException.class,
                () -> new User(GOOD_EMAIL, "abcde!@#"));
    }

    @Test
    @DisplayName("Password with no special character → invalid")
    void password_noSpecial() {
        // "abcde123" = letters + digits, no specials
        assertThrows(IllegalArgumentException.class,
                () -> new User(GOOD_EMAIL, "abcde123"));
    }

    // ─── Password: disallowed characters ──────────────────────────────────────

    @Test
    @DisplayName("Password with space → invalid")
    void password_withSpace() {
        assertThrows(IllegalArgumentException.class,
                () -> new User(GOOD_EMAIL, "Pass 1!a"));
    }

    @Test
    @DisplayName("Password with dot → invalid")
    void password_withDot() {
        assertThrows(IllegalArgumentException.class,
                () -> new User(GOOD_EMAIL, "Pass1.ab"));
    }

    @Test
    @DisplayName("Password with dash → invalid")
    void password_withDash() {
        assertThrows(IllegalArgumentException.class,
                () -> new User(GOOD_EMAIL, "Pass1-ab"));
    }

    @Test
    @DisplayName("Password with tilde '~' → invalid")
    void password_withTilde() {
        assertThrows(IllegalArgumentException.class,
                () -> new User(GOOD_EMAIL, "Pass1~ab"));
    }

    @Test
    @DisplayName("Password with underscore '_' → invalid")
    void password_withUnderscore() {
        assertThrows(IllegalArgumentException.class,
                () -> new User(GOOD_EMAIL, "Pass1_ab"));
    }

    @Test
    @DisplayName("Password with plus '+' → invalid")
    void password_withPlus() {
        assertThrows(IllegalArgumentException.class,
                () -> new User(GOOD_EMAIL, "Pass1+ab"));
    }

    // ─── Password: every allowed special character ────────────────────────────

    @ParameterizedTest(name = "Special char ''{0}'' is accepted in password")
    @ValueSource(strings = {"!", "@", "#", "$", "%", "^", "&", "*", ")", "("})
    @DisplayName("Each of the 10 allowed special characters is accepted")
    void password_eachAllowedSpecialChar(String special) {
        // "Abc1" + special + "xxx" = 8 chars with letter + digit + special
        String pwd = "Abc1" + special + "xxx";
        assertDoesNotThrow(() -> new User(GOOD_EMAIL, pwd));
    }

    // ─── Users.txt: known valid lines (should create User successfully) ────────

    @Test
    @DisplayName("Users.txt line 1 valid: B_Kernighan@mail.com / !a01011942")
    void usersFile_line1_BKernighan() {
        assertDoesNotThrow(() -> new User("B_Kernighan@mail.com", "!a01011942"));
    }

    @Test
    @DisplayName("Users.txt line 2 valid: hello@world.net / 01041972^H")
    void usersFile_line2_helloWorld() {
        // ^H → ^ is allowed special, H is a letter, digits present → valid
        assertDoesNotThrow(() -> new User("hello@world.net", "01041972^H"));
    }

    @Test
    @DisplayName("Users.txt line 3 valid: +first_error@this.row / passw0rd%")
    void usersFile_line3_plusFirst() {
        // + allowed in local part; passw0rd% has letter+digit+special
        assertDoesNotThrow(() -> new User("+first_error@this.row", "passw0rd%"));
    }

    @Test
    @DisplayName("Users.txt line 7 valid: thisEmailIsok@wow0123456789.co.il / @1234Abcd")
    void usersFile_line7_longDomain() {
        assertDoesNotThrow(() -> new User("thisEmailIsok@wow0123456789.co.il", "@1234Abcd"));
    }

    // ─── Users.txt: known invalid lines (should throw) ────────────────────────

    @Test
    @DisplayName("Users.txt line 4 invalid: 'hello' is not a valid email")
    void usersFile_line4_notAnEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("hello", "123456789"));
    }

    @Test
    @DisplayName("Users.txt line 5 invalid: '~' is not an allowed special char in password")
    void usersFile_line5_invalidSpecialChar() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("password@error.now", "~helloworld"));
    }

    @Test
    @DisplayName("Users.txt line 6 invalid: email is over 50 characters")
    void usersFile_line6_emailTooLong() {
        // "thisEmailIsToLong@wow0123456789wow0123456789wow0123456789.co.il" = 63 chars
        String longEmail = "thisEmailIsToLong@wow0123456789wow0123456789wow0123456789.co.il";
        assertTrue(longEmail.length() > 50);
        assertThrows(IllegalArgumentException.class,
                () -> new User(longEmail, "@1234Abcd"));
    }

    @Test
    @DisplayName("Users.txt line 8 invalid: password 'abc' is too short (only 3 chars)")
    void usersFile_line8_passwordTooShort() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("passwordTooShort@mail.ru", "123"));
    }

    @Test
    @DisplayName("Users.txt line 9 invalid: password '123456789ABcdefg' is too long (16 chars)")
    void usersFile_line9_passwordTooLong() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("passwordToolong@mail.ru", "123456789ABcdefg"));
    }

    // ─── Error message ordering (length checked before format) ────────────────

    @Test
    @DisplayName("51-char invalid email → 'too long', not 'valid email' (length checked first)")
    void username_lengthCheckedBeforeFormat() {
        // 51-char string that is NOT a valid email — should still say "too long"
        String notEmailButLong = "a".repeat(51);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new User(notEmailButLong, GOOD_PASS));
        assertTrue(ex.getMessage().toLowerCase().contains("too long"),
                "Expected 'too long' but got: " + ex.getMessage());
    }

    @Test
    @DisplayName("Password 7 chars with no special → 'too short', not 'invalid password'")
    void password_lengthCheckedBeforeContent() {
        // "abcdef1" = 7 chars, has letter+digit but no special
        // Should throw "too short", not "invalid password"
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new User(GOOD_EMAIL, "abcdef1"));
        assertTrue(ex.getMessage().toLowerCase().contains("too short"),
                "Expected 'too short' but got: " + ex.getMessage());
    }
}
