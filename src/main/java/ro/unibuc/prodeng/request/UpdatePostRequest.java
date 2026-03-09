package ro.unibuc.prodeng.request;

import ro.unibuc.prodeng.model.VisibilityEnum;

public record UpdatePostRequest(
    String content,
    String imageUrl,
    VisibilityEnum visibility
) {}
