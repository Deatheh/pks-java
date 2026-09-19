package petproject.javapks.service;

import cn.idev.excel.FastExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import petproject.javapks.dto.export.FileExportRow;
import petproject.javapks.dto.export.ResourceExportRow;
import petproject.javapks.dto.export.UserExportRow;
import petproject.javapks.exception.ExportException;
import petproject.javapks.exception.ResourceNotFoundException;
import petproject.javapks.model.File;
import petproject.javapks.model.Resource;
import petproject.javapks.model.User;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private final UserService userService;
    private final ResourceService resourceService;
    private final FileService fileService;

    public byte[] exportUsers() {
        return run("users", UserExportRow.class,
                () -> userService.getAll().stream().map(this::toUserRow).toList());
    }

    public byte[] exportResources() {
        return run("resources", ResourceExportRow.class,
                () -> resourceService.getAll().stream().map(this::toResourceRow).toList());
    }

    public byte[] exportResourceFiles(UUID resourceId) {
        return run("files", FileExportRow.class, () -> {
            // Проверяем существование ресурса: несуществующий id → 404, а не пустой лист
            resourceService.getByUuid(resourceId);
            return fileService.getAllByResourceUuid(resourceId).stream().map(this::toFileRow).toList();
        });
    }

    private <T> byte[] run(String sheetName, Class<T> rowClass, Supplier<List<T>> rowsSupplier) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            FastExcel.write(baos, rowClass).sheet(sheetName).doWrite(rowsSupplier.get());
            return baos.toByteArray();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Export to XLSX failed: {}", e.getMessage());
            throw new ExportException("Failed to export data to XLSX", e);
        }
    }

    private UserExportRow toUserRow(User user) {
        // UUID передаём строкой: в FastExcel нет встроенного конвертера для UUID
        return new UserExportRow(
                user.getUuid() != null ? user.getUuid().toString() : null,
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
                resource.getUuid() != null ? resource.getUuid().toString() : null,
                resource.getTitle(),
                resource.getDescription(),
                resource.getCreatedAt(),
                resource.getUpdatedAt());
    }

    private FileExportRow toFileRow(File file) {
        return new FileExportRow(
                file.getUuid() != null ? file.getUuid().toString() : null,
                file.getName(),
                file.getContentType(),
                file.getSize());
    }
}
