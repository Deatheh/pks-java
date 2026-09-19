package petproject.javapks.dto.response;

import petproject.javapks.model.File;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ResourceDto(
        UUID uuid,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<FileDto> files
) {}
