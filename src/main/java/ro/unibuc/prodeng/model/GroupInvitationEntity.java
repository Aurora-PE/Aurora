package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "group_invitations")
public record GroupInvitationEntity(
    @Id String id,
    String groupId,
    String inviterId,
    String inviteeId,
    LocalDateTime createdAt
) {

    public GroupInvitationEntity(String groupId, String inviterId, String inviteeId) {
        this(null, groupId, inviterId, inviteeId, LocalDateTime.now());
    }
}