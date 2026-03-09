package ro.unibuc.prodeng.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="posts")
public record PostEntity (
    @Id String id,
    String authorId,
    String content,
    String imageUrl,
    VisibilityEnum visibility,
    int likesCount,
    LocalDateTime localDateTime
){ 
}
