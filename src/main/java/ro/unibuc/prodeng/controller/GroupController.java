package ro.unibuc.prodeng.controller;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.prodeng.request.CreateGroupRequest;
import ro.unibuc.prodeng.response.GroupResponse;
import ro.unibuc.prodeng.service.GroupService;
import ro.unibuc.prodeng.util.JwtUtil;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateGroupRequest request) {
        String requesterId = JwtUtil.extractRequesterId(authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(requesterId, request));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>>getMyGroups(
            @RequestHeader("Authorization") String authHeader) {
        String requesterId = JwtUtil.extractRequesterId(authHeader);
        return ResponseEntity.ok(groupService.getMyGroups(requesterId));
    } 

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroupById(@PathVariable String id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {
        String requesterId = JwtUtil.extractRequesterId(authHeader);
        groupService.deleteGroup(requesterId, id);
        return ResponseEntity.noContent().build();
    }

}