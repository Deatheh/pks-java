package petproject.javapks.exception;

public class StorageException extends RuntimeException {
    public StorageException(String err) {
        super(err);
    }

    public StorageException(String err, Throwable cause) {
        super(err, cause);
    }
}
