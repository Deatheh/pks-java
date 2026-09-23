package petproject.javapks.dto.request.resource;

import java.time.LocalDateTime;

public record ResourceFilterRequest(
        String title,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
