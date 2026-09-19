package petproject.javapks.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import petproject.javapks.dto.request.admin.RegisterRequest;
import petproject.javapks.dto.request.admin.UpdateUserRequest;
import petproject.javapks.dto.request.admin.UserFilterRequest;
import petproject.javapks.dto.response.UserDto;
import petproject.javapks.service.AdminService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(
            @Valid @RequestBody RegisterRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(dto));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable UUID uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.getUserByUUID(uuid));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsersByFilters(
            @ModelAttribute UserFilterRequest filter,
            @RequestParam(defaultValue = "0") Long offset,
            @RequestParam(defaultValue = "20") Long count,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.getUsers(
                filter,
                offset,
                count,
                sortBy,
                sortDir));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateUserRequest dto) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.updateUser(uuid, dto));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID uuid) {
        adminService.deleteUser(uuid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
