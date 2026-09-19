package petproject.javapks.utils;

import jakarta.servlet.http.HttpServletRequest;

public final class JwtUtils {

    private JwtUtils() {
    }

    public static String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
