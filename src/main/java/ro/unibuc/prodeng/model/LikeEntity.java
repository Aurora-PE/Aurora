package ro.unibuc.prodeng.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "likes")
public record LikeEntity(
    @Id String id,
    String userId,
    String targetId,
    LikeTargetTypeEnum targetType,
    LocalDateTime createdAt
) {}