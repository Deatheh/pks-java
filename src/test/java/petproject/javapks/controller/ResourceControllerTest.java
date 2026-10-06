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
import petproject.javapks.dto.request.resource.CreateResourceRequest;
import petproject.javapks.dto.request.resource.UpdateResourceRequest;
import petproject.javapks.dto.response.FileDto;
import petproject.javapks.dto.response.ResourceDto;
import petproject.javapks.exception.ResourceNotFoundException;
import petproject.javapks.security.jwt.JwtFilter;
import petproject.javapks.service.ResourceService;

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

@WebMvcTest(value = ResourceController.class,
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
public class ResourceControllerTest {

        private static final UUID RESOURCE_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
        private static final UUID FILE_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ResourceService resourceService;

        private final ObjectMapper objectMapper = new ObjectMapper();

        private ResourceDto buildResourceDto() {
                return buildResourceDto(RESOURCE_UUID, "Отчёт", "Годовой отчёт");
        }

        private ResourceDto buildResourceDto(UUID uuid, String title, String description) {
                return new ResourceDto(
                        uuid,
                        title,
                        description,
                        LocalDateTime.of(2024, 3, 1, 10, 30),
                        LocalDateTime.of(2024, 3, 2, 10, 30),
                        List.of(new FileDto(FILE_UUID, "report.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 1024L))
                );
        }

        // ===================== POST /api/v1/resource =====================

        @Test
        void createResource_shouldReturnCreatedResource_whenValidRequest() throws Exception {
                // given
                CreateResourceRequest request = new CreateResourceRequest("Отчёт", "Годовой отчёт");
                ResourceDto response = buildResourceDto();

                when(resourceService.createResource(any(CreateResourceRequest.class))).thenReturn(response);

                // when / then
                mockMvc.perform(post("/api/v1/resource")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.uuid").value(RESOURCE_UUID.toString()))
                        .andExpect(jsonPath("$.title").value("Отчёт"))
                        .andExpect(jsonPath("$.description").value("Годовой отчёт"))
                        .andExpect(jsonPath("$.files.length()").value(1))
                        .andExpect(jsonPath("$.files[0].uuid").value(FILE_UUID.toString()))
                        .andExpect(jsonPath("$.files[0].name").value("report.xlsx"));
        }

        @Test
        void createResource_shouldReturnBadRequest_whenTitleIsTooShort() throws Exception {
                // given — title короче 3 символов
                CreateResourceRequest request = new CreateResourceRequest("ab", "Valid description");

                // when / then — GlobalExceptionHandler вернёт ErrorResponse
                mockMvc.perform(post("/api/v1/resource")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(400))
                        .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        void createResource_shouldReturnBadRequest_whenTitleIsBlank() throws Exception {
                // given
                CreateResourceRequest request = new CreateResourceRequest("", "Valid description");

                // when / then
                mockMvc.perform(post("/api/v1/resource")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(400));
        }

        // ===================== GET /api/v1/resource/{uuid} =====================

        @Test
        void getResourceByUUID_shouldReturnResource_whenExists() throws Exception {
                // given
                when(resourceService.getResourceByUuid(RESOURCE_UUID)).thenReturn(buildResourceDto());

                // when / then
                mockMvc.perform(get("/api/v1/resource/{uuid}", RESOURCE_UUID))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.uuid").value(RESOURCE_UUID.toString()))
                        .andExpect(jsonPath("$.title").value("Отчёт"))
                        .andExpect(jsonPath("$.description").value("Годовой отчёт"));
        }

        @Test
        void getResourceByUUID_shouldReturnNotFound_whenMissing() throws Exception {
                // given
                when(resourceService.getResourceByUuid(any(UUID.class)))
                        .thenThrow(new ResourceNotFoundException("Resource not found by uuid"));

                // when / then
                mockMvc.perform(get("/api/v1/resource/{uuid}", RESOURCE_UUID))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(404))
                        .andExpect(jsonPath("$.message").value("Resource not found by uuid"));
        }

        @Test
        void getResourceByUUID_shouldReturnBadRequest_whenUuidIsInvalid() throws Exception {
                // when / then — невалидный UUID → MethodArgumentTypeMismatchException → 400
                mockMvc.perform(get("/api/v1/resource/{uuid}", "not-a-uuid"))
                        .andExpect(status().isBadRequest());
        }

        // ===================== GET /api/v1/resource =====================

        @Test
        void getResourcesByFilters_shouldReturnListOfResources() throws Exception {
                // given
                List<ResourceDto> response = List.of(buildResourceDto());
                when(resourceService.getResources(any(), any(), any(), any(), any())).thenReturn(response);

                // when / then
                mockMvc.perform(get("/api/v1/resource")
                                .param("title", "Отчёт")
                                .param("offset", "0")
                                .param("count", "20")
                                .param("sortBy", "createdAt")
                                .param("sortDir", "desc"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(1))
                        .andExpect(jsonPath("$[0].uuid").value(RESOURCE_UUID.toString()))
                        .andExpect(jsonPath("$[0].title").value("Отчёт"));
        }

        @Test
        void getResourcesByFilters_shouldUseDefaultPaginationParams() throws Exception {
                // given — проверяем, что дефолты @RequestParam реально подставляются
                when(resourceService.getResources(any(), eq(0L), eq(20L), eq("createdAt"), eq("desc")))
                        .thenReturn(List.of());

                // when / then
                mockMvc.perform(get("/api/v1/resource"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void getResourcesByFilters_shouldReturnEmptyList_whenNothingMatches() throws Exception {
                // given
                when(resourceService.getResources(any(), any(), any(), any(), any()))
                        .thenReturn(List.of());

                // when / then
                mockMvc.perform(get("/api/v1/resource")
                                .param("title", "nonexistent"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(0));
        }

        // ===================== PUT /api/v1/resource/{uuid} =====================

        @Test
        void updateResource_shouldReturnUpdatedResource_whenValidRequest() throws Exception {
                // given
                UpdateResourceRequest request = new UpdateResourceRequest("Новый отчёт", "Обновлённое описание");
                ResourceDto response = buildResourceDto(RESOURCE_UUID, "Новый отчёт", "Обновлённое описание");

                when(resourceService.updateResource(eq(RESOURCE_UUID), any(UpdateResourceRequest.class)))
                        .thenReturn(response);

                // when / then
                mockMvc.perform(put("/api/v1/resource/{uuid}", RESOURCE_UUID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.uuid").value(RESOURCE_UUID.toString()))
                        .andExpect(jsonPath("$.title").value("Новый отчёт"))
                        .andExpect(jsonPath("$.description").value("Обновлённое описание"));
        }

        @Test
        void updateResource_shouldReturnBadRequest_whenValidationFails() throws Exception {
                // given — title короче 3 символов
                UpdateResourceRequest request = new UpdateResourceRequest("ab", "Valid description");

                // when / then
                mockMvc.perform(put("/api/v1/resource/{uuid}", RESOURCE_UUID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(400))
                        .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        void updateResource_shouldReturnNotFound_whenMissing() throws Exception {
                // given
                UpdateResourceRequest request = new UpdateResourceRequest("Новый отчёт", "Описание");
                when(resourceService.updateResource(eq(RESOURCE_UUID), any(UpdateResourceRequest.class)))
                        .thenThrow(new ResourceNotFoundException("Resource not found by uuid"));

                // when / then
                mockMvc.perform(put("/api/v1/resource/{uuid}", RESOURCE_UUID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(404));
        }

        // ===================== DELETE /api/v1/resource/{uuid} =====================

        @Test
        void deleteResource_shouldReturnNoContent_whenExists() throws Exception {
                // given
                doNothing().when(resourceService).deleteResource(RESOURCE_UUID);

                // when / then
                mockMvc.perform(delete("/api/v1/resource/{uuid}", RESOURCE_UUID))
                        .andExpect(status().isNoContent());
        }

        @Test
        void deleteResource_shouldReturnNotFound_whenMissing() throws Exception {
                // given
                doThrow(new ResourceNotFoundException("Resource not found by uuid"))
                        .when(resourceService).deleteResource(RESOURCE_UUID);

                // when / then
                mockMvc.perform(delete("/api/v1/resource/{uuid}", RESOURCE_UUID))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(404))
                        .andExpect(jsonPath("$.message").value("Resource not found by uuid"));
        }
}