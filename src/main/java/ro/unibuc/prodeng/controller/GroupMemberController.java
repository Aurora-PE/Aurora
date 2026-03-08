package ro.unibuc.prodeng.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.prodeng.request.UpdateRoleRequest;
import ro.unibuc.prodeng.response.GroupMemberResponse;
import ro.unibuc.prodeng.service.GroupMemberService;
import ro.unibuc.prodeng.util.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/members")
public class GroupMemberController {

    private final GroupMemberService groupMemberService;

    public GroupMemberController(GroupMemberService groupMemberService) {
        this.groupMemberService = groupMemberService;
    }

    @GetMapping
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String groupId) {
        String requesterId = JwtUtil.extractRequesterId(authHeader);
        return ResponseEntity.ok(groupMemberService.getGroupMembers(requesterId, groupId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> kickMember(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String groupId,
            @PathVariable String userId) {
        String requesterId = JwtUtil.extractRequesterId(authHeader);
        groupMemberService.kickMember(requesterId, groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<Void> changeMemberRole(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String groupId,
            @PathVariable String userId,
            @Valid @RequestBody UpdateRoleRequest request) {
        String requesterId = JwtUtil.extractRequesterId(authHeader);
        groupMemberService.changeMemberRole(requesterId, groupId, userId, request.role());
        return ResponseEntity.ok().build();
    }
}