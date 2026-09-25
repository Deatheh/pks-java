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
import petproject.javapks.dto.response.ResourceDto;
import petproject.javapks.exception.ResourceNotFoundException;
import petproject.javapks.security.jwt.JwtFilter;
import petproject.javapks.service.ResourceService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ResourceController.class, excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                ServletWebSecurityAutoConfiguration.class
}, excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class)
})
public class ResourceControllerTest {

        private static final UUID RESOURCE_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ResourceService resourceService;

        // ===================== GET /api/v1/resource/{uuid} =====================

        @Test
        void getResourceByUuid_shouldReturnResource_whenExists() throws Exception {
                // given
                ResourceDto response = new ResourceDto(
                                RESOURCE_UUID,
                                "Отчёт",
                                "Годовой отчёт",
                                LocalDateTime.of(2024, 3, 1, 10, 30, 0),
                                LocalDateTime.of(2024, 3, 2, 10, 30, 0),
                                List.of());
                when(resourceService.getResourceByUuid(RESOURCE_UUID)).thenReturn(response);

                // when / then
                mockMvc.perform(get("/api/v1/resource/{uuid}", RESOURCE_UUID))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.uuid").value(RESOURCE_UUID.toString()))
                                .andExpect(jsonPath("$.title").value("Отчёт"))
                                .andExpect(jsonPath("$.description").value("Годовой отчёт"));
        }

        @Test
        void getResourceByUuid_shouldReturnNotFound_whenMissing() throws Exception {
                // given
                when(resourceService.getResourceByUuid(RESOURCE_UUID))
                                .thenThrow(new ResourceNotFoundException("Resource not found by uuid"));

                // when / then — GlobalExceptionHandler вернёт ErrorResponse
                mockMvc.perform(get("/api/v1/resource/{uuid}", RESOURCE_UUID))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.message").isNotEmpty());
        }
}
