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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import petproject.javapks.dto.request.auth.LoginRequest;
import petproject.javapks.dto.response.jwt.JwtTokenResponse;
import petproject.javapks.security.jwt.JwtFilter;
import petproject.javapks.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                ServletWebSecurityAutoConfiguration.class
        },
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtFilter.class
                )
        }
)
public class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private AuthService authService;

        @MockitoBean
        private PasswordEncoder passwordEncoder;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Test
        void login_shouldReturnTokens_whenValidRequest() throws Exception {
                // given
                LoginRequest request = new LoginRequest("user@example.com", "secret123");
                when(authService.login(any(LoginRequest.class)))
                        .thenReturn(new JwtTokenResponse("access", "refresh"));

                // when / then
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.accessToken").value("access"))
                        .andExpect(jsonPath("$.refreshToken").value("refresh"));
        }

        @Test
        void login_shouldReturnBadRequest_whenBodyIsInvalid() throws Exception {
                // given — пустой объект: срабатывает bean-валидация
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(400))
                        .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        void login_shouldReturnBadRequest_whenBodyIsMissing() throws Exception {
                // given — тела нет вообще: раньше падало в handleGeneric → 500,
                // теперь HttpMessageNotReadableException маппится в 400
                mockMvc.perform(post("/api/v1/auth/login"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(400))
                        .andExpect(jsonPath("$.message").isNotEmpty());
        }
}
