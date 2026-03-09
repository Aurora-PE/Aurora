package ro.unibuc.prodeng.service;

import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.UnauthorizedException;
import ro.unibuc.prodeng.model.GroupEntity;
import ro.unibuc.prodeng.model.GroupMemberEntity;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.GroupMemberRepository;
import ro.unibuc.prodeng.repository.GroupRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.response.GroupMemberResponse;
import java.util.*;
import java.util.stream.*;

@Service
public class GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public GroupMemberService(GroupMemberRepository groupMemberRepository, GroupRepository groupRepository, UserRepository userRepository, NotificationService notificationService) {
        this.groupMemberRepository = groupMemberRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public void addMemberToGroup(String groupId, String userId, GroupMemberEntity.Role role) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            groupMemberRepository.save(new GroupMemberEntity(groupId, userId, role));
        }
    }

   public List<GroupMemberResponse> getGroupMembers(String requesterId, String groupId) {
        checkGroupAccess(requesterId, groupId);

        GroupEntity group = groupRepository.findById(groupId)
            .orElseThrow(() -> new EntityNotFoundException("Group " + groupId));
        UserEntity creator = userRepository.findById(group.creatorId())
            .orElseThrow(() -> new EntityNotFoundException("User " + group.creatorId()));
        GroupMemberResponse creatorResponse = new GroupMemberResponse(
            creator.id(),
            creator.username(),
            creator.avatarUrl(),
            "CREATOR",
            group.createdAt()
        );
        Stream<GroupMemberResponse> memberStream = groupMemberRepository.findByGroupId(groupId).stream()
            .sorted(Comparator.comparing(GroupMemberEntity::role)
                                        .thenComparing(GroupMemberEntity::joinedAt))
            .map(member -> {
                UserEntity user = userRepository.findById(member.userId())
                    .orElseThrow(() -> new EntityNotFoundException("User " + member.userId()));
                return new GroupMemberResponse(
                    user.id(), 
                    user.username(), 
                    user.avatarUrl(), 
                    member.role().name(), 
                    member.joinedAt()
                );
            });

        return Stream.concat(Stream.of(creatorResponse), memberStream)
            .collect(Collectors.toList());
    }

    public void kickMember(String requesterId, String groupId, String targetUserId) {
        GroupEntity group = groupRepository.findById(groupId)
            .orElseThrow(() -> new EntityNotFoundException("Group " + groupId));

        verifyAdminOrCreator(requesterId, group);

        if (group.creatorId().equals(targetUserId)) {
            throw new IllegalArgumentException("Cannot kick the group creator.");
        }

        GroupMemberEntity member = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
            .orElseThrow(() -> new EntityNotFoundException("GroupMember "+ targetUserId));

        if (!group.creatorId().equals(requesterId) && member.role() == GroupMemberEntity.Role.ADMIN) {
            throw new UnauthorizedException("Admins cannot kick other admins.");
        }

        notificationService.createNotification(targetUserId, "You have been kicked from the group " + group.name(), requesterId);

        groupMemberRepository.delete(member);
    }

    public void changeMemberRole(String requesterId, String groupId, String targetUserId, String newRole) {
        GroupEntity group = groupRepository.findById(groupId)
            .orElseThrow(() -> new EntityNotFoundException("Group " + groupId));

        if (!group.creatorId().equals(requesterId)) {
            throw new UnauthorizedException("Only the group creator can change member roles.");
        }

        if (group.creatorId().equals(targetUserId)) {
            throw new IllegalArgumentException("Cannot change the group creator's role.");
        }

        GroupMemberEntity member = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
            .orElseThrow(() -> new EntityNotFoundException("GroupMember "+ targetUserId));

        GroupMemberEntity.Role roleEnum;
        try {
            roleEnum = GroupMemberEntity.Role.valueOf(newRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role. Must be ADMIN or MEMBER.");
        }

        GroupMemberEntity updatedMember = new GroupMemberEntity(
            member.id(), member.groupId(), member.userId(), roleEnum, member.joinedAt()
        );

        notificationService.createNotification(targetUserId, "You have been made " + roleEnum.toString() + " in group " + group.name(), requesterId);

        groupMemberRepository.save(updatedMember);
    }

    private void checkGroupAccess(String requesterId, String groupId) {
        GroupEntity group = groupRepository.findById(groupId)
            .orElseThrow(() -> new EntityNotFoundException("Group " + groupId));
        
        if (group.creatorId().equals(requesterId)) return;
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, requesterId)) {
            throw new UnauthorizedException("You are not a member of this group.");
        }
    }

    private void verifyAdminOrCreator(String requesterId, GroupEntity group) {
        if (group.creatorId().equals(requesterId)) return;

        GroupMemberEntity requesterMember = groupMemberRepository.findByGroupIdAndUserId(group.id(), requesterId)
            .orElseThrow(() -> new UnauthorizedException("You are not part of this group."));

        if (requesterMember.role() != GroupMemberEntity.Role.ADMIN) {
            throw new UnauthorizedException("You must be an ADMIN or the CREATOR to perform this action.");
        }
    }
}