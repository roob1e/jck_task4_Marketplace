package by.assxmblxr.marketplace.exception;

/**
 * Signals an expected business-rule violation in the service layer (e.g. a duplicate
 * login, invalid credentials), as opposed to an unexpected infrastructure failure
 * (see {@link DaoException}, {@link ConnectionPoolException}).
 * <p>
 * Carries a {@link #getMessageKey()} instead of a literal message: the caller resolves
 * it against a {@code ResourceBundle} to render a localized message, so no English text
 * is hardcoded here.
 */
public class ServiceException extends RuntimeException {
  private final String messageKey;

  /**
   * @param messageKey the resource bundle key identifying the localized message to show
   */
  public ServiceException(String messageKey) {
    super(messageKey);
    this.messageKey = messageKey;
  }

  /**
   * @param messageKey the resource bundle key identifying the localized message to show
   * @param cause      the underlying cause of this exception
   */
  public ServiceException(String messageKey, Throwable cause) {
    super(messageKey, cause);
    this.messageKey = messageKey;
  }

  /**
   * @return the resource bundle key identifying the localized message to show
   */
  public String getMessageKey() {
    return messageKey;
  }
}
