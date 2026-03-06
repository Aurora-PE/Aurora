package ro.unibuc.prodeng.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ro.unibuc.prodeng.response.UserResponse;
import ro.unibuc.prodeng.response.UserSummaryResponse;
import ro.unibuc.prodeng.service.FollowService;
import ro.unibuc.prodeng.util.JwtUtil;

@RestController
@RequestMapping("/api/users")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> followUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {
        String requesterId = JwtUtil.extractRequesterId(authHeader);
        followService.followUser(requesterId, id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/unfollow")
    public ResponseEntity<Void> unfollowUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {
        String requesterId = JwtUtil.extractRequesterId(authHeader);
        followService.unfollowUser(requesterId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/followers")
    public ResponseEntity<List<UserSummaryResponse>> getFollowers(
        @RequestHeader("Authorization") String authHeader
    ){
        String requesterId = JwtUtil.extractRequesterId(authHeader);
        return ResponseEntity.ok(followService.getFollowers(requesterId));

    }

    @GetMapping("/following")
    public ResponseEntity<List<UserResponse>> getFollowing(
        @RequestHeader("Authorization") String authHeader
    ){
        String requesterId = JwtUtil.extractRequesterId(authHeader);
        return ResponseEntity.ok(followService.getFollowing(requesterId));

    }
}