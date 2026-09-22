package petproject.javapks.exception;

public record ErrorResponse(
        String message,
        int status,
        long timestamp) {
    public ErrorResponse(String message, int status) {
        this(message, status, System.currentTimeMillis());
    }
}
