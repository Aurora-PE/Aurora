package ro.unibuc.prodeng.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import ro.unibuc.prodeng.request.CreateInviteRequest;
import ro.unibuc.prodeng.response.GroupInvitationResponse;
import ro.unibuc.prodeng.service.GroupInvitationService;
import ro.unibuc.prodeng.util.JwtUtil;

@RestController
@RequestMapping("/api/groups")
public class GroupInvitationController {
    private final GroupInvitationService groupInviteService;

    public GroupInvitationController(GroupInvitationService groupInviteService) {
        this.groupInviteService = groupInviteService;
    }

    @PostMapping("/{groupId}/invitations")
    public ResponseEntity<GroupInvitationResponse> createInvitation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String groupId,
            @Valid @RequestBody CreateInviteRequest request) {
        String requesterId = JwtUtil.extractRequesterId(authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(groupInviteService.createInvitation(requesterId, groupId, request));
    }

    @GetMapping("/invitations")
    public ResponseEntity<List<GroupInvitationResponse>> getMyInvitations(
            @RequestHeader("Authorization") String authHeader) {
        String requesterId = JwtUtil.extractRequesterId(authHeader);
        return ResponseEntity.ok(groupInviteService.getMyInvitations(requesterId));
    }

    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<Void> acceptInvitation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String invitationId) {
        String requesterId = JwtUtil.extractRequesterId(authHeader);
        groupInviteService.acceptInvitation(requesterId, invitationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/invitations/{invitationId}/decline")
    public ResponseEntity<Void> declineInvitation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String invitationId) {
        String requesterId = JwtUtil.extractRequesterId(authHeader);
        groupInviteService.declineInvitation(requesterId, invitationId);
        return ResponseEntity.ok().build();
    }
}
