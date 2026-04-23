package ro.unibuc.prodeng.controller;
import jakarta.validation.Valid;
import java.util.List;
import ro.unibuc.prodeng.service.MetricsService;
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
    private final MetricsService metricsService;

    public GroupController(GroupService groupService, MetricsService metricsService) {
        this.groupService = groupService;
        this.metricsService = metricsService;
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateGroupRequest request) {
        try {
            String requesterId = JwtUtil.extractRequesterId(authHeader);
            GroupResponse response = groupService.createGroup(requesterId, request);
            metricsService.recordGroupCreated();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            metricsService.recordError();
            throw e;
        }
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