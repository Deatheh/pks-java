package petproject.javapks.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String err) {
        super(err);
    }
}
