package petproject.javapks.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import petproject.javapks.dto.request.admin.RegisterRequest;
import petproject.javapks.dto.request.admin.UpdateUserRequest;
import petproject.javapks.dto.request.admin.UserFilterRequest;
import petproject.javapks.dto.response.UserDto;
import petproject.javapks.service.AdminService;
import petproject.javapks.service.ExportService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private final AdminService adminService;
    private final ExportService exportService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(
            @Valid @RequestBody RegisterRequest dto
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(dto));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable UUID uuid
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.getUserByUUID(uuid));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsersByFilters(
            @ModelAttribute UserFilterRequest filter,
            @RequestParam(defaultValue = "0") Long offset,
            @RequestParam(defaultValue = "20") Long count,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.getUsers(
                filter,
                offset,
                count,
                sortBy,
                sortDir
        ));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateUserRequest dto
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.updateUser(uuid, dto));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID uuid
    ){
        adminService.deleteUser(uuid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/users/export")
    public ResponseEntity<byte[]> exportUsers() {
        byte[] body = exportService.exportUsers();
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(XLSX_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users.xlsx\"")
                .body(body);
    }

    @GetMapping("/resource/export")
    public ResponseEntity<byte[]> exportResources() {
        byte[] body = exportService.exportResources();
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(XLSX_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resources.xlsx\"")
                .body(body);
    }

    @GetMapping("/resource/{resourceId}/files/export")
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
