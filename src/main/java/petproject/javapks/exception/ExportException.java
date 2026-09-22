package petproject.javapks.exception;

public class ExportException extends RuntimeException {
    public ExportException(String err) {
        super(err);
    }

    public ExportException(String err, Throwable cause) {
        super(err, cause);
    }
}
