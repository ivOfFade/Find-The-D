import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/** Salted SHA-256. Stored form is "salt:hash". Raw passwords are never kept. */
final class PasswordHasher {
    private static final SecureRandom RANDOM = new SecureRandom();
    private PasswordHasher() { }

    static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new InvalidInputException("Password must be at least 6 characters.");
        }
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String s = Base64.getEncoder().encodeToString(salt);
        return s + ":" + digest(s, rawPassword);
    }

    static boolean matches(String rawPassword, String stored) {
        if (rawPassword == null || stored == null || !stored.contains(":")) {
            return false;
        }
        String[] parts = stored.split(":", 2);
        return digest(parts[0], rawPassword).equals(parts[1]);
    }

    private static String digest(String salt, String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest((salt + raw).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
