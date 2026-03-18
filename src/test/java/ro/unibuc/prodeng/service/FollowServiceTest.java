package ro.unibuc.prodeng.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.FollowEntity;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.FollowRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.response.UserResponse;
import ro.unibuc.prodeng.response.UserSummaryResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock private FollowRepository followRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private FollowService followService;

    private UserEntity user1;
    private UserEntity user2;

    @BeforeEach
    void setUp() {
        user1 = new UserEntity("1", "user1", "u1@test.com", "hash", "bio", "url", LocalDateTime.now(), false);
        user2 = new UserEntity("2", "user2", "u2@test.com", "hash", "bio", "url", LocalDateTime.now(), false);
    }

    @Test
    void testFollowUser_validTarget_savesFollowEntity() {
        when(userRepository.existsById("2")).thenReturn(true);
        when(followRepository.existsByFollowerIdAndFollowingId("1", "2")).thenReturn(false);
        when(userRepository.findById("1")).thenReturn(Optional.of(user1));

        followService.followUser("1", "2");

        verify(notificationService).createNotification(eq("2"), anyString(), eq("1"));
        verify(followRepository).save(any(FollowEntity.class));
    }

    @Test
    void testFollowUser_selfFollow_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> followService.followUser("1", "1"));
    }

    @Test
    void testFollowUser_alreadyFollowing_throwsIllegalArgumentException() {
        when(userRepository.existsById("2")).thenReturn(true);
        when(followRepository.existsByFollowerIdAndFollowingId("1", "2")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> followService.followUser("1", "2"));
    }

    @Test
    void testUnfollowUser_validTarget_deletesFollowEntity() {
        when(userRepository.existsById("2")).thenReturn(true);
        when(followRepository.existsByFollowerIdAndFollowingId("1", "2")).thenReturn(true);

        followService.unfollowUser("1", "2");

        verify(followRepository).deleteByFollowerIdAndFollowingId("1", "2");
    }

    @Test
    void testGetFollowers_existingUser_returnsFollowerList() {
        FollowEntity follow = new FollowEntity("f1", "1", "2", LocalDateTime.now());
        when(followRepository.findByFollowingId("2")).thenReturn(List.of(follow));
        when(userRepository.findById("1")).thenReturn(Optional.of(user1));

        List<UserSummaryResponse> followers = followService.getFollowers("2");
        
        assertEquals(1, followers.size());
        assertEquals("user1", followers.get(0).username());
    }

    @Test
    void testGetFollowing_existingUser_returnsFollowingList() {
        FollowEntity follow = new FollowEntity("f1", "1", "2", LocalDateTime.now());
        when(followRepository.findByFollowerId("1")).thenReturn(List.of(follow));
        when(userRepository.findById("2")).thenReturn(Optional.of(user2));

        List<UserResponse> following = followService.getFollowing("1");
        
        assertEquals(1, following.size());
        assertEquals("user2", following.get(0).username());
    }

    @Test
    void testFollowUser_targetNotFound_throwsEntityNotFoundException() {
        when(userRepository.existsById("2")).thenReturn(false);
        
        assertThrows(EntityNotFoundException.class, () -> followService.followUser("1", "2"));
    }

    @Test
    void testUnfollowUser_selfUnfollow_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> followService.unfollowUser("1", "1"));
    }

    @Test
    void testUnfollowUser_targetNotFound_throwsEntityNotFoundException() {
        when(userRepository.existsById("2")).thenReturn(false);
        
        assertThrows(EntityNotFoundException.class, () -> followService.unfollowUser("1", "2"));
    }

    @Test
    void testUnfollowUser_notFollowing_throwsIllegalArgumentException() {
        when(userRepository.existsById("2")).thenReturn(true);
        when(followRepository.existsByFollowerIdAndFollowingId("1", "2")).thenReturn(false);
        
        assertThrows(IllegalArgumentException.class, () -> followService.unfollowUser("1", "2"));
    }
}