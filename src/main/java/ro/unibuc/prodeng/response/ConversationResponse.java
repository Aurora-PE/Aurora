package ro.unibuc.prodeng.response;

import java.time.LocalDateTime;

public record ConversationResponse(
        String id,
        String participant1Id,
        String participant2Id,
        LocalDateTime createdAt,
        LocalDateTime lastMessageAt
) {}