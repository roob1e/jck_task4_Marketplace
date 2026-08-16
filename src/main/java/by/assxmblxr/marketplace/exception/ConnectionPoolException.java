package by.assxmblxr.marketplace.exception;

/**
 * Signals a failure of the {@link by.assxmblxr.marketplace.db.ConnectionPool} itself
 * (e.g. unable to create a new connection, pool misconfiguration), as opposed to
 * {@link DaoException}, which signals a failure of an individual query.
 */
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
