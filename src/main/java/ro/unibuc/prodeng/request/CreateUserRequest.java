package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
    @NotBlank(message= "Username is required!")
    String username,

    @NotBlank(message= "Email is required!")
    @Email(message= "Invalid email format!")
    String email, 

    @NotBlank(message= "Password is required!")
    String password,

    String bio,

    String avatarUrl,

    Boolean isPrivate
) {}