package ro.unibuc.prodeng.service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.UnauthorizedException;
import ro.unibuc.prodeng.model.GroupEntity;
import ro.unibuc.prodeng.model.GroupMemberEntity;
import ro.unibuc.prodeng.repository.GroupInvitationRepository;
import ro.unibuc.prodeng.repository.GroupMemberRepository;
import ro.unibuc.prodeng.repository.GroupRepository;
import ro.unibuc.prodeng.request.CreateGroupRequest;
import ro.unibuc.prodeng.response.GroupResponse;

@Service
public class GroupService {

  private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupInvitationRepository groupInvitationRepository;

    public GroupService(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository, GroupInvitationRepository groupInvitationRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupInvitationRepository = groupInvitationRepository;
    }

    public GroupResponse createGroup(String requesterId, CreateGroupRequest request) {
        if(groupRepository.findByName(request.name()).isPresent()){
            throw new IllegalArgumentException("There is already a group with this name!");
        }
        GroupEntity group = new GroupEntity(request.name(), request.description(), requesterId);
        return toResponse(groupRepository.save(group));
    }

    public GroupResponse getGroupById(String groupId) {
        GroupEntity group = groupRepository.findById(groupId)
            .orElseThrow(() -> new EntityNotFoundException("Group " + groupId));
        return toResponse(group);
    }

    public List<GroupResponse> getMyGroups(String requesterId) {
        List<GroupEntity> createdGroups = groupRepository.findByCreatorId(requesterId);

        List<String> memberGroupIds = groupMemberRepository.findByUserId(requesterId).stream()
            .map(GroupMemberEntity::groupId)
            .collect(Collectors.toList());

        List<GroupEntity> memberGroups = (List<GroupEntity>) groupRepository.findAllById(memberGroupIds);

        Set<String> uniqueGroupIds = Stream.concat(createdGroups.stream(), memberGroups.stream())
            .map(GroupEntity::id)
            .collect(Collectors.toSet());

        return ((List<GroupEntity>) groupRepository.findAllById(uniqueGroupIds)).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public void deleteGroup(String requesterId, String groupId) {
        GroupEntity group = groupRepository.findById(groupId)
            .orElseThrow(() -> new EntityNotFoundException("Group " + groupId));
        if (!group.creatorId().equals(requesterId)) {
            throw new UnauthorizedException("Only the group creator can delete this group.");
        }
        groupMemberRepository.deleteByGroupId(groupId);
        groupInvitationRepository.deleteByGroupId(groupId);
        groupRepository.deleteById(groupId);
    }

    private GroupResponse toResponse(GroupEntity group) {
        return new GroupResponse(group.id(), group.name(), group.description(), group.creatorId(), group.createdAt());
    }
}