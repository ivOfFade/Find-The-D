/** Base class of every business-rule error in the app (unchecked so the UI decides where to catch). */
public class DormAppException extends RuntimeException {
    public DormAppException(String message) { super(message); }
}
