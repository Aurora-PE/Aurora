package ro.unibuc.prodeng.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.UnauthorizedException;
import ro.unibuc.prodeng.model.GroupEntity;
import ro.unibuc.prodeng.repository.GroupRepository;
import ro.unibuc.prodeng.request.CreateGroupRequest;
import ro.unibuc.prodeng.response.GroupResponse;

@Service
public class GroupService {

    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
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
        return groupRepository.findByCreatorId(requesterId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public void deleteGroup(String requesterId, String groupId) {
        GroupEntity group = groupRepository.findById(groupId)
            .orElseThrow(() -> new EntityNotFoundException("Group " + groupId));
        if (!group.creatorId().equals(requesterId)) {
            throw new UnauthorizedException("Only the group creator can delete this group.");
        }
        
        groupRepository.deleteById(groupId);
    }

    private GroupResponse toResponse(GroupEntity group) {
        return new GroupResponse(group.id(), group.name(), group.description(), group.creatorId(), group.createdAt());
    }
}