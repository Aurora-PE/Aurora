package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.UnauthorizedException;
import ro.unibuc.prodeng.model.NotificationEntity;
import ro.unibuc.prodeng.repository.NotificationRepository;
import ro.unibuc.prodeng.response.NotificationResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void testCreateNotification_validData_savesNotification() {

        notificationService.createNotification("user1", "Test message", "user2");

        verify(notificationRepository).save(any(NotificationEntity.class));
    }

    @Test
    void testGetUserNotifications_existingNotifications_returnsList() {

        NotificationEntity notif = new NotificationEntity(
                "1", "user1", "msg", "user2", false, LocalDateTime.now()
        );

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc("user1"))
                .thenReturn(List.of(notif));

        List<NotificationResponse> result =
                notificationService.getUserNotifications("user1");

        assertEquals(1, result.size());
        assertEquals("msg", result.get(0).message());
    }

    @Test
    void testGetUserNotifications_noNotifications_returnsEmptyList() {

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc("user1"))
                .thenReturn(List.of());

        List<NotificationResponse> result =
                notificationService.getUserNotifications("user1");

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetUnreadUserNotifications_existingUnread_returnsList() {

        NotificationEntity notif1 = new NotificationEntity(
                "1", "user1", "msg", "user2", false, LocalDateTime.now()
        );
        
        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc("user1"))
                .thenReturn(List.of(notif1));

        List<NotificationResponse> result =
                notificationService.getUnreadUserNotifications("user1");

        assertEquals(1, result.size());
        assertFalse(result.get(0).read());
    }

    @Test
    void testMarkNotificationRead_validRequest_updatesNotification() {

        NotificationEntity notif = new NotificationEntity(
                "1", "user1", "msg", "user2", false, LocalDateTime.now()
        );

        when(notificationRepository.findById("1"))
                .thenReturn(Optional.of(notif));

        when(notificationRepository.save(any(NotificationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse result =
                notificationService.markNotificationRead("user1", "1");

        assertTrue(result.read());
    }

    @Test
    void testMarkNotificationRead_notificationNotFound_throwsEntityNotFoundException() {

        when(notificationRepository.findById("1"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> notificationService.markNotificationRead("user1", "1"));
    }

    @Test
    void testMarkNotificationRead_wrongUser_throwsUnauthorizedException() {

        NotificationEntity notif = new NotificationEntity(
                "1", "user2", "msg", "user1", false, LocalDateTime.now()
        );

        when(notificationRepository.findById("1"))
                .thenReturn(Optional.of(notif));

        assertThrows(UnauthorizedException.class,
                () -> notificationService.markNotificationRead("user1", "1"));
    }

    @Test
    void testMarkAllNotificationsRead_existingUnread_updatesAll() {

        NotificationEntity notif1 = new NotificationEntity(
                "1", "user1", "msg1", "user2", false, LocalDateTime.now()
        );

        NotificationEntity notif2 = new NotificationEntity(
                "2", "user1", "msg2", "user3", false, LocalDateTime.now()
        );

        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc("user1"))
                .thenReturn(List.of(notif1, notif2));

        notificationService.markAllNotificationsRead("user1");

        verify(notificationRepository).saveAll(any());
    }

    @Test
    void testMarkAllNotificationsRead_noUnread_stillCallsSaveAll() {

        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc("user1"))
                .thenReturn(List.of());

        notificationService.markAllNotificationsRead("user1");

        verify(notificationRepository).saveAll(any());
    }
}