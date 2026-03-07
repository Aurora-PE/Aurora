package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "groups")
public record GroupEntity(
    @Id
    String id,
    String name,
    String description,
    String creatorId,
    LocalDateTime createdAt
) {
    public GroupEntity(String name, String description, String creatorId) {
        this(null, name, description, creatorId, LocalDateTime.now());
    }
}