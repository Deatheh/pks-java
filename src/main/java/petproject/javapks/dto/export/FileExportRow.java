package petproject.javapks.dto.export;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileExportRow {

    @ExcelProperty("UUID")
    private UUID uuid;

    @ExcelProperty("Имя файла")
    private String name;

    @ExcelProperty("Тип содержимого")
    private String contentType;

    @ExcelProperty("Размер")
    private Long size;
}
