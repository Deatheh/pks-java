package petproject.javapks.service;

import cn.idev.excel.FastExcel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import petproject.javapks.dto.export.FileExportRow;
import petproject.javapks.dto.export.ResourceExportRow;
import petproject.javapks.dto.export.UserExportRow;
import petproject.javapks.exception.ExportException;
import petproject.javapks.exception.ResourceNotFoundException;
import petproject.javapks.model.File;
import petproject.javapks.model.Resource;
import petproject.javapks.model.Role;
import petproject.javapks.model.User;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

        private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        private static final UUID RESOURCE_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
        private static final UUID FILE_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");

        @Mock
        private UserService userService;

        @Mock
        private ResourceService resourceService;

        @Mock
        private FileService fileService;

        @InjectMocks
        private ExportService exportService;

        // ===================== users =====================

        @Test
        void exportUsersReturnsValidXlsxWithRows() {
                LocalDateTime created = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
                LocalDateTime updated = LocalDateTime.of(2024, 1, 2, 12, 0, 0);
                User user = new User();
                user.setUuid(USER_UUID);
                user.setEmail("test@example.com");
                user.setPassword("secret");
                user.setRole(Role.USER);
                user.setEnabled(true);
                user.setFirstName("Johnny");
                user.setLastName("Doeman");
                user.setCreatedAt(created);
                user.setUpdatedAt(updated);
                when(userService.getAll()).thenReturn(List.of(user));

                byte[] bytes = exportService.exportUsers();

                List<UserExportRow> rows = FastExcel.read(new ByteArrayInputStream(bytes), UserExportRow.class, null)
                                .sheet()
                                .doReadSync();
                assertEquals(1, rows.size());
                UserExportRow row = rows.get(0);
                assertEquals(USER_UUID.toString(), row.getUuid());
                assertEquals("test@example.com", row.getEmail());
                assertEquals("USER", row.getRole());
                assertEquals("Johnny", row.getFirstName());
                assertEquals("Doeman", row.getLastName());
                assertEquals(true, row.getEnabled());
                assertEquals(created, row.getCreatedAt());
                assertEquals(updated, row.getUpdatedAt());
        }

        @Test
        void exportUsersWithEmptyDatasetReturnsHeaderOnlySheet() {
                when(userService.getAll()).thenReturn(List.of());

                byte[] bytes = exportService.exportUsers();

                List<UserExportRow> rows = FastExcel.read(new ByteArrayInputStream(bytes), UserExportRow.class, null)
                                .sheet()
                                .doReadSync();
                assertTrue(rows.isEmpty());
        }

        @Test
        void exportUsersWrapsDataAccessFailure() {
                when(userService.getAll())
                                .thenThrow(new DataAccessResourceFailureException("db is down"));

                ExportException ex = assertThrows(ExportException.class, exportService::exportUsers);
                assertEquals("Failed to export data to XLSX", ex.getMessage());
        }

        // ===================== resources =====================

        @Test
        void exportResourcesReturnsValidXlsxWithRows() {
                LocalDateTime created = LocalDateTime.of(2024, 3, 1, 10, 30, 0);
                LocalDateTime updated = LocalDateTime.of(2024, 3, 2, 10, 30, 0);
                Resource resource = new Resource();
                resource.setUuid(RESOURCE_UUID);
                resource.setTitle("Отчёт");
                resource.setDescription("Годовой отчёт");
                resource.setCreatedAt(created);
                resource.setUpdatedAt(updated);
                when(resourceService.getAll()).thenReturn(List.of(resource));

                byte[] bytes = exportService.exportResources();

                List<ResourceExportRow> rows = FastExcel.read(new ByteArrayInputStream(bytes),
                                ResourceExportRow.class, null)
                                .sheet()
                                .doReadSync();
                assertEquals(1, rows.size());
                ResourceExportRow row = rows.get(0);
                assertEquals(RESOURCE_UUID.toString(), row.getUuid());
                assertEquals("Отчёт", row.getTitle());
                assertEquals("Годовой отчёт", row.getDescription());
                assertEquals(created, row.getCreatedAt());
                assertEquals(updated, row.getUpdatedAt());
        }

        @Test
        void exportResourcesWithEmptyDatasetReturnsHeaderOnlySheet() {
                when(resourceService.getAll()).thenReturn(List.of());

                byte[] bytes = exportService.exportResources();

                List<ResourceExportRow> rows = FastExcel.read(new ByteArrayInputStream(bytes),
                                ResourceExportRow.class, null)
                                .sheet()
                                .doReadSync();
                assertTrue(rows.isEmpty());
        }

        // ===================== resource files =====================

        @Test
        void exportResourceFilesReturnsValidXlsxWithRows() {
                Resource resource = new Resource();
                resource.setUuid(RESOURCE_UUID);
                when(resourceService.getByUuid(RESOURCE_UUID)).thenReturn(resource);
                File file = new File();
                file.setUuid(FILE_UUID);
                file.setName("report.xlsx");
                file.setContentType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                file.setSize(1024L);
                file.setResource(resource);
                when(fileService.getAllByResourceUuid(RESOURCE_UUID)).thenReturn(List.of(file));

                byte[] bytes = exportService.exportResourceFiles(RESOURCE_UUID);

                List<FileExportRow> rows = FastExcel.read(new ByteArrayInputStream(bytes), FileExportRow.class, null)
                                .sheet()
                                .doReadSync();
                assertEquals(1, rows.size());
                FileExportRow row = rows.get(0);
                assertEquals(FILE_UUID.toString(), row.getUuid());
                assertEquals("report.xlsx", row.getName());
                assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                row.getContentType());
                assertEquals(1024L, row.getSize());
        }

        @Test
        void exportResourceFilesWithEmptyDatasetReturnsHeaderOnlySheet() {
                Resource resource = new Resource();
                resource.setUuid(RESOURCE_UUID);
                when(resourceService.getByUuid(RESOURCE_UUID)).thenReturn(resource);
                when(fileService.getAllByResourceUuid(RESOURCE_UUID)).thenReturn(List.of());

                byte[] bytes = exportService.exportResourceFiles(RESOURCE_UUID);

                List<FileExportRow> rows = FastExcel.read(new ByteArrayInputStream(bytes), FileExportRow.class, null)
                                .sheet()
                                .doReadSync();
                assertTrue(rows.isEmpty());
        }

        @Test
        void exportResourceFilesWithUnknownIdThrowsNotFound() {
                when(resourceService.getByUuid(RESOURCE_UUID))
                                .thenThrow(new ResourceNotFoundException("Resource not found by uuid"));

                assertThrows(ResourceNotFoundException.class,
                                () -> exportService.exportResourceFiles(RESOURCE_UUID));
        }

        @Test
        void exportResourceFilesVerifiesResourceExistsBeforeLoadingFiles() {
                Resource resource = new Resource();
                resource.setUuid(RESOURCE_UUID);
                when(resourceService.getByUuid(RESOURCE_UUID)).thenReturn(resource);
                when(fileService.getAllByResourceUuid(RESOURCE_UUID)).thenReturn(List.of());

                exportService.exportResourceFiles(RESOURCE_UUID);

                verify(resourceService).getByUuid(RESOURCE_UUID);
                verify(fileService).getAllByResourceUuid(RESOURCE_UUID);
        }
}
