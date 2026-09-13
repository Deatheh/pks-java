package petproject.javapks.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record LoginRequest (
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    @Size(min = 5, max = 100, message = "Email must be between 5 and 100 characters")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 256, message = "Password must be between 6 and 256 characters")
    String password
) {}
