package ro.unibuc.prodeng.response;

import java.time.LocalDateTime;

import ro.unibuc.prodeng.model.VisibilityEnum;

public record PostResponse(
    String id,
    String authorId,
    String content,
    String imageUrl,
    VisibilityEnum visibility,
    int likesCount,
    LocalDateTime createdAt) {
    
}
