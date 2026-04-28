package ro.unibuc.prodeng.controller;

import io.micrometer.core.instrument.Timer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.prodeng.response.NotificationResponse;
import ro.unibuc.prodeng.service.MetricsService;
import ro.unibuc.prodeng.service.NotificationService;
import ro.unibuc.prodeng.util.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final MetricsService metricsService;

    public NotificationController(NotificationService notificationService, MetricsService metricsService) {
        this.notificationService = notificationService;
        this.metricsService = metricsService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(
            @RequestHeader("Authorization") String authHeader) {

        Timer.Sample sample = metricsService.startTimer();
        try {
            String requesterId = JwtUtil.extractRequesterId(authHeader);

            return ResponseEntity.ok(
                    notificationService.getUserNotifications(requesterId)
            );
        } catch (RuntimeException e) {
            metricsService.recordNotificationError();
            throw e;
        } finally {
            metricsService.stopNotificationsReadTimer(sample);
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadUserNotifications(
            @RequestHeader("Authorization") String authHeader) {

        Timer.Sample sample = metricsService.startTimer();
        try {
            String requesterId = JwtUtil.extractRequesterId(authHeader);

            return ResponseEntity.ok(
                    notificationService.getUnreadUserNotifications(requesterId)
            );
        } catch (RuntimeException e) {
            metricsService.recordNotificationError();
            throw e;
        } finally {
            metricsService.stopNotificationsReadTimer(sample);
        }
    }

    @PatchMapping("/{notifId}/read")
    public ResponseEntity<NotificationResponse> markNotificationRead(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String notifId) {

        Timer.Sample sample = metricsService.startTimer();
        try {
            String requesterId = JwtUtil.extractRequesterId(authHeader);

            NotificationResponse response =
                    notificationService.markNotificationRead(requesterId, notifId);

            metricsService.decrementUnreadNotifications();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            metricsService.recordNotificationError();
            throw e;
        } finally {
            metricsService.stopNotificationsReadTimer(sample);
        }
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllNotificationsRead(
            @RequestHeader("Authorization") String authHeader) {

        Timer.Sample sample = metricsService.startTimer();
        try {
            String requesterId = JwtUtil.extractRequesterId(authHeader);

            int unreadNotifications = notificationService.markAllNotificationsRead(requesterId);

            for (int i = 0; i<unreadNotifications; i++){
                metricsService.decrementUnreadNotifications();
            }

            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            metricsService.recordNotificationError();
            throw e;
        } finally {
            metricsService.stopNotificationsReadTimer(sample);
        }
    }
}