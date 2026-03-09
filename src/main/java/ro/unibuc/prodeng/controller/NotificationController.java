package ro.unibuc.prodeng.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.prodeng.response.NotificationResponse;
import ro.unibuc.prodeng.service.NotificationService;
import ro.unibuc.prodeng.util.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(
            @RequestHeader("Authorization") String authHeader) {

        String requesterId = JwtUtil.extractRequesterId(authHeader);

        return ResponseEntity.ok(
                notificationService.getUserNotifications(requesterId)
        );
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadUserNotifications(
            @RequestHeader("Authorization") String authHeader) {

        String requesterId = JwtUtil.extractRequesterId(authHeader);

        return ResponseEntity.ok(
                notificationService.getUnreadUserNotifications(requesterId)
        );
    }

    @PatchMapping("/{notifId}/read")
    public ResponseEntity<NotificationResponse> markNotificationRead(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String notifId) {

        String requesterId = JwtUtil.extractRequesterId(authHeader);

        return ResponseEntity.ok(
                notificationService.markNotificationRead(requesterId, notifId)
        );
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllNotificationsRead(
            @RequestHeader("Authorization") String authHeader) {

        String requesterId = JwtUtil.extractRequesterId(authHeader);

        notificationService.markAllNotificationsRead(requesterId);

        return ResponseEntity.noContent().build();
    }
}