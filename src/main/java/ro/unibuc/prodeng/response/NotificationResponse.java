package ro.unibuc.prodeng.response;

import java.time.LocalDateTime;

public record NotificationResponse(

        String id,
        String userId,
        String message,
        String sourceUserId,
        Boolean read,
        LocalDateTime createdAt

) {}