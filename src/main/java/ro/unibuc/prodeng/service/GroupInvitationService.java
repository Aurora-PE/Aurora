package ro.unibuc.prodeng.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.UnauthorizedException;
import ro.unibuc.prodeng.model.GroupEntity;
import ro.unibuc.prodeng.model.GroupInvitationEntity;
import ro.unibuc.prodeng.model.GroupMemberEntity;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.GroupInvitationRepository;
import ro.unibuc.prodeng.repository.GroupMemberRepository;
import ro.unibuc.prodeng.repository.GroupRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.CreateInviteRequest;
import ro.unibuc.prodeng.response.GroupInvitationResponse;

@Service
public class GroupInvitationService {
   private final GroupRepository groupRepository;
    private final GroupInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final GroupMemberService groupMemberService;
    private final NotificationService notificationService;

    public GroupInvitationService(GroupRepository groupRepository, GroupInvitationRepository invitationRepository, UserRepository userRepository, GroupMemberService groupMemberService, NotificationService notificationService) {
        this.groupRepository = groupRepository;
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.groupMemberService = groupMemberService;
        this.notificationService = notificationService;
    }

    public GroupInvitationResponse createInvitation(String requesterId, String groupId, CreateInviteRequest request) {
        GroupEntity group = groupRepository.findById(groupId)
            .orElseThrow(() -> new EntityNotFoundException("Group " + groupId));

        if (!group.creatorId().equals(requesterId)) {
            throw new UnauthorizedException("Only the group creator can send invitations.");
        }

        if (!userRepository.existsById(request.inviteeId())) {
            throw new EntityNotFoundException("User "+ request.inviteeId());
        }

        if (invitationRepository.existsByGroupIdAndInviteeId(groupId, request.inviteeId())) {
            throw new IllegalArgumentException("User already has a pending invitation to this group.");
        }

        GroupInvitationEntity invitation = new GroupInvitationEntity(groupId, requesterId, request.inviteeId());

        notificationService.createNotification(request.inviteeId(), "You have been invited to group " + group.name(), requesterId);

        return toResponse(invitationRepository.save(invitation));
    }

    public List<GroupInvitationResponse> getMyInvitations(String requesterId) {
        return invitationRepository.findByInviteeId(requesterId)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

   public void acceptInvitation(String requesterId, String invitationId) {
        GroupInvitationEntity inv = getAndValidateInvitation(requesterId, invitationId);
        groupMemberService.addMemberToGroup(inv.groupId(), requesterId, GroupMemberEntity.Role.MEMBER);
        
        invitationRepository.deleteById(inv.id());

        GroupEntity group = groupRepository.findById(inv.groupId())
            .orElseThrow(() -> new EntityNotFoundException("Group " + inv.groupId()));

        UserEntity requesterUser = userRepository.findById(requesterId).get();

        notificationService.createNotification(group.creatorId(), "User " + requesterUser.username() + " has joined the group " + group.name(), requesterId);
    }

    public void declineInvitation(String requesterId, String invitationId) {
        GroupInvitationEntity inv = getAndValidateInvitation(requesterId, invitationId);
        invitationRepository.deleteById(inv.id());
    }

    private GroupInvitationEntity getAndValidateInvitation(String requesterId, String invitationId) {
        GroupInvitationEntity inv = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new EntityNotFoundException("Invitation " + invitationId));

        if (!inv.inviteeId().equals(requesterId)) {
            throw new UnauthorizedException("You can only respond to your own invitations.");
        }
        return inv;
    }

    private GroupInvitationResponse toResponse(GroupInvitationEntity inv) {
        return new GroupInvitationResponse(inv.id(), inv.groupId(), inv.inviterId(), inv.inviteeId(), inv.createdAt());
    }
}
