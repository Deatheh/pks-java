package petproject.javapks.dto.response;

import petproject.javapks.model.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto(
        UUID uuid,
        String email,
        Role role,
        String firstname,
        String lastname,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
