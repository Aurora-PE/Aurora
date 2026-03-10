package ro.unibuc.prodeng.controller;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.prodeng.response.PostResponse;
import ro.unibuc.prodeng.service.FeedService;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getFeed(
        @PathVariable String userId,  @RequestHeader("Authorization") String authHeader
    ) {
        return ResponseEntity.ok(feedService.getFeed(userId));
    }
}