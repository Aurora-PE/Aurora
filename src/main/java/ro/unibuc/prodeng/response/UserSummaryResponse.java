package ro.unibuc.prodeng.response;

public record UserSummaryResponse(
    String id,
    String username,
    String avatarUrl
) {}