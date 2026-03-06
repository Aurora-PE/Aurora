package ro.unibuc.prodeng.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import ro.unibuc.prodeng.exception.UnauthorizedException;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "AuroraMvpSuperSecretKeyThatIsVeryLong123!";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    private static final long EXPIRATION_TIME = 86400000;

    public static String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String extractUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public static String extractRequesterId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header. Must be 'Bearer <token>'");
        }
        
        String token = authHeader.substring(7);
        
        try {
            return JwtUtil.extractUserId(token);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid or expired JWT token.");
        }
    }
}