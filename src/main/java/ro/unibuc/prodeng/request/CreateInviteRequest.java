package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.NotBlank;

public record CreateInviteRequest(
    @NotBlank
    String inviteeId
) {}
