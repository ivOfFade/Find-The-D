package exception;

public class ServiceOperationException extends DormAppException {

  public ServiceOperationException(String message, Throwable cause) {
    super(message, cause);
  }
}
