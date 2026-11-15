package petproject.javapks.dto.response;

import java.util.List;

public record ImportResultDto(
        int totalRows,
        int validRows,
        boolean dryRun,
        boolean imported,
        List<RowError> errors
) {
    public record RowError(int row, String field, String message) {}
}