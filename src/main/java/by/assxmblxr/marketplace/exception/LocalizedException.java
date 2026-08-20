package by.assxmblxr.marketplace.exception;

/**
 * Common contract for exceptions that carry a resource bundle key identifying a
 * localized message to show the user, instead of a hardcoded literal message.
 * Lets a single catch-all error handler resolve a displayable message for any of
 * this project's exception types without knowing about each one individually.
 */
public interface LocalizedException {

  /**
   * @return the resource bundle key identifying the localized message to show
   */
  String getMessageKey();
}
