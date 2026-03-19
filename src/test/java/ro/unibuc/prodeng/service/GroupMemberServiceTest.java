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
import ro.unibuc.prodeng.model.GroupMemberEntity;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.GroupMemberRepository;
import ro.unibuc.prodeng.repository.GroupRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.response.GroupMemberResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupMemberServiceTest {

    @Mock private GroupMemberRepository groupMemberRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private GroupMemberService groupMemberService;

    private GroupEntity group;
    private UserEntity creatorUser;
    private UserEntity regularUser;

    @BeforeEach
    void setUp() {
        group = new GroupEntity("g1", "Test Group", "Desc", "creator1", LocalDateTime.now());
        creatorUser = new UserEntity("creator1", "creator", "c@t.com", "hash", "bio", "url", LocalDateTime.now(), false);
        regularUser = new UserEntity("user1", "user", "u@t.com", "hash", "bio", "url", LocalDateTime.now(), false);
    }

    @Test
    void testAddMemberToGroup_newMember_savesGroupMemberEntity() {
        when(groupMemberRepository.existsByGroupIdAndUserId("g1", "user1")).thenReturn(false);
        
        groupMemberService.addMemberToGroup("g1", "user1", GroupMemberEntity.Role.MEMBER);
        
        verify(groupMemberRepository).save(any(GroupMemberEntity.class));
    }

    @Test
    void testGetGroupMembers_validGroup_returnsSortedMembers() {
        when(groupRepository.findById("g1")).thenReturn(Optional.of(group));
        when(userRepository.findById("creator1")).thenReturn(Optional.of(creatorUser));
        when(userRepository.findById("user1")).thenReturn(Optional.of(regularUser));
        GroupMemberEntity member = new GroupMemberEntity("m1", "g1", "user1", GroupMemberEntity.Role.ADMIN, LocalDateTime.now());
        when(groupMemberRepository.findByGroupId("g1")).thenReturn(List.of(member));

        List<GroupMemberResponse> members = groupMemberService.getGroupMembers("creator1", "g1");

        assertEquals(2, members.size());
        assertEquals("CREATOR", members.get(0).role()); 
        assertEquals("ADMIN", members.get(1).role());
    }

    @Test
    void testGetGroupMembers_notAMember_throwsUnauthorizedException() {
        when(groupRepository.findById("g1")).thenReturn(Optional.of(group));
        when(groupMemberRepository.existsByGroupIdAndUserId("g1", "hacker")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> groupMemberService.getGroupMembers("hacker", "g1"));
    }

    @Test
    void testKickMember_validRequest_deletesMemberAndNotifies() {
        when(groupRepository.findById("g1")).thenReturn(Optional.of(group));
        GroupMemberEntity targetMember = new GroupMemberEntity("m1", "g1", "user1", GroupMemberEntity.Role.MEMBER, LocalDateTime.now());
        when(groupMemberRepository.findByGroupIdAndUserId("g1", "user1")).thenReturn(Optional.of(targetMember));

        groupMemberService.kickMember("creator1", "g1", "user1");

        verify(notificationService).createNotification(eq("user1"), contains("kicked"), eq("creator1"));
        verify(groupMemberRepository).delete(targetMember);
    }

    @Test
    void testKickMember_adminKickingAdmin_throwsUnauthorizedException() {
        GroupEntity group2 = new GroupEntity("g2", "Group 2", "Desc", "creator2", LocalDateTime.now());
        when(groupRepository.findById("g2")).thenReturn(Optional.of(group2));
        GroupMemberEntity requesterAdmin = new GroupMemberEntity("m1", "g2", "admin1", GroupMemberEntity.Role.ADMIN, LocalDateTime.now());
        when(groupMemberRepository.findByGroupIdAndUserId("g2", "admin1")).thenReturn(Optional.of(requesterAdmin));
        GroupMemberEntity targetAdmin = new GroupMemberEntity("m2", "g2", "admin2", GroupMemberEntity.Role.ADMIN, LocalDateTime.now());
        when(groupMemberRepository.findByGroupIdAndUserId("g2", "admin2")).thenReturn(Optional.of(targetAdmin));

        assertThrows(UnauthorizedException.class, () -> groupMemberService.kickMember("admin1", "g2", "admin2"));
    }

    @Test
    void testChangeMemberRole_validRequest_updatesRole() {
        when(groupRepository.findById("g1")).thenReturn(Optional.of(group));
        GroupMemberEntity member = new GroupMemberEntity("m1", "g1", "user1", GroupMemberEntity.Role.MEMBER, LocalDateTime.now());
        when(groupMemberRepository.findByGroupIdAndUserId("g1", "user1")).thenReturn(Optional.of(member));

        groupMemberService.changeMemberRole("creator1", "g1", "user1", "ADMIN");

        verify(notificationService).createNotification(eq("user1"), contains("ADMIN"), eq("creator1"));
        verify(groupMemberRepository).save(argThat(m -> m.role() == GroupMemberEntity.Role.ADMIN));
    }

    @Test
    void testChangeMemberRole_notCreator_throwsUnauthorizedException() {
        when(groupRepository.findById("g1")).thenReturn(Optional.of(group));
        
        assertThrows(UnauthorizedException.class, () -> groupMemberService.changeMemberRole("admin1", "g1", "user1", "ADMIN"));
    }

    @Test
    void testKickMember_memberNotFound_throwsEntityNotFoundException() {
        when(groupRepository.findById("g1")).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserId("g1", "user1")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> groupMemberService.kickMember("creator1", "g1", "user1"));
    }

    @Test
    void testChangeMemberRole_targetIsCreator_throwsIllegalArgumentException() {
        when(groupRepository.findById("g1")).thenReturn(Optional.of(group));

        assertThrows(IllegalArgumentException.class, () -> groupMemberService.changeMemberRole("creator1", "g1", "creator1", "ADMIN"));
    }

    @Test
    void testChangeMemberRole_invalidRoleString_throwsIllegalArgumentException() {
        when(groupRepository.findById("g1")).thenReturn(Optional.of(group));
        GroupMemberEntity targetMember = new GroupMemberEntity("m1", "g1", "user1", GroupMemberEntity.Role.MEMBER, LocalDateTime.now());
        when(groupMemberRepository.findByGroupIdAndUserId("g1", "user1")).thenReturn(Optional.of(targetMember));

        assertThrows(IllegalArgumentException.class, () -> groupMemberService.changeMemberRole("creator1", "g1", "user1", "SUPER_ADMIN"));
    }
}