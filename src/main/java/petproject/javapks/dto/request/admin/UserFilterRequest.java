package petproject.javapks.dto.request.admin;

import petproject.javapks.model.Role;

import java.time.LocalDateTime;

public record UserFilterRequest(
                String email,
                Role role,
                String firstName,
                String lastName,
                Boolean enabled,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
}
