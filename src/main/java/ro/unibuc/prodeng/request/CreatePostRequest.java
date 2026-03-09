package ro.unibuc.prodeng.request;

import ro.unibuc.prodeng.model.VisibilityEnum;

public record CreatePostRequest (
    String authorId,
    String content,
    String imageUrl,
    VisibilityEnum visibility
){}
