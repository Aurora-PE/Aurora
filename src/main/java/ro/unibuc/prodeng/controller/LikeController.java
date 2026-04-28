package ro.unibuc.prodeng.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.prodeng.service.LikeService;
import ro.unibuc.prodeng.service.MetricsService;

@RestController
@RequestMapping("/api")
public class LikeController {

    private final LikeService likeService;
    private final MetricsService metricsService;

    public LikeController(LikeService likeService, MetricsService metricsService) {
        this.likeService = likeService;
        this.metricsService = metricsService;
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Void> likePost(
        @PathVariable String postId,
        @RequestParam String userId,
        @RequestHeader("Authorization") String authHeader
    ) {
        try {
            likeService.likePost(postId, userId);
            metricsService.recordLikeGiven();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            metricsService.recordError();
            throw e;
        }
    }

    @DeleteMapping("/posts/{postId}/unlike")
    public ResponseEntity<Void> unlikePost(
        @PathVariable String postId,
        @RequestParam String userId,
        @RequestHeader("Authorization") String authHeader
    ) {
        try {
            likeService.unlikePost(postId, userId);
            metricsService.recordLikeRemoved();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            metricsService.recordError();
            throw e;
        }
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/like")
    public ResponseEntity<Void> likeComment(
        @PathVariable String postId,
        @PathVariable String commentId,
        @RequestParam String userId,
        @RequestHeader("Authorization") String authHeader
    ) {
        try {
            likeService.likeComment(commentId, userId);
            metricsService.recordLikeGiven();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            metricsService.recordError();
            throw e;
        }
    }
}