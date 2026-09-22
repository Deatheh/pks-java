package petproject.javapks.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String err) {
        super(err);
    }

    public ResourceNotFoundException(String err, Throwable cause) {
        super(err, cause);
    }
}
