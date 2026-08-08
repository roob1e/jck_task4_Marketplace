package by.assxmblxr.marketplace.exception;

public class ConnectionPoolException extends RuntimeException {
  public ConnectionPoolException(String message) {
    super(message);
  }

  public ConnectionPoolException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConnectionPoolException(Throwable cause) {
    super(cause);
  }

  public ConnectionPoolException() {}
}
