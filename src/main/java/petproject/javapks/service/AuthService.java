package petproject.javapks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import petproject.javapks.dto.request.auth.LoginRequest;
import petproject.javapks.dto.request.auth.RefreshTokenRequest;
import petproject.javapks.dto.response.UserDto;
import petproject.javapks.dto.response.jwt.JwtTokenResponse;
import petproject.javapks.mapper.UserMapper;
import petproject.javapks.security.jwt.JwtService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public JwtTokenResponse login(LoginRequest dto){
        log.info("Login attempt for email: {}", dto.email());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.email(),
                        dto.password()
                )
        );
        log.info("User authentication with email: {} completed", dto.email());
        return jwtService.generateToken(dto.email());
    }

    public JwtTokenResponse refresh(RefreshTokenRequest dto){
        log.info("Refresh token request");
        JwtTokenResponse tokenResponse = jwtService.refreshToken(dto.refreshToken());
        log.info("Token refreshed successfully");
        return tokenResponse;
    }

    public void logout(String accessToken, String refreshToken) {
        log.info("Logout request");
        if (accessToken != null) {
            jwtService.blacklistToken(accessToken);
        }
        if (refreshToken != null) {
            jwtService.blacklistToken(refreshToken);
        }
        log.info("User logged out successfully");
    }
}
