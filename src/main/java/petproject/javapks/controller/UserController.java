package petproject.javapks.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import petproject.javapks.dto.response.UserDto;
import petproject.javapks.service.ExportService;
import petproject.javapks.service.UserService;
import petproject.javapks.utils.JwtUtils;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final UserService userService;
    private final ExportService exportService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> infoMe(
        HttpServletRequest request
    ){
        String accessToken = JwtUtils.extractToken(request);
        return ResponseEntity.status(HttpStatus.OK).body(userService.getInfoAboutMe(accessToken));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportUsers() {
        byte[] body = exportService.exportUsers();
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(XLSX_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users.xlsx\"")
                .body(body);
    }

}
