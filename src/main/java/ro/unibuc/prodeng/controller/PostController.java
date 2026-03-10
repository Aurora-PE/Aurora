package ro.unibuc.prodeng.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ro.unibuc.prodeng.request.CreatePostRequest;
import ro.unibuc.prodeng.request.UpdatePostRequest;
import ro.unibuc.prodeng.response.PostResponse;
import ro.unibuc.prodeng.service.PostService;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(
        @PathVariable String id, @RequestHeader("Authorization") String authHeader
    ) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
        @RequestBody CreatePostRequest request,
         @RequestHeader("Authorization") String authHeader
    ) {
        PostResponse created = postService.createPost(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
        @PathVariable String id,
        @RequestBody UpdatePostRequest request,
         @RequestHeader("Authorization") String authHeader
    ) {
        return ResponseEntity.ok(
            postService.updatePost(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
        @PathVariable String id,  @RequestHeader("Authorization") String authHeader
    ) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}