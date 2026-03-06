package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "users")
public record UserEntity(
    @Id String id,
    String username,
    String email,
    String passwordHash,
    String bio,
    String avatarUrl,
    LocalDateTime createdAt,
    Boolean isPrivate
) {
    public UserEntity(String username, String email, String passwordHash, String bio, String avatarUrl, Boolean isPrivate) {
        this(null, username, email, passwordHash, bio, avatarUrl, LocalDateTime.now(), isPrivate);
    }
}