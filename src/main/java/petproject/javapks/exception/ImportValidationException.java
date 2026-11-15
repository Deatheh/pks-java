package petproject.javapks.exception;

import lombok.Getter;
import petproject.javapks.dto.response.ImportResultDto;

@Getter
public class ImportValidationException extends RuntimeException {
    private final ImportResultDto report;

    public ImportValidationException(ImportResultDto report) {
        super("Import failed: " + report.errors().size() + " invalid row(s)");
        this.report = report;
    }
}
