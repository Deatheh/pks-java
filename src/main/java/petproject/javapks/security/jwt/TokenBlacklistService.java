package petproject.javapks.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final StringRedisTemplate redisTemplate;

    public void blacklist(String token, long expirationMs) {
        redisTemplate.opsForValue().set(
                "blacklist:" + token,
                "true",
                Duration.ofMillis(expirationMs));
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey("blacklist:" + token));
    }
}
