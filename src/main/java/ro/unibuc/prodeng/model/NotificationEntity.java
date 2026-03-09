package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "notifications")
public record NotificationEntity(
        @Id
        String id,
        String userId,
        String message,
        String sourceUserId,
        Boolean read,
        LocalDateTime createdAt
) {
    public NotificationEntity(String userId, String message, String sourceUserId) {
        this(null, userId, message, sourceUserId, false, LocalDateTime.now());
    }

}