package petproject.javapks.dto.response;

import java.io.IOException;
import java.io.InputStream;

public record StoredFile(
        InputStream content,
        String contentType,
        long size) implements AutoCloseable {

    @Override
    public void close() throws IOException {
        if (content != null) {
            content.close();
        }
    }
}
