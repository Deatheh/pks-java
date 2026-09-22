package petproject.javapks.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import petproject.javapks.dto.request.auth.LoginRequest;
import petproject.javapks.dto.request.auth.RefreshTokenRequest;
import petproject.javapks.dto.response.jwt.JwtTokenResponse;
import petproject.javapks.service.AuthService;
import petproject.javapks.utils.JwtUtils;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponse> login(
            @Valid @RequestBody LoginRequest dto) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(dto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest dto) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.refresh(dto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            @RequestBody(required = false) RefreshTokenRequest dto) {
        String accessToken = JwtUtils.extractToken(request);
        String refreshToken = dto != null ? dto.refreshToken() : null;
        authService.logout(accessToken, refreshToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
