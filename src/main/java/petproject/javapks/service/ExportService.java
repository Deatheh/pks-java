package petproject.javapks.service;

import cn.idev.excel.FastExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import petproject.javapks.dto.export.FileExportRow;
import petproject.javapks.dto.export.ResourceExportRow;
import petproject.javapks.dto.export.UserExportRow;
import petproject.javapks.exception.ExportException;
import petproject.javapks.model.File;
import petproject.javapks.model.Resource;
import petproject.javapks.model.User;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private final UserService userService;
    private final ResourceService resourceService;
    private final FileService fileService;

    public byte[] exportUsers() {
        List<UserExportRow> rows = userService.getAll().stream()
                .map(this::toUserRow)
                .toList();
        return write("users", UserExportRow.class, rows);
    }

    public byte[] exportResources() {
        List<ResourceExportRow> rows = resourceService.getAll().stream()
                .map(this::toResourceRow)
                .toList();
        return write("resources", ResourceExportRow.class, rows);
    }

    public byte[] exportResourceFiles(UUID resourceId) {
        // Проверяем существование ресурса: несуществующий id → 404, а не пустой лист
        resourceService.getByUuid(resourceId);
        List<FileExportRow> rows = fileService.getAllByResourceUuid(resourceId).stream()
                .map(this::toFileRow)
                .toList();
        return write("files", FileExportRow.class, rows);
    }

    private <T> byte[] write(String sheetName, Class<T> rowClass, List<T> rows) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            FastExcel.write(baos, rowClass).sheet(sheetName).doWrite(rows);
            return baos.toByteArray();
        } catch (Exception e) {
            log.warn("Export to XLSX failed: {}", e.getMessage());
            throw new ExportException("Failed to export data to XLSX", e);
        }
    }

    private UserExportRow toUserRow(User user) {
        return new UserExportRow(
                user.getUuid(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getFirstName(),
                user.getLastName(),
                user.getEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    private ResourceExportRow toResourceRow(Resource resource) {
        return new ResourceExportRow(
                resource.getUuid(),
                resource.getTitle(),
                resource.getDescription(),
                resource.getCreatedAt(),
                resource.getUpdatedAt());
    }

    private FileExportRow toFileRow(File file) {
        return new FileExportRow(
                file.getUuid(),
                file.getName(),
                file.getContentType(),
                file.getSize());
    }
}
