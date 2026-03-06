package ro.unibuc.prodeng.response;

import java.time.LocalDateTime;

public record UserResponse(
    String id,
    String username,
    String email,
    String bio,
    String avatarUrl,
    boolean isPrivate,
    LocalDateTime createdAt
) {}