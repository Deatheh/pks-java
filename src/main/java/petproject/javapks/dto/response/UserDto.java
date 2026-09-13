package petproject.javapks.dto.response;

import petproject.javapks.model.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto(
        UUID uuid,
        String email,
        String firstName,
        String lastName,
        Role role,
        Boolean enabled,
        LocalDateTime createdAt
) {
}
