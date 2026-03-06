package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.NotBlank;

public record FollowRequest(
    @NotBlank(message = "Follower ID is required")
    String followerId
) {}