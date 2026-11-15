package petproject.javapks.service;

import cn.idev.excel.FastExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import petproject.javapks.dto.export.ExportFormat;
import petproject.javapks.dto.export.FileExportRow;
import petproject.javapks.dto.export.ResourceExportRow;
import petproject.javapks.dto.export.UserExportRow;
import petproject.javapks.dto.response.ExportedFileDto;
import petproject.javapks.exception.ExportException;
import petproject.javapks.exception.ResourceNotFoundException;
import petproject.javapks.model.File;
import petproject.javapks.model.Resource;
import petproject.javapks.model.User;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

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
                user.getFirstname(),
                user.getLastname(),
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

    public ExportedFileDto exportUsers(ExportFormat format) {
        List<User> users = userService.getAll();
        return switch (format) {
            case XLSX -> new ExportedFileDto(
                    run("users", UserExportRow.class, () -> users.stream().map(this::toUserRow).toList()),
                    XLSX_CONTENT_TYPE, "users.xlsx");
            case CSV -> csv("users",
                    new String[]{"uuid", "email", "role", "firstname", "lastname", "enabled", "createdat", "updatedat"},
                    () -> users.stream().map(this::toUserCsvRow).toList());
        };
    }

    public ExportedFileDto exportResources(ExportFormat format) {
        List<Resource> resources = resourceService.getAll();
        return switch (format) {
            case XLSX -> new ExportedFileDto(
                    run("resources", ResourceExportRow.class, () -> resources.stream().map(this::toResourceRow).toList()),
                    XLSX_CONTENT_TYPE, "resources.xlsx");
            case CSV -> csv("resources",
                    new String[]{"uuid", "title", "description", "createdat", "updatedat"},
                    () -> resources.stream().map(this::toResourceCsvRow).toList());
        };
    }

    private ExportedFileDto csv(String name, String[] header, Supplier<List<Object[]>> rows) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}); // BOM — Excel и кириллица
            try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(baos, StandardCharsets.UTF_8),
                    CSVFormat.DEFAULT.builder().setHeader(header).get())) {
                for (Object[] row : rows.get()) {
                    printer.printRecord(escapeFormulas(row));
                }
            }
            return new ExportedFileDto(baos.toByteArray(), "text/csv;charset=UTF-8", name + ".csv");
        } catch (Exception e) {
            log.warn("Export to CSV failed: {}", e.getMessage());
            throw new ExportException("Failed to export data to CSV", e);
        }
    }

    // CSV injection: Excel исполняет ячейки, начинающиеся с = + - @
    private Object[] escapeFormulas(Object[] cells) {
        return Arrays.stream(cells)
                .map(c -> c instanceof String s && !s.isEmpty() && "=+-@\t".indexOf(s.charAt(0)) >= 0
                        ? "'" + s : c)
                .toArray();
    }

    private Object[] toUserCsvRow(User u) {
        // пароль не экспортируем никогда
        return new Object[]{u.getUuid(), u.getEmail(), u.getRole(), u.getFirstname(),
                u.getLastname(), u.getEnabled(), u.getCreatedAt(), u.getUpdatedAt()};
    }

    private Object[] toResourceCsvRow(Resource r) {
        return new Object[]{r.getUuid(), r.getTitle(), r.getDescription(), r.getCreatedAt(), r.getUpdatedAt()};
    }
}
