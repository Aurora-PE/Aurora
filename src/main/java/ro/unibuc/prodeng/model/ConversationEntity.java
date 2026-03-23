package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "conversations")
public record ConversationEntity(
        @Id
        String id,
        String participant1Id,
        String participant2Id,
        LocalDateTime createdAt,
        LocalDateTime lastMessageAt
) {

    public ConversationEntity(String id, String participant1Id, String participant2Id) {
        this(id, participant1Id, participant2Id, LocalDateTime.now(), LocalDateTime.now());
    }
}