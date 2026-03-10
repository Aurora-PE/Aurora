package ro.unibuc.prodeng.service;



import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.model.FollowEntity;
import ro.unibuc.prodeng.model.FollowEntity;
import ro.unibuc.prodeng.model.VisibilityEnum;
import ro.unibuc.prodeng.model.VisibilityEnum;
import ro.unibuc.prodeng.repository.FollowRepository;
import ro.unibuc.prodeng.repository.PostRepository;
import ro.unibuc.prodeng.response.PostResponse;
import java.util.List;

@Service
public class FeedService {

    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final PostService postService;

    public FeedService(
        PostRepository postRepository,
        FollowRepository followRepository,
        PostService postService
    ) {
        this.postRepository = postRepository;
        this.followRepository = followRepository;
        this.postService = postService;
    }

    public List<PostResponse> getFeed(String userId) {
        List<String> followingIds = followRepository
            .findByFollowerId(userId)
            .stream()
            .map(FollowEntity::followingId)
            .toList();

        if (followingIds.isEmpty()) {
            return List.of();
        }

        return postRepository
            .findByAuthorIdInOrderByLocalDateTimeDesc(followingIds)
            .stream()
            .filter(post ->
                post.visibility() == VisibilityEnum.PUBLIC
                    || post.authorId().equals(userId)
            )
            .map(postService::toResponse)
            .toList();
    }
}