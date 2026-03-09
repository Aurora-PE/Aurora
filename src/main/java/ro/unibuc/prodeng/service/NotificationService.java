package ro.unibuc.prodeng.service;

import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.model.NotificationEntity;
import ro.unibuc.prodeng.repository.NotificationRepository;
import ro.unibuc.prodeng.response.NotificationResponse;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.UnauthorizedException;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationResponse> getUserNotifications(String userId) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<NotificationResponse> getUnreadUserNotifications(String userId) {

        return notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void createNotification(String userId, String message, String sourceUserId) {

        NotificationEntity notification = new NotificationEntity(
                userId,
                message,
                sourceUserId
        );

        notificationRepository.save(notification);
    }

    public NotificationResponse markNotificationRead(String requesterId, String notifId) {
        NotificationEntity notification = notificationRepository.findById(notifId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Notification " + notifId));

        if (!notification.userId().equals(requesterId)) {
                throw new UnauthorizedException("Cannot modify another user's notification.");
        }

        NotificationEntity updatedNotification = new NotificationEntity(
                notification.id(),
                notification.userId(),
                notification.message(),
                notification.sourceUserId(),
                true,
                notification.createdAt()
        );

        NotificationEntity saved = notificationRepository.save(updatedNotification);

        return toResponse(saved);
    }

    public void markAllNotificationsRead(String userId) {

        List<NotificationEntity> notifications =
                notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);

        List<NotificationEntity> updatedNotifications = notifications.stream()
                .map(n -> new NotificationEntity(
                        n.id(),
                        n.userId(),
                        n.message(),
                        n.sourceUserId(),
                        true,
                        n.createdAt()
                ))
                .toList();

        notificationRepository.saveAll(updatedNotifications);
    }

    private NotificationResponse toResponse(NotificationEntity entity) {

        return new NotificationResponse(
                entity.id(),
                entity.userId(),
                entity.message(),
                entity.sourceUserId(),
                entity.read(),
                entity.createdAt()
        );
    }
}
