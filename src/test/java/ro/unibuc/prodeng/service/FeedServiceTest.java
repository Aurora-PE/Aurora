package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.prodeng.model.FollowEntity;
import ro.unibuc.prodeng.model.PostEntity;
import ro.unibuc.prodeng.model.VisibilityEnum;
import ro.unibuc.prodeng.repository.FollowRepository;
import ro.unibuc.prodeng.repository.PostRepository;
import ro.unibuc.prodeng.response.PostResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private PostService postService;

    @InjectMocks
    private FeedService feedService;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
    }


    @Test
    void getFeed_success_publicPosts() {
        FollowEntity follow1 = new FollowEntity(
            "f1", "user1", "author1", now
        );
        FollowEntity follow2 = new FollowEntity(
            "f2", "user1", "author2", now
        );

        PostEntity post1 = new PostEntity(
            "post1", "author1", "Post from author1",
            null, VisibilityEnum.PUBLIC, 0, now
        );
        PostEntity post2 = new PostEntity(
            "post2", "author2", "Post from author2",
            null, VisibilityEnum.PUBLIC, 5, now
        );

        PostResponse response1 = new PostResponse(
            "post1", "author1", "Post from author1",
            null, VisibilityEnum.PUBLIC, 0, now
        );
        PostResponse response2 = new PostResponse(
            "post2", "author2", "Post from author2",
            null, VisibilityEnum.PUBLIC, 5, now
        );

        when(followRepository.findByFollowerId("user1"))
            .thenReturn(List.of(follow1, follow2));
        when(postRepository
            .findByAuthorIdInOrderByLocalDateTimeDesc(
                List.of("author1", "author2")
            )).thenReturn(List.of(post1, post2));
        when(postService.toResponse(post1)).thenReturn(response1);
        when(postService.toResponse(post2)).thenReturn(response2);

        List<PostResponse> feed =
            feedService.getFeed("user1");

        assertEquals(2, feed.size());
        assertEquals("post1", feed.get(0).id());
        assertEquals("post2", feed.get(1).id());

        verify(followRepository).findByFollowerId("user1");
        verify(postRepository)
            .findByAuthorIdInOrderByLocalDateTimeDesc(
                List.of("author1", "author2")
            );
    }

    @Test
    void getFeed_filtersPrivatePosts() {
        FollowEntity follow = new FollowEntity(
            "f1", "user1", "author1", now
        );

        PostEntity publicPost = new PostEntity(
            "post1", "author1", "Public post",
            null, VisibilityEnum.PUBLIC, 0, now
        );
        PostEntity privatePost = new PostEntity(
            "post2", "author1", "Private post",
            null, VisibilityEnum.PRIVATE, 0, now
        );

        PostResponse publicResponse = new PostResponse(
            "post1", "author1", "Public post",
            null, VisibilityEnum.PUBLIC, 0, now
        );

        when(followRepository.findByFollowerId("user1"))
            .thenReturn(List.of(follow));
        when(postRepository
            .findByAuthorIdInOrderByLocalDateTimeDesc(
                List.of("author1")
            )).thenReturn(List.of(publicPost, privatePost));
        when(postService.toResponse(publicPost))
            .thenReturn(publicResponse);

        List<PostResponse> feed =
            feedService.getFeed("user1");

        assertEquals(1, feed.size());
        assertEquals("post1", feed.get(0).id());

        verify(postService, never()).toResponse(privatePost);
    }

    @Test
    void getFeed_includesOwnPrivatePosts() {
        FollowEntity follow = new FollowEntity(
            "f1", "user1", "author1", now
        );

        PostEntity otherPublic = new PostEntity(
            "post1", "author1", "Public post",
            null, VisibilityEnum.PUBLIC, 0, now
        );
        PostEntity ownPrivate = new PostEntity(
            "post2", "user1", "My private post",
            null, VisibilityEnum.PRIVATE, 0, now
        );

        PostResponse response1 = new PostResponse(
            "post1", "author1", "Public post",
            null, VisibilityEnum.PUBLIC, 0, now
        );
        PostResponse response2 = new PostResponse(
            "post2", "user1", "My private post",
            null, VisibilityEnum.PRIVATE, 0, now
        );

        when(followRepository.findByFollowerId("user1"))
            .thenReturn(List.of(follow));
        when(postRepository
            .findByAuthorIdInOrderByLocalDateTimeDesc(
                List.of("author1")
            )).thenReturn(List.of(otherPublic, ownPrivate));
        when(postService.toResponse(otherPublic))
            .thenReturn(response1);
        when(postService.toResponse(ownPrivate))
            .thenReturn(response2);

        List<PostResponse> feed =
            feedService.getFeed("user1");

        assertEquals(2, feed.size());
        assertEquals("post1", feed.get(0).id());
        assertEquals("post2", feed.get(1).id());
    }

    @Test
    void getFeed_noFollowings_returnsEmpty() {
        when(followRepository.findByFollowerId("user1"))
            .thenReturn(List.of());

        List<PostResponse> feed =
            feedService.getFeed("user1");

        assertTrue(feed.isEmpty());

        verify(followRepository).findByFollowerId("user1");
        verifyNoInteractions(postRepository);
        verifyNoInteractions(postService);
    }

    @Test
    void getFeed_followingsButNoPosts_returnsEmpty() {
        FollowEntity follow = new FollowEntity(
            "f1", "user1", "author1", now
        );

        when(followRepository.findByFollowerId("user1"))
            .thenReturn(List.of(follow));
        when(postRepository
            .findByAuthorIdInOrderByLocalDateTimeDesc(
                List.of("author1")
            )).thenReturn(List.of());

        List<PostResponse> feed =
            feedService.getFeed("user1");

        assertTrue(feed.isEmpty());
    }

    @Test
    void getFeed_allPrivatePosts_returnsEmpty() {
        FollowEntity follow = new FollowEntity(
            "f1", "user1", "author1", now
        );

        PostEntity private1 = new PostEntity(
            "post1", "author1", "Private 1",
            null, VisibilityEnum.PRIVATE, 0, now
        );
        PostEntity private2 = new PostEntity(
            "post2", "author1", "Private 2",
            null, VisibilityEnum.PRIVATE, 0, now
        );

        when(followRepository.findByFollowerId("user1"))
            .thenReturn(List.of(follow));
        when(postRepository
            .findByAuthorIdInOrderByLocalDateTimeDesc(
                List.of("author1")
            )).thenReturn(List.of(private1, private2));

        List<PostResponse> feed =
            feedService.getFeed("user1");

        assertTrue(feed.isEmpty());

        verify(postService, never()).toResponse(any());
    }
}