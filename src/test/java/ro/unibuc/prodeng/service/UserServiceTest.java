package ro.unibuc.prodeng.service;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.prodeng.exception.BadCredentialsException;
import ro.unibuc.prodeng.exception.UnauthorizedException;
import ro.unibuc.prodeng.model.*;
import ro.unibuc.prodeng.repository.*;
import ro.unibuc.prodeng.request.CreateUserRequest;
import ro.unibuc.prodeng.request.UpdateUserRequest;
import ro.unibuc.prodeng.response.LoginResponse;
import ro.unibuc.prodeng.response.UserResponse;
import ro.unibuc.prodeng.response.UserSummaryResponse;
import ro.unibuc.prodeng.util.JwtUtil;
import ro.unibuc.prodeng.util.PasswordHasher;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private FollowRepository followRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private GroupMemberRepository groupMemberRepository;
    @Mock private GroupInvitationRepository groupInvitationRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private PostRepository postRepository;
    @Mock private ConversationService conversationService;
    @Mock private PostService postService;

    @InjectMocks
    private UserService userService;

    private UserEntity publicUser;
    private UserEntity privateUser;
    private MockedStatic<PasswordHasher> mockedPasswordHasher;
    private MockedStatic<JwtUtil> mockedJwtUtil;

    @BeforeEach
    void setUp() {
        publicUser = new UserEntity("1", "john_public", "john@test.com", "hashed_pw", "Bio", "url", LocalDateTime.now(), false);
        privateUser = new UserEntity("2", "jane_private", "jane@test.com", "hashed_pw", "Bio", "url", LocalDateTime.now(), true);
        
        mockedPasswordHasher = mockStatic(PasswordHasher.class);
        mockedJwtUtil = mockStatic(JwtUtil.class);
    }

    @AfterEach
    void tearDown() {
        mockedPasswordHasher.close();
        mockedJwtUtil.close();
    }

    @Test
    void testGetUserById_sameUser_returnsUser() {
        when(userRepository.findById("1")).thenReturn(Optional.of(publicUser));
        
        UserResponse response = userService.getUserById("1", "1");
        
        assertEquals("john_public", response.username());
    }

    @Test
    void testGetUserById_publicProfile_returnsUser() {
        when(userRepository.findById("1")).thenReturn(Optional.of(publicUser));
        
        UserResponse response = userService.getUserById("2", "1");
        
        assertEquals("john_public", response.username());
    }

    @Test
    void testGetUserById_privateProfileFollowing_returnsUser() {
        when(userRepository.findById("2")).thenReturn(Optional.of(privateUser));
        when(followRepository.existsByFollowerIdAndFollowingId("1", "2")).thenReturn(true);
        
        UserResponse response = userService.getUserById("1", "2");
        
        assertEquals("jane_private", response.username());
    }

    @Test
    void testGetUserById_privateProfileNotFollowing_throwsUnauthorizedException() {
        when(userRepository.findById("2")).thenReturn(Optional.of(privateUser));
        when(followRepository.existsByFollowerIdAndFollowingId("1", "2")).thenReturn(false);
        
        assertThrows(UnauthorizedException.class, () -> userService.getUserById("1", "2"));
    }

    @Test
    void testSearchUsersByUsername_matchingUsername_returnsUserSummaries() {
        when(userRepository.findByUsernameContainingIgnoreCase("john")).thenReturn(List.of(publicUser));

        List<UserSummaryResponse> results = userService.searchUsersByUsername("john");
        
        assertEquals(1, results.size());
        assertEquals("john_public", results.get(0).username());
    }

    @Test
    void testUpdateUser_existingUser_returnsUpdatedUser() {
        when(userRepository.findById("1")).thenReturn(Optional.of(publicUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        UpdateUserRequest request = new UpdateUserRequest("New Bio", "new_url", true);
        
        UserResponse response = userService.updateUser("1", request);

        assertEquals("New Bio", response.bio());
        assertEquals("new_url", response.avatarUrl());
        assertTrue(response.isPrivate());
    }

    @Test
    void testCreateUser_validData_returnsCreatedUser() {
        CreateUserRequest request = new CreateUserRequest("new_user", "new@test.com", "password", "Bio", "url", false);
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        mockedPasswordHasher.when(() -> PasswordHasher.hashPassword("password")).thenReturn("hashed_pw");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        UserResponse response = userService.createUser(request);
        
        assertEquals("new_user", response.username());
    }

    @Test
    void testCreateUser_duplicateUsername_throwsIllegalArgumentException() {
        CreateUserRequest request = new CreateUserRequest("john_public", "new@test.com", "password", "Bio", "url", false);
        when(userRepository.findByUsername("john_public")).thenReturn(Optional.of(publicUser));

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
    }

    @Test
    void testLogin_validCredentials_returnsLoginResponse() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(publicUser));
        mockedPasswordHasher.when(() -> PasswordHasher.hashPassword("password123")).thenReturn("hashed_pw");
        mockedJwtUtil.when(() -> JwtUtil.generateToken("1")).thenReturn("mocked-jwt-token");

        LoginResponse response = userService.login("john@test.com", "password123");
        
        assertEquals("mocked-jwt-token", response.token());
        assertEquals("john_public", response.user().username());
    }

    @Test
    void testLogin_invalidPassword_throwsBadCredentialsException() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(publicUser));
        mockedPasswordHasher.when(() -> PasswordHasher.hashPassword("wrong_password")).thenReturn("wrong_hash");

        assertThrows(BadCredentialsException.class, () -> userService.login("john@test.com", "wrong_password"));
    }

    @Test
    void testDeleteUser_withOwnedGroupAndOtherMembers_transfersOwnership() {
        when(userRepository.existsById("1")).thenReturn(true);
        GroupEntity group = new GroupEntity("g1", "Test Group", "Desc", "1", LocalDateTime.now());
        when(groupRepository.findByCreatorId("1")).thenReturn(List.of(group));
        GroupMemberEntity otherMember = new GroupMemberEntity("m1", "g1", "2", GroupMemberEntity.Role.MEMBER, LocalDateTime.now());
        when(groupMemberRepository.findByGroupId("g1")).thenReturn(List.of(otherMember));
        PostEntity post = new PostEntity("p1", "content", "1", null, null, 0, LocalDateTime.now());
        when(postRepository.findByAuthorIdOrderByLocalDateTimeDesc("1")).thenReturn(List.of(post));

        userService.deleteUser("1");

        verify(followRepository).deleteByFollowerId("1");
        verify(groupInvitationRepository).deleteByInviterId("1");
        verify(notificationRepository).deleteByUserId("1");
        verify(conversationService).deleteConversation("1");
        verify(postService).deletePost("p1");
        verify(userRepository).deleteById("1");
        verify(groupRepository).save(argThat(savedGroup -> savedGroup.creatorId().equals("2")));
        verify(groupRepository, never()).deleteById("g1");
    }

    @Test
    void testDeleteUser_withOwnedGroupAndNoOtherMembers_deletesGroup() {
        when(userRepository.existsById("1")).thenReturn(true);
        GroupEntity group = new GroupEntity("g1", "Test Group", "Desc", "1", LocalDateTime.now());
        when(groupRepository.findByCreatorId("1")).thenReturn(List.of(group));
        GroupMemberEntity theLeavingCreator = new GroupMemberEntity("m1", "g1", "1", GroupMemberEntity.Role.ADMIN, LocalDateTime.now());
        when(groupMemberRepository.findByGroupId("g1")).thenReturn(List.of(theLeavingCreator));

        userService.deleteUser("1");

        verify(groupInvitationRepository).deleteByGroupId("g1");
        verify(groupMemberRepository).deleteByGroupId("g1");
        verify(groupRepository).deleteById("g1");
    }
}