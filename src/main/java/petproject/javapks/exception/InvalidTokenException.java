package petproject.javapks.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String err) {
        super(err);
    }
}
