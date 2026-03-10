package ro.unibuc.prodeng.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.prodeng.service.LikeService;

@RestController
@RequestMapping("/api")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Void> likePost(
        @PathVariable String postId,
        @RequestParam String userId
    ) {
        likeService.likePost(postId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/{postId}/unlike")
    public ResponseEntity<Void> unlikePost(
        @PathVariable String postId,
        @RequestParam String userId
    ) {
        likeService.unlikePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/like")
    public ResponseEntity<Void> likeComment(
        @PathVariable String postId,
        @PathVariable String commentId,
        @RequestParam String userId
    ) {
        likeService.likeComment(commentId, userId);
        return ResponseEntity.ok().build();
    }
}