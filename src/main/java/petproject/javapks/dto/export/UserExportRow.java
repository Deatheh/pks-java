package petproject.javapks.dto.export;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.format.DateTimeFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserExportRow {

    @ExcelProperty("UUID")
    private String uuid;

    @ExcelProperty("Email")
    private String email;

    @ExcelProperty("Роль")
    private String role;

    @ExcelProperty("Имя")
    private String firstName;

    @ExcelProperty("Фамилия")
    private String lastName;

    @ExcelProperty("Активен")
    private Boolean enabled;

    @ExcelProperty("Создан")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @ExcelProperty("Обновлён")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
