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
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.CreateCommentRequest;
import ro.unibuc.prodeng.response.CommentResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CommentService commentService;

    private PostEntity post;
    private UserEntity user;
    private CommentEntity comment;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        post = new PostEntity(
            "post1", "author1", "Post content",
            null, VisibilityEnum.PUBLIC,
            0, now
        );

        user = new UserEntity(
            "user1", "testuser", "test@example.com",
            "password", "bio", null, now, null
        );

        comment = new CommentEntity(
            "comment1", "post1", "user1",
            "Nice post!", 0, now
        );
    }


    @Test
    void createComment_success() {
        CreateCommentRequest request =
            new CreateCommentRequest("user1", "Nice post!");

        when(postRepository.findById("post1"))
            .thenReturn(Optional.of(post));
        when(commentRepository.save(any(CommentEntity.class)))
            .thenReturn(comment);
        when(userRepository.findById("user1"))
            .thenReturn(Optional.of(user));

        CommentResponse response =
            commentService.createComment("post1", request);

        assertNotNull(response);
        assertEquals("comment1", response.id());
        assertEquals("post1", response.postId());
        assertEquals("user1", response.authorId());
        assertEquals("Nice post!", response.content());
        assertEquals(0, response.likesCount());

        verify(postRepository, times(2)).findById("post1");
        verify(commentRepository).save(any(CommentEntity.class));
        verify(userRepository).findById("user1");
        verify(notificationService).createNotification(
            eq("author1"), anyString(), eq("user1")
        );
    }

    @Test
    void createComment_postNotFound() {
        CreateCommentRequest request =
            new CreateCommentRequest("user1", "Nice post!");

        when(postRepository.findById("invalid"))
            .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            commentService.createComment("invalid", request)
        );

        verify(postRepository).findById("invalid");
        verifyNoInteractions(commentRepository);
        verifyNoInteractions(notificationService);
    }


    @Test
    void getCommentsByPostId_success() {
        CommentEntity comment2 = new CommentEntity(
            "comment2", "post1", "user2",
            "Great!", 2, now
        );

        when(commentRepository
            .findByPostIdOrderByCreatedAtDesc("post1"))
            .thenReturn(List.of(comment, comment2));

        List<CommentResponse> responses =
            commentService.getCommentsByPostId("post1");

        assertEquals(2, responses.size());
        assertEquals("comment1", responses.get(0).id());
        assertEquals("comment2", responses.get(1).id());

        verify(commentRepository)
            .findByPostIdOrderByCreatedAtDesc("post1");
    }


    @Test
    void deleteComment_success() {
        when(commentRepository.findById("comment1"))
            .thenReturn(Optional.of(comment));

        commentService.deleteComment("comment1");

        verify(commentRepository).findById("comment1");
        verify(likeRepository).deleteAllByTargetIdAndTargetType(
            "comment1", LikeTargetTypeEnum.COMMENT
        );
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_notFound() {
        when(commentRepository.findById("invalid"))
            .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            commentService.deleteComment("invalid")
        );

        verify(commentRepository).findById("invalid");
        verifyNoInteractions(likeRepository);
        verify(commentRepository, never())
            .delete(any(CommentEntity.class));
    }
}