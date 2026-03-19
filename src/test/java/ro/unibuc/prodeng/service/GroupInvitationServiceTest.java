package ro.unibuc.prodeng.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.UnauthorizedException;
import ro.unibuc.prodeng.model.GroupEntity;
import ro.unibuc.prodeng.model.GroupInvitationEntity;
import ro.unibuc.prodeng.model.GroupMemberEntity;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.GroupInvitationRepository;
import ro.unibuc.prodeng.repository.GroupRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.CreateInviteRequest;
import ro.unibuc.prodeng.response.GroupInvitationResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupInvitationServiceTest {

    @Mock private GroupRepository groupRepository;
    @Mock private GroupInvitationRepository invitationRepository;
    @Mock private UserRepository userRepository;
    @Mock private GroupMemberService groupMemberService;
    @Mock private NotificationService notificationService;

    @InjectMocks private GroupInvitationService invitationService;

    private GroupEntity group;
    private GroupInvitationEntity invitation;

    @BeforeEach
    void setUp() {
        group = new GroupEntity("g1", "Test Group", "Desc", "creator1", LocalDateTime.now());
        invitation = new GroupInvitationEntity("inv1", "g1", "creator1", "invitee1", LocalDateTime.now());
    }

    @Test
    void testCreateInvitation_validRequest_returnsInvitationResponse() {
        CreateInviteRequest request = new CreateInviteRequest("invitee1");
        when(groupRepository.findById("g1")).thenReturn(Optional.of(group));
        when(userRepository.existsById("invitee1")).thenReturn(true);
        when(invitationRepository.existsByGroupIdAndInviteeId("g1", "invitee1")).thenReturn(false);
        when(invitationRepository.save(any(GroupInvitationEntity.class))).thenReturn(invitation);

        GroupInvitationResponse response = invitationService.createInvitation("creator1", "g1", request);

        assertEquals("inv1", response.id());
        verify(notificationService).createNotification(eq("invitee1"), contains("invited"), eq("creator1"));
    }

    @Test
    void testCreateInvitation_notCreator_throwsUnauthorizedException() {
        CreateInviteRequest request = new CreateInviteRequest("invitee1");
        when(groupRepository.findById("g1")).thenReturn(Optional.of(group));

        assertThrows(UnauthorizedException.class, () -> invitationService.createInvitation("random_guy", "g1", request));
    }

    @Test
    void testGetMyInvitations_existingUser_returnsInvitationList() {
        when(invitationRepository.findByInviteeId("invitee1")).thenReturn(List.of(invitation));
        
        List<GroupInvitationResponse> responses = invitationService.getMyInvitations("invitee1");
        
        assertEquals(1, responses.size());
    }

    @Test
    void testAcceptInvitation_validInvitation_addsMemberAndDeletesInvite() {
        when(invitationRepository.findById("inv1")).thenReturn(Optional.of(invitation));
        when(groupRepository.findById("g1")).thenReturn(Optional.of(group));
        UserEntity inviteeUser = new UserEntity("invitee1", "Invitee", "i@t.com", "hash", "bio", "url", LocalDateTime.now(), false);
        when(userRepository.findById("invitee1")).thenReturn(Optional.of(inviteeUser));

        invitationService.acceptInvitation("invitee1", "inv1");

        verify(groupMemberService).addMemberToGroup("g1", "invitee1", GroupMemberEntity.Role.MEMBER);
        verify(invitationRepository).deleteById("inv1");
        verify(notificationService).createNotification(eq("creator1"), contains("joined"), eq("invitee1"));
    }

    @Test
    void testAcceptInvitation_wrongUser_throwsUnauthorizedException() {
        when(invitationRepository.findById("inv1")).thenReturn(Optional.of(invitation));

        assertThrows(UnauthorizedException.class, () -> invitationService.acceptInvitation("hacker", "inv1"));
    }

    @Test
    void testDeclineInvitation_validInvitation_deletesInvite() {
        when(invitationRepository.findById("inv1")).thenReturn(Optional.of(invitation));

        invitationService.declineInvitation("invitee1", "inv1");

        verify(invitationRepository).deleteById("inv1");
        verifyNoInteractions(groupMemberService);
    }

    @Test
    void testCreateInvitation_userNotFound_throwsEntityNotFoundException() {
        CreateInviteRequest request = new CreateInviteRequest("2");
        when(groupRepository.findById("g1")).thenReturn(Optional.of(group));
        when(userRepository.existsById("2")).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> invitationService.createInvitation("creator1", "g1", request));
    }

    @Test
    void testCreateInvitation_alreadyInvited_throwsIllegalArgumentException() {
        CreateInviteRequest request = new CreateInviteRequest("2");
        when(groupRepository.findById("g1")).thenReturn(Optional.of(group));
        when(userRepository.existsById("2")).thenReturn(true);
        when(invitationRepository.existsByGroupIdAndInviteeId("g1", "2")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> invitationService.createInvitation("creator1", "g1", request));
    }

    @Test
    void testAcceptInvitation_invitationNotFound_throwsEntityNotFoundException() {
        when(invitationRepository.findById("inv1")).thenReturn(Optional.empty());
        
        assertThrows(EntityNotFoundException.class, () -> invitationService.acceptInvitation("1", "inv1"));
    }
}