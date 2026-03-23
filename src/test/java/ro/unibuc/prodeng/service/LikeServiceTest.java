package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.prodeng.exception.DuplicateActionException;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.UnauthorizedException;
import ro.unibuc.prodeng.model.*;
import ro.unibuc.prodeng.repository.*;
import ro.unibuc.prodeng.service.NotificationService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LikeService likeService;

    private PostEntity publicPost;
    private PostEntity privatePost;
    private CommentEntity comment;
    private UserEntity user2;

    @BeforeEach
    void setUp() {
        publicPost = new PostEntity(
            "post1", "user1", "Public post",
            null, VisibilityEnum.PUBLIC,
            0, LocalDateTime.now()
        );

        privatePost = new PostEntity(
            "post2", "user1", "Private post",
            null, VisibilityEnum.PRIVATE,
            0, LocalDateTime.now()
        );

        comment = new CommentEntity(
            "comment1", "post1", "user2",
            "Nice!", 0, LocalDateTime.now()
        );

        user2 = new UserEntity(
            "user2", "johndoe", "john@example.com",
            "hashedpw", "Bio", null, null, false
        );
    }

    @Test
    void likePost_public_success() {
        when(postRepository.findById("post1"))
            .thenReturn(Optional.of(publicPost));
        when(likeRepository
            .findByUserIdAndTargetIdAndTargetType(
                "user2", "post1", LikeTargetTypeEnum.POST
            )).thenReturn(Optional.empty());
        when(likeRepository.save(any(LikeEntity.class)))
            .thenReturn(new LikeEntity(
                "like1", "user2", "post1",
                LikeTargetTypeEnum.POST, LocalDateTime.now()
            ));
        when(postRepository.save(any(PostEntity.class)))
            .thenReturn(publicPost);
        when(userRepository.findById("user2"))
            .thenReturn(Optional.of(user2));

        likeService.likePost("post1", "user2");

        verify(likeRepository).save(any(LikeEntity.class));
        verify(postRepository).save(any(PostEntity.class));
        verify(notificationService).createNotification(
            eq("user1"), any(String.class), eq("user2")
        );
    }


    @Test
    void likePost_private_asFollower_success() {
        when(postRepository.findById("post2"))
            .thenReturn(Optional.of(privatePost));
        when(followRepository
            .existsByFollowerIdAndFollowingId(
                "user2", "user1"
            )).thenReturn(true);
        when(likeRepository
            .findByUserIdAndTargetIdAndTargetType(
                "user2", "post2", LikeTargetTypeEnum.POST
            )).thenReturn(Optional.empty());
        when(likeRepository.save(any(LikeEntity.class)))
            .thenReturn(new LikeEntity(
                "like1", "user2", "post2",
                LikeTargetTypeEnum.POST, LocalDateTime.now()
            ));
        when(postRepository.save(any(PostEntity.class)))
            .thenReturn(privatePost);
        when(userRepository.findById("user2"))
            .thenReturn(Optional.of(user2));

        likeService.likePost("post2", "user2");

        verify(likeRepository).save(any(LikeEntity.class));
        verify(postRepository).save(any(PostEntity.class));
    }


    @Test
    void likePost_private_notFollower_throwsUnauthorized() {
        when(postRepository.findById("post2"))
            .thenReturn(Optional.of(privatePost));
        when(followRepository
            .existsByFollowerIdAndFollowingId(
                "user3", "user1"
            )).thenReturn(false);

        assertThrows(UnauthorizedException.class,
            () -> likeService.likePost("post2", "user3")
        );

        verify(likeRepository, never()).save(any(LikeEntity.class));
        verify(postRepository, never())
            .save(any(PostEntity.class));
    }

    @Test
    void likePost_duplicate_throwsDuplicateAction() {
        LikeEntity existingLike = new LikeEntity(
            "like1", "user2", "post1",
            LikeTargetTypeEnum.POST, LocalDateTime.now()
        );

        when(postRepository.findById("post1"))
            .thenReturn(Optional.of(publicPost));
        when(likeRepository
            .findByUserIdAndTargetIdAndTargetType(
                "user2", "post1", LikeTargetTypeEnum.POST
            )).thenReturn(Optional.of(existingLike));

        assertThrows(DuplicateActionException.class,
            () -> likeService.likePost("post1", "user2")
        );

        verify(likeRepository, never()).save(any(LikeEntity.class));
    }

    @Test
    void likePost_postNotFound_throwsException() {
        when(postRepository.findById("invalid"))
            .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> likeService.likePost("invalid", "user2")
        );

        verify(likeRepository, never()).save(any(LikeEntity.class));
    }

    @Test
    void unlikePost_success() {
        LikeEntity existingLike = new LikeEntity(
            "like1", "user2", "post1",
            LikeTargetTypeEnum.POST, LocalDateTime.now()
        );

        PostEntity postWithLike = new PostEntity(
            "post1", "user1", "Public post",
            null, VisibilityEnum.PUBLIC,
            1, LocalDateTime.now()
        );

        when(postRepository.findById("post1"))
            .thenReturn(Optional.of(postWithLike));
        when(likeRepository
            .findByUserIdAndTargetIdAndTargetType(
                "user2", "post1", LikeTargetTypeEnum.POST
            )).thenReturn(Optional.of(existingLike));

        likeService.unlikePost("post1", "user2");

        verify(likeRepository).delete(existingLike);
        verify(postRepository).save(any(PostEntity.class));
    }

   
   
    @Test
    void likeComment_success() {
        when(commentRepository.findById("comment1"))
            .thenReturn(Optional.of(comment));
        when(likeRepository
            .findByUserIdAndTargetIdAndTargetType(
                "user1", "comment1",
                LikeTargetTypeEnum.COMMENT
            )).thenReturn(Optional.empty());
        when(likeRepository.save(any(LikeEntity.class)))
            .thenReturn(new LikeEntity(
                "like1", "user1", "comment1",
                LikeTargetTypeEnum.COMMENT, LocalDateTime.now()
            ));
        when(commentRepository.save(any(CommentEntity.class)))
            .thenReturn(comment);


        UserEntity user = new UserEntity(
            "user1", "author", "author@example.com",
            "hashedpw", "Bio", null, null, false
        );
        when(userRepository.findById("user1"))
            .thenReturn(Optional.of(user));
       
        likeService.likeComment("comment1", "user1");

        verify(likeRepository).save(any(LikeEntity.class));
        verify(commentRepository).save(any(CommentEntity.class));
        verify(notificationService).createNotification(
            eq(comment.authorId()),
            anyString(),
            eq("user1")
        );
    }

   
}