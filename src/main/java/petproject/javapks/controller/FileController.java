package petproject.javapks.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import petproject.javapks.dto.response.FileDto;
import petproject.javapks.dto.response.StoredFile;
import petproject.javapks.model.File;
import petproject.javapks.service.FileService;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resource/{uuid}/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileDto> upload(
            @PathVariable("uuid") UUID resourceUuid,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileService.upload(resourceUuid, file));
    }

    @GetMapping("/{fileUuid}")
    public ResponseEntity<InputStreamResource> download(
            @PathVariable("uuid") UUID resourceUuid,
            @PathVariable UUID fileUuid
    ) {
        StoredFile stored = fileService.download(resourceUuid, fileUuid);
        File fileMeta = fileService.getOwnedFile(resourceUuid, fileUuid); // см. ниже

        // ContentDisposition корректно кодирует не-ASCII имена (RFC 5987).
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(fileMeta.getName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType(stored.contentType()))
                .contentLength(stored.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new InputStreamResource(stored.content()));
    }

    @DeleteMapping("/{fileUuid}")
    public ResponseEntity<Void> delete(
            @PathVariable("uuid") UUID resourceUuid,
            @PathVariable UUID fileUuid
    ) {
        fileService.delete(resourceUuid, fileUuid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}