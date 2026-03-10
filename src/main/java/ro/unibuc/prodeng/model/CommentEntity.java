package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "comments")
public record CommentEntity(
    @Id String id,
    String postId,
    String authorId,
    String content,
    int likesCount,
    LocalDateTime createdAt
) {}