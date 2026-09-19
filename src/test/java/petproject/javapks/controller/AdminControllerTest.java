package petproject.javapks.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import petproject.javapks.dto.request.admin.RegisterRequest;
import petproject.javapks.dto.request.admin.UpdateUserRequest;
import petproject.javapks.dto.response.UserDto;
import petproject.javapks.exception.EmailAlreadyExistsException;
import petproject.javapks.model.Role;
import petproject.javapks.security.jwt.JwtFilter;
import petproject.javapks.service.AdminService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AdminController.class, excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                ServletWebSecurityAutoConfiguration.class
}, excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class)
})
public class AdminControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private AdminService adminService;

        private final ObjectMapper objectMapper = new ObjectMapper();

        private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

        private UserDto buildUserDto() {
                return new UserDto(
                                USER_UUID,
                                "test@example.com",
                                Role.USER,
                                "Johnny",
                                "Doeman",
                                true,
                                LocalDateTime.of(2024, 1, 1, 12, 0),
                                LocalDateTime.of(2024, 1, 2, 12, 0));
        }

        // ===================== POST /api/v1/admin =====================

        @Test
        void createUser_shouldReturnCreatedUser_whenValidRequest() throws Exception {
                // given
                RegisterRequest request = new RegisterRequest(
                                "test@example.com",
                                "password123",
                                Role.USER,
                                "Johnny",
                                "Doeman");
                UserDto response = buildUserDto();

                when(adminService.createUser(any(RegisterRequest.class))).thenReturn(response);

                // when / then
                mockMvc.perform(post("/api/v1/admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.uuid").value(USER_UUID.toString()))
                                .andExpect(jsonPath("$.email").value("test@example.com"))
                                .andExpect(jsonPath("$.role").value("USER"))
                                .andExpect(jsonPath("$.firstName").value("Johnny"))
                                .andExpect(jsonPath("$.lastName").value("Doeman"))
                                .andExpect(jsonPath("$.enabled").value(true));
        }

        @Test
        void createUser_shouldReturnBadRequest_whenValidationFails() throws Exception {
                // given — email невалиден, пароль короткий, role = null, имена пустые
                RegisterRequest request = new RegisterRequest(
                                "bad",
                                "123",
                                null,
                                "",
                                "");

                // when / then — GlobalExceptionHandler вернёт ErrorResponse
                mockMvc.perform(post("/api/v1/admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        void createUser_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
                // given
                RegisterRequest request = new RegisterRequest(
                                "test@example.com",
                                "password123",
                                Role.USER,
                                "Johnny",
                                "Doeman");

                when(adminService.createUser(any(RegisterRequest.class)))
                                .thenThrow(new EmailAlreadyExistsException("Email already exists"));

                // when / then
                mockMvc.perform(post("/api/v1/admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status").value(409))
                                .andExpect(jsonPath("$.message").value("Email already exists"));
        }

        // ===================== GET /api/v1/admin/{uuid} =====================

        @Test
        void getUserById_shouldReturnUser_whenExists() throws Exception {
                // given
                UserDto response = buildUserDto();
                when(adminService.getUserByUUID(USER_UUID)).thenReturn(response);

                // when / then
                mockMvc.perform(get("/api/v1/admin/{uuid}", USER_UUID))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.uuid").value(USER_UUID.toString()))
                                .andExpect(jsonPath("$.email").value("test@example.com"))
                                .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        void getUserById_shouldReturnBadRequest_whenUuidIsInvalid() throws Exception {
                // when / then — невалидный UUID не биндится, летит
                // IllegalArgumentException/MethodArgumentTypeMismatch
                mockMvc.perform(get("/api/v1/admin/{uuid}", "not-a-uuid"))
                                .andExpect(status().isBadRequest());
        }

        // ===================== GET /api/v1/admin =====================

        @Test
        void getUsersByFilters_shouldReturnListOfUsers() throws Exception {
                // given
                List<UserDto> response = List.of(buildUserDto());
                when(adminService.getUsers(any(), any(), any(), any(), any())).thenReturn(response);

                // when / then
                mockMvc.perform(get("/api/v1/admin")
                                .param("email", "test")
                                .param("role", "USER")
                                .param("offset", "0")
                                .param("count", "20")
                                .param("sortBy", "createdAt")
                                .param("sortDir", "desc"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].uuid").value(USER_UUID.toString()))
                                .andExpect(jsonPath("$[0].email").value("test@example.com"))
                                .andExpect(jsonPath("$[0].role").value("USER"));
        }

        @Test
        void getUsersByFilters_shouldUseDefaultPaginationParams() throws Exception {
                // given — проверяем, что дефолты @RequestParam реально подставляются
                when(adminService.getUsers(any(), eq(0L), eq(20L), eq("createdAt"), eq("desc")))
                                .thenReturn(List.of());

                // when / then
                mockMvc.perform(get("/api/v1/admin"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(0));
        }

        // ===================== PUT /api/v1/admin/{uuid} =====================

        @Test
        void updateUser_shouldReturnUpdatedUser_whenValidRequest() throws Exception {
                // given
                UpdateUserRequest request = new UpdateUserRequest(
                                "updated@example.com",
                                false,
                                Role.ADMIN,
                                "Johnny",
                                "Doeman");
                UserDto response = new UserDto(
                                USER_UUID,
                                "updated@example.com",
                                Role.ADMIN,
                                "Johnny",
                                "Doeman",
                                false,
                                LocalDateTime.of(2024, 1, 1, 12, 0),
                                LocalDateTime.of(2024, 1, 2, 12, 0));

                when(adminService.updateUser(eq(USER_UUID), any(UpdateUserRequest.class))).thenReturn(response);

                // when / then
                mockMvc.perform(put("/api/v1/admin/{uuid}", USER_UUID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.uuid").value(USER_UUID.toString()))
                                .andExpect(jsonPath("$.email").value("updated@example.com"))
                                .andExpect(jsonPath("$.role").value("ADMIN"))
                                .andExpect(jsonPath("$.firstName").value("Johnny"))
                                .andExpect(jsonPath("$.enabled").value(false));
        }

        @Test
        void updateUser_shouldReturnBadRequest_whenValidationFails() throws Exception {
                // given — role = null, поля невалидны
                UpdateUserRequest request = new UpdateUserRequest(
                                "bad",
                                null,
                                null,
                                "",
                                "");

                // when / then
                mockMvc.perform(put("/api/v1/admin/{uuid}", USER_UUID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").isNotEmpty());
        }

        // ===================== DELETE /api/v1/admin/{uuid} =====================

        @Test
        void deleteUser_shouldReturnNoContent_whenExists() throws Exception {
                // given
                doNothing().when(adminService).deleteUser(USER_UUID);

                // when / then
                mockMvc.perform(delete("/api/v1/admin/{uuid}", USER_UUID))
                                .andExpect(status().isNoContent());
        }

        @Test
        void deleteUser_shouldPropagateException_whenServiceThrows() throws Exception {
                // given — сервис кидает EmailAlreadyExistsException (как пример),
                // чтобы проверить, что GlobalExceptionHandler отрабатывает и в delete
                doThrow(new EmailAlreadyExistsException("boom"))
                                .when(adminService).deleteUser(USER_UUID);

                // when / then
                mockMvc.perform(delete("/api/v1/admin/{uuid}", USER_UUID))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status").value(409))
                                .andExpect(jsonPath("$.message").value("boom"));
        }
}