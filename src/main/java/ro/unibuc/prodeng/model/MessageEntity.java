package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "messages")
public record MessageEntity(
        @Id
        String id,
        String conversationId,
        String senderId,
        String content,
        Boolean read,
        LocalDateTime createdAt
) {

    public MessageEntity(String conversationId, String senderId, String content) {
        this(null, conversationId, senderId, content, false, LocalDateTime.now());
    }
}