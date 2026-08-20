package by.assxmblxr.marketplace.exception;

/**
 * Signals an unexpected failure in a {@link by.assxmblxr.marketplace.storage.FileStorage}
 * implementation (e.g. a MinIO/S3 call failing), analogous to {@link DaoException} for
 * the database layer.
 */
public class FileStorageException extends RuntimeException implements LocalizedException {
  private static final String MESSAGE_KEY = "error.file_storage";

  public FileStorageException(String message) {
    super(message);
  }

  public FileStorageException(String message, Throwable cause) {
    super(message, cause);
  }

  public FileStorageException(Throwable cause) {
    super(cause);
  }

  public FileStorageException() {}

  @Override
  public String getMessageKey() {
    return MESSAGE_KEY;
  }
}
