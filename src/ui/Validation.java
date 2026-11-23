import java.util.regex.Pattern;

/** Shared input rules so every domain class validates the same way. */
final class Validation {
    private static final Pattern PH_MOBILE = Pattern.compile("^(09\\d{9}|\\+639\\d{9})$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private Validation() { }

    static String required(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(field + " is required.");
        }
        return value.trim();
    }

    static String message(String value) {
        String m = required(value, "Message");
        if (m.length() > 1000) {
            throw new InvalidInputException("Message must be 1000 characters or fewer.");
        }
        return m;
    }

    static int age(int age) {
        if (age < 16 || age > 120) {
            throw new InvalidInputException("Age must be between 16 and 120.");
        }
        return age;
    }

    static String contactNum(String v) {
        String c = required(v, "Contact number");
        if (!PH_MOBILE.matcher(c).matches()) {
            throw new InvalidInputException("Contact number must be 09XXXXXXXXX or +639XXXXXXXXX.");
        }
        return c;
    }

    static String email(String v) {
        String e = required(v, "Email");
        if (!EMAIL.matcher(e).matches()) {
            throw new InvalidInputException("Email format is invalid.");
        }
        return e.toLowerCase();
    }
}
