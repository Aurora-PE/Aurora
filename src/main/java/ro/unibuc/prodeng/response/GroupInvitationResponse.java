package ro.unibuc.prodeng.response;

import java.time.LocalDateTime;

public record GroupInvitationResponse(
    String id, 
    String groupId, 
    String inviterId, 
    String inviteeId, 
    LocalDateTime createdAt
) {}