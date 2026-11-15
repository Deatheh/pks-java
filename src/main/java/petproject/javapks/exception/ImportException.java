package petproject.javapks.exception;

public class ImportException extends RuntimeException {
    public ImportException(String err) {
        super(err);
    }

    public ImportException(String err, Throwable cause) {
        super(err, cause);
    }
}