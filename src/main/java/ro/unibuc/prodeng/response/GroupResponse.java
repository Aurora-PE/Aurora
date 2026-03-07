package ro.unibuc.prodeng.response;

import java.time.LocalDateTime;

public record GroupResponse(
    String id,
    String name,
    String description,
    String creatorId,
    LocalDateTime createdAt
) {}
