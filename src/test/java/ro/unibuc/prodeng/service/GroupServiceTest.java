package ro.unibuc.prodeng.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.prodeng.exception.UnauthorizedException;
import ro.unibuc.prodeng.model.GroupEntity;
import ro.unibuc.prodeng.model.GroupMemberEntity;
import ro.unibuc.prodeng.repository.GroupInvitationRepository;
import ro.unibuc.prodeng.repository.GroupMemberRepository;
import ro.unibuc.prodeng.repository.GroupRepository;
import ro.unibuc.prodeng.request.CreateGroupRequest;
import ro.unibuc.prodeng.response.GroupResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock private GroupRepository groupRepository;
    @Mock private GroupMemberRepository groupMemberRepository;
    @Mock private GroupInvitationRepository groupInvitationRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private GroupService groupService;

    private GroupEntity testGroup;

    @BeforeEach
    void setUp() {
        testGroup = new GroupEntity("g1", "Test Group", "Desc", "creator1", LocalDateTime.now());
    }

    @Test
    void testCreateGroup_validRequest_returnsGroupResponse() {
        CreateGroupRequest request = new CreateGroupRequest("Test Group", "Desc");
        when(groupRepository.findByName(request.name())).thenReturn(Optional.empty());
        when(groupRepository.save(any(GroupEntity.class))).thenReturn(testGroup);

        GroupResponse response = groupService.createGroup("creator1", request);
        
        assertEquals("Test Group", response.name());
    }

    @Test
    void testCreateGroup_duplicateName_throwsIllegalArgumentException() {
        CreateGroupRequest request = new CreateGroupRequest("Test Group", "Desc");
        when(groupRepository.findByName("Test Group")).thenReturn(Optional.of(testGroup));

        assertThrows(IllegalArgumentException.class, () -> groupService.createGroup("creator1", request));
    }

    @Test
    void testGetMyGroups_existingUser_returnsCombinedGroupList() {
        GroupEntity joinedGroup = new GroupEntity("g2", "Joined Group", "Desc", "creator2", LocalDateTime.now());
        GroupMemberEntity memberEntity = new GroupMemberEntity("m1", "g2", "creator1", GroupMemberEntity.Role.MEMBER, LocalDateTime.now());

        when(groupRepository.findByCreatorId("creator1")).thenReturn(List.of(testGroup));
        when(groupMemberRepository.findByUserId("creator1")).thenReturn(List.of(memberEntity));
        when(groupRepository.findAllById(anyList())).thenReturn(List.of(joinedGroup)); 
        when(groupRepository.findAllById(anySet())).thenReturn(List.of(testGroup, joinedGroup));

        List<GroupResponse> myGroups = groupService.getMyGroups("creator1");
        
        assertEquals(2, myGroups.size());
    }

    @Test
    void testDeleteGroup_isCreator_deletesGroupAndCascades() {
        when(groupRepository.findById("g1")).thenReturn(Optional.of(testGroup));
        GroupMemberEntity otherMember = new GroupMemberEntity("m1", "g1", "user2", GroupMemberEntity.Role.MEMBER, LocalDateTime.now());
        when(groupMemberRepository.findByGroupId("g1")).thenReturn(List.of(otherMember));

        groupService.deleteGroup("creator1", "g1");

        verify(notificationService).createNotification(eq("user2"), anyString(), eq("creator1"));
        verify(groupMemberRepository).deleteByGroupId("g1");
        verify(groupInvitationRepository).deleteByGroupId("g1");
        verify(groupRepository).deleteById("g1");
    }

    @Test
    void testDeleteGroup_notCreator_throwsUnauthorizedException() {
        when(groupRepository.findById("g1")).thenReturn(Optional.of(testGroup));

        assertThrows(UnauthorizedException.class, () -> groupService.deleteGroup("hacker_user", "g1"));
    }
}