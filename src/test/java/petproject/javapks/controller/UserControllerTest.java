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
import petproject.javapks.dto.export.UserExportRow;
import petproject.javapks.security.jwt.JwtFilter;
import petproject.javapks.service.ExportService;
import petproject.javapks.service.UserService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

        private static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private ExportService exportService;

        // ===================== GET /api/v1/user/export =====================

        @Test
        void exportUsers_shouldReturnXlsx_whenRequested() throws Exception {
                // given — пароля в выгрузке быть не должно
                UserExportRow row = new UserExportRow(
                                "11111111-1111-1111-1111-111111111111",
                                "test@example.com",
                                "USER",
                                "Johnny",
                                "Doeman",
                                true,
                                LocalDateTime.of(2024, 1, 1, 12, 0, 0),
                                LocalDateTime.of(2024, 1, 2, 12, 0, 0));
                when(exportService.exportUsers()).thenReturn(writeRows(row));

                // when / then
                MvcResult result = mockMvc.perform(get("/api/v1/user/export"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(XLSX_MEDIA_TYPE))
                                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"users.xlsx\""))
                                .andReturn();

                List<Map<Integer, String>> rows = readRows(result.getResponse().getContentAsByteArray());
                assertEquals(List.of("UUID", "Email", "Роль", "Имя", "Фамилия", "Активен", "Создан", "Обновлён"),
                                List.copyOf(rows.get(0).values()));
                assertEquals("11111111-1111-1111-1111-111111111111", rows.get(1).get(0));
                assertEquals("test@example.com", rows.get(1).get(1));
                assertEquals("USER", rows.get(1).get(2));
                assertEquals("Johnny", rows.get(1).get(3));
                assertEquals("Doeman", rows.get(1).get(4));
                assertEquals("true", rows.get(1).get(5));
                assertEquals("2024-01-01 12:00:00", rows.get(1).get(6));
                assertEquals("2024-01-02 12:00:00", rows.get(1).get(7));
        }

        // ===================== helpers =====================

        private static byte[] writeRows(UserExportRow... rows) throws Exception {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                FastExcel.write(baos, UserExportRow.class).sheet("users").doWrite(List.of(rows));
                return baos.toByteArray();
        }

        private static List<Map<Integer, String>> readRows(byte[] bytes) {
                return FastExcel.read(new ByteArrayInputStream(bytes))
                                .headRowNumber(0)
                                .sheet()
                                .doReadSync();
        }
}
