package ro.unibuc.prodeng.response;

import java.time.LocalDateTime;

public record CommentResponse(
    String id,
    String postId,
    String authorId,
    String content,
    int likesCount,
    LocalDateTime createdAt
) {}
