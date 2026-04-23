package ro.unibuc.prodeng.controller;
import ro.unibuc.prodeng.service.MetricsService;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.prodeng.request.CreateUserRequest;
import ro.unibuc.prodeng.request.LoginRequest;
import ro.unibuc.prodeng.request.UpdateUserRequest;
import ro.unibuc.prodeng.response.LoginResponse;
import ro.unibuc.prodeng.response.UserResponse;
import ro.unibuc.prodeng.response.UserSummaryResponse;
import ro.unibuc.prodeng.service.UserService;
import ro.unibuc.prodeng.util.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final MetricsService metricsService;

    public UserController(UserService userService, MetricsService metricsService) {
        this.userService = userService;
        this.metricsService = metricsService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
        @Valid
        @RequestBody
        CreateUserRequest request
    ) {
        try {
            UserResponse response = userService.createUser(request);
            metricsService.recordUserCreated();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            metricsService.recordError();
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @Valid
        @RequestBody LoginRequest request
    ) {
       Timer.Sample timer = metricsService.startTimer();
        try {
            LoginResponse response = userService.login(request.email(), request.password());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            metricsService.recordError();
            throw e;
        } finally {
            metricsService.stopLoginTimer(timer);
        }
    }

    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> searchUsers(
        @RequestParam(required = false, defaultValue = "") String username
    ) {
        return ResponseEntity.ok(userService.searchUsersByUsername(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {
        String requesterId = JwtUtil.extractRequesterId(authHeader);
        return ResponseEntity.ok(userService.getUserById(requesterId, id));
    }

    @PutMapping("/self")
    public ResponseEntity<UserResponse> updateMyProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateUserRequest request) {
        String requesterId = JwtUtil.extractRequesterId(authHeader);
        return ResponseEntity.ok(userService.updateUser(requesterId, request));
    }

    @DeleteMapping("/self")
    public ResponseEntity<Void> deleteMyProfile(
            @RequestHeader("Authorization") String authHeader) {
        String requesterId = JwtUtil.extractRequesterId(authHeader);
        userService.deleteUser(requesterId);
        return ResponseEntity.noContent().build();
    }

}