package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(

        @NotBlank(message = "Content cannot be empty")
        String content

) {}