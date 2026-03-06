package ro.unibuc.prodeng.request;

public record UpdateUserRequest(
    String bio, 
    String avatarUrl
) {}