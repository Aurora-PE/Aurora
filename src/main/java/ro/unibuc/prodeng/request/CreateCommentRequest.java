package ro.unibuc.prodeng.request;

public record CreateCommentRequest(
    String authorId,
    String content
) {}