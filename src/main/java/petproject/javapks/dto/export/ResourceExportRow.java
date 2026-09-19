package petproject.javapks.dto.export;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.format.DateTimeFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceExportRow {

    @ExcelProperty("UUID")
    private UUID uuid;

    @ExcelProperty("Название")
    private String title;

    @ExcelProperty("Описание")
    private String description;

    @ExcelProperty("Создан")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @ExcelProperty("Обновлён")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
