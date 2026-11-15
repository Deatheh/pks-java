package petproject.javapks.dto.response;

public record ExportedFileDto(
        byte[] bytes,
        String contentType,
        String filename
) {}