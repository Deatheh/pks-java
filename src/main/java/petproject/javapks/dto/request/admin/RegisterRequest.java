package petproject.javapks.dto.request.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import petproject.javapks.model.Role;

public record RegisterRequest(
                @Email(message = "Email must be valid") @NotBlank(message = "Email is required") @Size(min = 5, max = 100, message = "Email must be between 5 and 100 characters") String email,

                @NotBlank(message = "Password is required") @Size(min = 6, max = 256, message = "Password must be between 6 and 256 characters") String password,

                @NotNull(message = "Role is required") Role role,

                @NotBlank(message = "Firstname is required") @Size(min = 5, max = 100, message = "Firstname must be between 5 and 100 characters") String firstname,

                @NotBlank(message = "Lastname is required") @Size(min = 5, max = 100, message = "Lastname must be between 5 and 100 characters") String lastname) {
}
