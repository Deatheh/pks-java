package petproject.javapks.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import petproject.javapks.dto.response.UserDto;
import petproject.javapks.model.Role;
import petproject.javapks.security.jwt.JwtFilter;
import petproject.javapks.service.UserService;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class, excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                ServletWebSecurityAutoConfiguration.class
}, excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class)
})
public class UserControllerTest {

        private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private UserService userService;

        // ===================== GET /api/v1/user/me =====================

        @Test
        void infoMe_shouldReturnCurrentUser_whenTokenPresent() throws Exception {
                // given
                UserDto response = new UserDto(
                                USER_UUID,
                                "test@example.com",
                                Role.USER,
                                "Johnny",
                                "Doeman",
                                true,
                                LocalDateTime.of(2024, 1, 1, 12, 0, 0),
                                LocalDateTime.of(2024, 1, 2, 12, 0, 0));
                when(userService.getInfoAboutMe("token")).thenReturn(response);

                // when / then
                mockMvc.perform(get("/api/v1/user/me")
                                .header("Authorization", "Bearer token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.uuid").value(USER_UUID.toString()))
                                .andExpect(jsonPath("$.email").value("test@example.com"))
                                .andExpect(jsonPath("$.role").value("USER"));
        }
}
