package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "group_members")
public record GroupMemberEntity(
    @Id String id,
    String groupId,
    String userId,
    Role role,
    LocalDateTime joinedAt
) {
    public enum Role {
        ADMIN, MEMBER
    }

    public GroupMemberEntity(String groupId, String userId, Role role) {
        this(null, groupId, userId, role, LocalDateTime.now());
    }
}