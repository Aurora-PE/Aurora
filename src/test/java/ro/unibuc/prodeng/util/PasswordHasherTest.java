package ro.unibuc.prodeng.util;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class PasswordHasherTest {

    @Test
    void testHashPassword_validPassword_returnsDeterministicHash() {
        String password = "mySecretPassword123!";

        String hash1 = PasswordHasher.hashPassword(password);
        String hash2 = PasswordHasher.hashPassword(password);

        assertNotNull(hash1);
        assertNotEquals(password, hash1);
        assertEquals(hash1, hash2);
    }

    @Test
    void testHashPassword_nullPassword_returnsNull() {
        String result = PasswordHasher.hashPassword(null);

        assertNull(result);
    }

    @Test
    void testHashPassword_missingAlgorithm_throwsRuntimeException() {
        try (MockedStatic<MessageDigest> mockedDigest = mockStatic(MessageDigest.class)) {
            mockedDigest.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("Forced exception"));

            assertThrows(RuntimeException.class, () -> PasswordHasher.hashPassword("password"));
        }
    }
}