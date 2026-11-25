package exception;

public class DormAppException extends RuntimeException {

  public DormAppException(String message) {
    super(message);
  }

  public DormAppException(String message, Throwable cause) {
    super(message, cause);
  }
}
