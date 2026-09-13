package petproject.javapks.dto.response.jwt;

public record JwtTokenResponse(
    String accessToken,
    String refreshToken
) {}
