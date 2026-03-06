package ro.unibuc.prodeng.response;

public record LoginResponse(
    String token,
    UserResponse user
) {}