package ro.unibuc.prodeng.controller;

import java.util.List;

import io.micrometer.core.instrument.Timer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ro.unibuc.prodeng.request.CreatePostRequest;
import ro.unibuc.prodeng.request.UpdatePostRequest;
import ro.unibuc.prodeng.response.PostResponse;
import ro.unibuc.prodeng.service.MetricsService;
import ro.unibuc.prodeng.service.PostService;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final MetricsService metricsService;

    public PostController(PostService postService, MetricsService metricsService) {
        this.postService = postService;
        this.metricsService = metricsService;
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(
        @PathVariable String id, @RequestHeader("Authorization") String authHeader
    ) {
        try {
            return ResponseEntity.ok(postService.getPost(id));
        } catch (Exception e) {
            metricsService.recordError();
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
        @RequestBody CreatePostRequest request,
        @RequestHeader("Authorization") String authHeader
    ) {
        Timer.Sample sample = metricsService.startTimer();
        try {
            PostResponse created = postService.createPost(request);
            metricsService.recordPostCreated();
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            metricsService.recordError();
            throw e;
        } finally {
            metricsService.stopPostCreationTimer(sample);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
        @PathVariable String id,
        @RequestBody UpdatePostRequest request,
        @RequestHeader("Authorization") String authHeader
    ) {
        try {
            return ResponseEntity.ok(postService.updatePost(id, request));
        } catch (Exception e) {
            metricsService.recordError();
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
        @PathVariable String id, @RequestHeader("Authorization") String authHeader
    ) {
        try {
            postService.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            metricsService.recordError();
            throw e;
        }
    }
}