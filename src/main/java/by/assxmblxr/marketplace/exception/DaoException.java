package by.assxmblxr.marketplace.exception;

/**
 * Signals an unexpected failure in the DAO layer, typically wrapping a
 * {@link java.sql.SQLException} raised by a JDBC call. Unlike {@link ServiceException},
 * this represents an infrastructure failure the caller cannot recover from, not an
 * expected business-rule violation.
 */
public class DaoException extends RuntimeException implements LocalizedException {
  private static final String MESSAGE_KEY = "error.database";

  public DaoException(String message) {
    super(message);
  }

  public DaoException(String message, Throwable cause) {
    super(message, cause);
  }

  public DaoException(Throwable cause) {
    super(cause);
  }

  public DaoException() {
    super();
  }

  @Override
  public String getMessageKey() {
    return MESSAGE_KEY;
  }
}
