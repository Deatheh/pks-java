// service/ImportService.java
package petproject.javapks.service;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import petproject.javapks.dto.request.admin.RegisterRequest;
import petproject.javapks.dto.request.resource.CreateResourceRequest;
import petproject.javapks.dto.response.ImportResultDto;
import petproject.javapks.dto.response.ImportResultDto.RowError;
import petproject.javapks.exception.ImportException;
import petproject.javapks.exception.ImportValidationException;
import petproject.javapks.mapper.ResourceMapper;
import petproject.javapks.mapper.UserMapper;
import petproject.javapks.model.Resource;
import petproject.javapks.model.Role;
import petproject.javapks.model.User;
import petproject.javapks.repository.ResourceRepository;
import petproject.javapks.repository.UserRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class ImportService {

    private static final int MAX_ROWS = 10_000;

    private final Validator validator;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final ResourceMapper resourceMapper;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    @Transactional
    public ImportResultDto importUsers(MultipartFile file, char delimiter, boolean dryRun) {
        List<RowError> errors = new ArrayList<>();
        List<User> valid = new ArrayList<>();
        Set<String> emailsInFile = new HashSet<>();
        int total = 0;

        try (CSVParser parser = openCsv(file, delimiter)) {
            Map<String, String> cols = requireColumns(parser,
                    "email", "password", "role", "firstname", "lastname");

            for (CSVRecord rec : parser) {
                int row = ++total + 1; // +1 — строка заголовков, номер как в Excel
                checkRowLimit(total);

                Role role = null;
                String roleRaw = cell(rec, cols, "role");
                if (roleRaw != null) {
                    try {
                        role = Role.valueOf(roleRaw.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        errors.add(new RowError(row, "role", "Must be one of: USER, MODER, ADMIN"));
                        continue;
                    }
                }

                var dto = new RegisterRequest(
                        cell(rec, cols, "email"), cell(rec, cols, "password"),
                        role, cell(rec, cols, "firstname"), cell(rec, cols, "lastname"));

                int before = errors.size();
                validate(dto, row, errors);
                if (dto.email() != null) {
                    if (!emailsInFile.add(dto.email().toLowerCase())) {
                        errors.add(new RowError(row, "email", "Duplicate email in file"));
                    } else if (userRepository.existsByEmail(dto.email())) {
                        errors.add(new RowError(row, "email", "Email already in use"));
                    }
                }
                if (errors.size() == before) {
                    User user = userMapper.toEntity(dto); // enabled = true, как при регистрации
                    user.setPassword(passwordEncoder.encode(dto.password()));
                    valid.add(user);
                }
            }
        } catch (IOException | UncheckedIOException | IllegalStateException e) {
            throw new ImportException("Malformed CSV file", e);
        }
        checkNotEmpty(total);
        return finish(dryRun, total, valid, errors, userRepository::saveAll);
    }

    @Transactional
    public ImportResultDto importResources(MultipartFile file, char delimiter, boolean dryRun) {
        List<RowError> errors = new ArrayList<>();
        List<Resource> valid = new ArrayList<>();
        int total = 0;

        try (CSVParser parser = openCsv(file, delimiter)) {
            Map<String, String> cols = requireColumns(parser, "title");

            for (CSVRecord rec : parser) {
                int row = ++total + 1;
                checkRowLimit(total);

                var dto = new CreateResourceRequest(
                        cell(rec, cols, "title"), cell(rec, cols, "description"));

                if (validate(dto, row, errors)) {
                    valid.add(resourceMapper.toEntity(dto));
                }
            }
        } catch (IOException | UncheckedIOException | IllegalStateException e) {
            throw new ImportException("Malformed CSV file", e);
        }
        checkNotEmpty(total);
        return finish(dryRun, total, valid, errors, resourceRepository::saveAll);
    }

    // ===== Общие помощники =====

    private CSVParser openCsv(MultipartFile file, char delimiter) {
        if (file == null || file.isEmpty()) {
            throw new ImportException("File is empty");
        }
        try {
            PushbackReader reader = new PushbackReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8), 1);
            int first = reader.read();
            if (first != '\uFEFF' && first != -1) {
                reader.unread(first); // Excel сохраняет UTF-8 с BOM — пропускаем
            }
            return CSVFormat.DEFAULT.builder()
                    .setDelimiter(delimiter)
                    .setHeader().setSkipHeaderRecord(true)
                    .setIgnoreSurroundingSpaces(true)
                    .setTrim(true)
                    .get()
                    .parse(reader);
        } catch (IOException e) {
            throw new ImportException("Failed to read CSV file", e);
        }
    }

    // Заголовки регистронезависимы, порядок любой, лишние колонки игнорируются
    private Map<String, String> requireColumns(CSVParser parser, String... required) {
        Map<String, String> cols = new HashMap<>();
        for (String h : parser.getHeaderNames()) {
            if (cols.putIfAbsent(h.toLowerCase(), h) != null) {
                throw new ImportException("Duplicate column: " + h);
            }
        }
        List<String> missing = Arrays.stream(required).filter(r -> !cols.containsKey(r)).toList();
        if (!missing.isEmpty()) {
            throw new ImportException("Missing required columns: " + String.join(", ", missing));
        }
        return cols;
    }

    private String cell(CSVRecord rec, Map<String, String> cols, String name) {
        String header = cols.get(name);
        if (header == null || !rec.isSet(header)) {
            return null;
        }
        String value = rec.get(header);
        // пустая ячейка -> null: иначе "" поймает @Size(min=1) на description
        return (value == null || value.isBlank()) ? null : value;
    }

    private boolean validate(Object dto, int row, List<RowError> errors) {
        int before = errors.size();
        validator.validate(dto).forEach(v ->
                errors.add(new RowError(row, v.getPropertyPath().toString(), v.getMessage())));
        return errors.size() == before;
    }

    private void checkRowLimit(int total) {
        if (total > MAX_ROWS) {
            throw new ImportException("Too many rows (limit " + MAX_ROWS + ")");
        }
    }

    private void checkNotEmpty(int total) {
        if (total == 0) {
            throw new ImportException("CSV contains no data rows");
        }
    }

    private <T> ImportResultDto finish(boolean dryRun, int total, List<T> valid,
                                    List<RowError> errors, Consumer<List<T>> save) {
        if (!errors.isEmpty()) {
            ImportResultDto report = new ImportResultDto(total, valid.size(), dryRun, false, errors);
            if (dryRun) {
                return report; // проверка: 200 + отчёт
            }
            throw new ImportValidationException(report); // импорт: 422, ничего не сохранено
        }
        if (!dryRun) {
            save.accept(valid); // одна транзакция: всё или ничего
        }
        return new ImportResultDto(total, valid.size(), dryRun, !dryRun, List.of());
    }
}