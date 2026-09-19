package petproject.javapks.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

        private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

        private HttpServletRequest request(String uri) {
                HttpServletRequest request = mock(HttpServletRequest.class);
                when(request.getRequestURI()).thenReturn(uri);
                return request;
        }

        @Test
        void handleStoredFileNotFoundReturns404() {
                StoredFileNotFoundException ex = new StoredFileNotFoundException("File not found: 123");

                ResponseEntity<ErrorResponse> response = handler.handleStoredFileNotFound(ex, request("/api/v1/file/123"));

                assertEquals(404, response.getStatusCode().value());
                assertEquals("File not found: 123", response.getBody().message());
                assertEquals(404, response.getBody().status());
        }

        @Test
        void handleStorageReturns500() {
                StorageException ex = new StorageException("Failed to delete file");

                ResponseEntity<ErrorResponse> response = handler.handleStorage(ex, request("/api/v1/file/123"));

                assertEquals(500, response.getStatusCode().value());
                assertEquals("Failed to delete file", response.getBody().message());
                assertEquals(500, response.getBody().status());
        }
}
