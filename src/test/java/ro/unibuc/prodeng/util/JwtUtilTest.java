package ro.unibuc.prodeng.util;

import org.junit.jupiter.api.Test;
import ro.unibuc.prodeng.exception.UnauthorizedException;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @Test
    void testGenerateToken_validUserId_tokenContainsSameId() {
        String userId = "user123";

        String token = JwtUtil.generateToken(userId);
        String extractedId = JwtUtil.extractUserId(token);

        assertEquals(userId, extractedId);
    }

    @Test
    void testExtractRequesterId_validAuthHeader_returnsUserId() {
        String userId = "user123";
        String token = JwtUtil.generateToken(userId);
        String authHeader = "Bearer " + token;

        String extractedId = JwtUtil.extractRequesterId(authHeader);

        assertEquals(userId, extractedId);
    }

    @Test
    void testExtractRequesterId_nullAuthHeader_throwsUnauthorizedException() {
        assertThrows(UnauthorizedException.class, () -> JwtUtil.extractRequesterId(null));
    }

    @Test
    void testExtractRequesterId_invalidPrefix_throwsUnauthorizedException() {
        String authHeader = "Basic some_random_token";

        assertThrows(UnauthorizedException.class, () -> JwtUtil.extractRequesterId(authHeader));
    }

    @Test
    void testExtractRequesterId_invalidTokenFormat_throwsUnauthorizedException() {
        String authHeader = "Bearer invalid.jwt.token";

        assertThrows(UnauthorizedException.class, () -> JwtUtil.extractRequesterId(authHeader));
    }
}