package ro.unibuc.prodeng.response;

import java.time.LocalDateTime;

public record GroupMemberResponse(
    String userId,
    String username,
    String avatarUrl,
    String role,
    LocalDateTime joinedAt
) {}