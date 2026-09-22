package petproject.javapks.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import petproject.javapks.service.ExportService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resource")
@RequiredArgsConstructor
public class ResourceController {

    private static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ExportService exportService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportResources() {
        byte[] body = exportService.exportResources();
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(XLSX_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resources.xlsx\"")
                .body(body);
    }

    @GetMapping("/{resourceId}/files/export")
    public ResponseEntity<byte[]> exportResourceFiles(
            @PathVariable UUID resourceId) {
        byte[] body = exportService.exportResourceFiles(resourceId);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(XLSX_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"files-" + resourceId + ".xlsx\"")
                .body(body);
    }
}
