package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.*;
import ro.unibuc.prodeng.repository.CommentRepository;
import ro.unibuc.prodeng.repository.LikeRepository;
import ro.unibuc.prodeng.repository.PostRepository;
import ro.unibuc.prodeng.request.CreatePostRequest;
import ro.unibuc.prodeng.request.UpdatePostRequest;
import ro.unibuc.prodeng.response.PostResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private PostService postService;

    private PostEntity post;
    private CommentEntity comment1;
    private CommentEntity comment2;

    @BeforeEach
    void setUp() {
        post = new PostEntity(
            "post1", "user1", "Test content",
            "https://example.com/img.png",
            VisibilityEnum.PUBLIC, 0, LocalDateTime.now()
        );

        comment1 = new CommentEntity(
            "comment1", "post1", "user2",
            "Nice post!", 2, LocalDateTime.now()
        );

        comment2 = new CommentEntity(
            "comment2", "post1", "user3",
            "I agree!", 1, LocalDateTime.now()
        );
    }


    @Test
    void createPost_success() {
        CreatePostRequest request = new CreatePostRequest(
            "user1", "Test content",
            "https://example.com/img.png",
            VisibilityEnum.PUBLIC
        );

        when(postRepository.save(any(PostEntity.class)))
            .thenReturn(post);

        PostResponse response = postService.createPost(request);

        assertNotNull(response);
        assertEquals("user1", response.authorId());
        assertEquals("Test content", response.content());
        assertEquals(VisibilityEnum.PUBLIC, response.visibility());
        assertEquals(0, response.likesCount());
        verify(postRepository).save(any(PostEntity.class));
    }

    @Test
    void getPost_success() {
        when(postRepository.findById("post1"))
            .thenReturn(Optional.of(post));

        PostResponse response = postService.getPost("post1");

        assertNotNull(response);
        assertEquals("post1", response.id());
        verify(postRepository).findById("post1");
    }

    @Test
    void getPost_notFound_throwsException() {
        when(postRepository.findById("invalid"))
            .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> postService.getPost("invalid")
        );
    }

    @Test
    void getAllPosts_success() {
        when(postRepository.findAll())
            .thenReturn(List.of(post));

        List<PostResponse> responses =
            postService.getAllPosts();

        assertEquals(1, responses.size());
        assertEquals("post1", responses.get(0).id());
    }

   

    @Test
    void updatePost_success() {
        UpdatePostRequest request = new UpdatePostRequest(
            "Updated content",
            "https://example.com/new.png",
            VisibilityEnum.PRIVATE
        );

        PostEntity updated = new PostEntity(
            "post1", "user1", "Updated content",
            "https://example.com/new.png",
            VisibilityEnum.PRIVATE, 0, post.localDateTime()
        );

        when(postRepository.findById("post1"))
            .thenReturn(Optional.of(post));
        when(postRepository.save(any(PostEntity.class)))
            .thenReturn(updated);

        PostResponse response =
            postService.updatePost("post1", request);

        assertEquals("Updated content", response.content());
        assertEquals(VisibilityEnum.PRIVATE,
            response.visibility());
        verify(postRepository).save(any(PostEntity.class));
    }

    

    
    @Test
    void deletePost_cascadesDeleteCommentsAndLikes() {
        when(postRepository.findById("post1"))
            .thenReturn(Optional.of(post));
        when(commentRepository.findByPostIdOrderByCreatedAtDesc(
            "post1"
        )).thenReturn(List.of(comment1, comment2));

        postService.deletePost("post1");

        verify(likeRepository).deleteAllByTargetIdAndTargetType(
            "post1", LikeTargetTypeEnum.POST
        );

        
        verify(commentService).deleteComment("comment1");
        verify(commentService).deleteComment("comment2");

        
        verify(postRepository).delete(post);
    }

    @Test
    void deletePost_noComments_deletesPostAndPostLikes() {
        when(postRepository.findById("post1"))
            .thenReturn(Optional.of(post));
        when(commentRepository.findByPostIdOrderByCreatedAtDesc(
            "post1"
        )).thenReturn(List.of());

        postService.deletePost("post1");

        verify(likeRepository).deleteAllByTargetIdAndTargetType(
            "post1", LikeTargetTypeEnum.POST
        );
        verify(commentService, never()).deleteComment(any());
        verify(postRepository).delete(post);
    }

}