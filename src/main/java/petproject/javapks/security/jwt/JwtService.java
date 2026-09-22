package petproject.javapks.security.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import petproject.javapks.dto.response.jwt.JwtTokenResponse;
import petproject.javapks.exception.InvalidTokenException;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-expiration-ms}")
    private Long accessExpiration;

    @Value("${app.jwt.refresh-expiration-ms}")
    private Long refreshExpiration;

    private final TokenBlacklistService tokenBlacklistService;

    public JwtTokenResponse generateToken(String username) {
        String accessToken = buildToken(username, accessExpiration, "access");
        String refreshToken = buildToken(username, refreshExpiration, "refresh");
        return new JwtTokenResponse(accessToken, refreshToken);
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public JwtTokenResponse refreshToken(String refreshToken) {
        if (!isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Not refresh token");
        }

        if (!validateToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new InvalidTokenException("Refresh token in black list");
        }
        String username = extractUsername(refreshToken);
        tokenBlacklistService.blacklist(refreshToken, refreshExpiration);
        return generateToken(username);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractType(token));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractType(token));
    }

    public void blacklistToken(String token) {
        if (token == null || !validateToken(token)) {
            return;
        }
        long expiration = isAccessToken(token) ? accessExpiration : refreshExpiration;
        tokenBlacklistService.blacklist(token, expiration);
    }

    private String extractType(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("type", String.class);
    }

    private String buildToken(String username, Long expiration, String type) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("type", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expiration)))
                .signWith(getSigningKey())
                .compact();
    }
}
