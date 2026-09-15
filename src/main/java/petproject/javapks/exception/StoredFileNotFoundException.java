package petproject.javapks.exception;

public class StoredFileNotFoundException extends RuntimeException {
    public StoredFileNotFoundException(String err) {
        super(err);
    }
}
