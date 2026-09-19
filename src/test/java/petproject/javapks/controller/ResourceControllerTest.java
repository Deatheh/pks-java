package petproject.javapks.controller;

import cn.idev.excel.FastExcel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import petproject.javapks.dto.export.FileExportRow;
import petproject.javapks.dto.export.ResourceExportRow;
import petproject.javapks.exception.ExportException;
import petproject.javapks.exception.ResourceNotFoundException;
import petproject.javapks.security.jwt.JwtFilter;
import petproject.javapks.service.ExportService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

        private static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        private static final UUID RESOURCE_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ExportService exportService;

        // ===================== GET /api/v1/resource/export =====================

        @Test
        void exportResources_shouldReturnXlsx_whenRequested() throws Exception {
                // given
                ResourceExportRow row = new ResourceExportRow(
                                RESOURCE_UUID.toString(),
                                "Отчёт",
                                "Годовой отчёт",
                                LocalDateTime.of(2024, 3, 1, 10, 30, 0),
                                LocalDateTime.of(2024, 3, 2, 10, 30, 0));
                when(exportService.exportResources()).thenReturn(writeRows(ResourceExportRow.class, row));

                // when / then
                MvcResult result = mockMvc.perform(get("/api/v1/resource/export"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(XLSX_MEDIA_TYPE))
                                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"resources.xlsx\""))
                                .andReturn();

                List<Map<Integer, String>> rows = readRows(result.getResponse().getContentAsByteArray());
                assertEquals(List.of("UUID", "Название", "Описание", "Создан", "Обновлён"),
                                List.copyOf(rows.get(0).values()));
                assertEquals(RESOURCE_UUID.toString(), rows.get(1).get(0));
                assertEquals("Отчёт", rows.get(1).get(1));
                assertEquals("Годовой отчёт", rows.get(1).get(2));
                assertEquals("2024-03-01 10:30:00", rows.get(1).get(3));
                assertEquals("2024-03-02 10:30:00", rows.get(1).get(4));
        }

        @Test
        void exportResources_shouldReturnInternalError_whenExportFails() throws Exception {
                // given
                when(exportService.exportResources())
                                .thenThrow(new ExportException("Failed to export data to XLSX"));

                // when / then — GlobalExceptionHandler вернёт ErrorResponse
                mockMvc.perform(get("/api/v1/resource/export"))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.status").value(500))
                                .andExpect(jsonPath("$.message").isNotEmpty());
        }

        // ===================== GET /api/v1/resource/{resourceId}/files/export =====================

        @Test
        void exportResourceFiles_shouldReturnXlsx_whenRequested() throws Exception {
                // given
                FileExportRow row = new FileExportRow(
                                "33333333-3333-3333-3333-333333333333",
                                "report.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                1024L);
                when(exportService.exportResourceFiles(RESOURCE_UUID))
                                .thenReturn(writeRows(FileExportRow.class, row));

                // when / then
                MvcResult result = mockMvc.perform(
                                get("/api/v1/resource/{resourceId}/files/export", RESOURCE_UUID))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(XLSX_MEDIA_TYPE))
                                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"files-" + RESOURCE_UUID + ".xlsx\""))
                                .andReturn();

                List<Map<Integer, String>> rows = readRows(result.getResponse().getContentAsByteArray());
                assertEquals(List.of("UUID", "Имя файла", "Тип содержимого", "Размер"),
                                List.copyOf(rows.get(0).values()));
                assertEquals("33333333-3333-3333-3333-333333333333", rows.get(1).get(0));
                assertEquals("report.xlsx", rows.get(1).get(1));
                assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                rows.get(1).get(2));
                assertEquals("1024", rows.get(1).get(3));
        }

        @Test
        void exportResourceFiles_shouldReturnNotFound_whenResourceMissing() throws Exception {
                // given
                when(exportService.exportResourceFiles(any(UUID.class)))
                                .thenThrow(new ResourceNotFoundException("Resource not found by uuid"));

                // when / then — GlobalExceptionHandler вернёт ErrorResponse
                mockMvc.perform(get("/api/v1/resource/{resourceId}/files/export", RESOURCE_UUID))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        void exportResourceFiles_shouldReturnBadRequest_whenInvalidUuid() throws Exception {
                // when / then — невалидный UUID в пути → MethodArgumentTypeMismatchException → 400
                mockMvc.perform(get("/api/v1/resource/{resourceId}/files/export", "not-a-uuid"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").isNotEmpty());
        }

        // ===================== helpers =====================

        private static <T> byte[] writeRows(Class<T> rowClass, T... rows) throws Exception {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                FastExcel.write(baos, rowClass).sheet("sheet").doWrite(List.of(rows));
                return baos.toByteArray();
        }

        private static List<Map<Integer, String>> readRows(byte[] bytes) {
                return FastExcel.read(new ByteArrayInputStream(bytes))
                                .headRowNumber(0)
                                .sheet()
                                .doReadSync();
        }
}
