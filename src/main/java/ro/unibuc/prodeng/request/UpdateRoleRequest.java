package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateRoleRequest(
    @NotBlank(message = "Role is required (ADMIN or MEMBER)")
    String role
) {}