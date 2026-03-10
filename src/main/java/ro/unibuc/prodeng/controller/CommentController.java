package ro.unibuc.prodeng.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.prodeng.request.CreateCommentRequest;
import ro.unibuc.prodeng.response.CommentResponse;
import ro.unibuc.prodeng.service.CommentService;
import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments( @RequestHeader("Authorization") String authHeader,
        @PathVariable String postId
    ) {
        return ResponseEntity.ok(
            commentService.getCommentsByPostId(postId)
        );
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
        @PathVariable String postId,
        @RequestBody CreateCommentRequest request, @RequestHeader("Authorization") String authHeader
    ) {
        CommentResponse created =
            commentService.createComment(postId, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(created);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
        @PathVariable String postId,
        @PathVariable String commentId,
         @RequestHeader("Authorization") String authHeader
    ) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}