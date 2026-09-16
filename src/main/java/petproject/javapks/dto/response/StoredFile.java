package petproject.javapks.dto.response;

import java.io.InputStream;

public record StoredFile(
        InputStream content,
        String contentType,
        long size
) {
}