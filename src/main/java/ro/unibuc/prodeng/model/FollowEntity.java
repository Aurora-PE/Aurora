package ro.unibuc.prodeng.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document( collection= "follows")
public record FollowEntity (
    @Id
    String id,
    String followerId,
    String followingId,
    LocalDateTime createdAt
){
    public FollowEntity(String followerId, String followingId) {
        this(null, followerId, followingId, LocalDateTime.now());
    }
}
