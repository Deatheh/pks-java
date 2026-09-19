package petproject.javapks.dto.response;

import java.util.UUID;

public record FileDto(
        UUID uuid,
        String name,
        String contentType,
        Long size
) {}
