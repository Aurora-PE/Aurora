package ro.unibuc.prodeng.service;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MetricsService {
    private final Counter usersCreatedCounter;
    private final Counter groupsCreatedCounter;
    private final Counter notificationsCreatedCounter;

    private final Counter followsTotalCounter;
    private final Counter membersKickedCounter;

    private final Counter domainErrorsCounter;
    private final Counter notificationsErrorCounter;
    private final Counter messagesCreatedCounter;

    private final AtomicInteger pendingInvitations;
    private final AtomicInteger unreadNotifications;

    private final Timer userLoginTimer;
    private final Timer notificationReadTimer;

    private final Counter postsCreatedCounter;
    private final Counter commentsCreatedCounter;
    private final Counter likesGivenCounter;
    private final Counter likesRemovedCounter;
    private final Timer postCreationTimer;

    public MetricsService(MeterRegistry registry) {
        this.usersCreatedCounter = Counter.builder("app_users_created_total")
            .description("Total number of users created").register(registry);
            
        this.groupsCreatedCounter = Counter.builder("app_groups_created_total")
            .description("Total number of groups created").register(registry);
            
        this.followsTotalCounter = Counter.builder("app_follows_total")
            .description("Total number of follow actions").register(registry);
            
        this.membersKickedCounter = Counter.builder("app_group_members_kicked_total")
            .description("Total group members kicked").register(registry);
            
        this.domainErrorsCounter = Counter.builder("app_domain_errors_total")
            .description("Total errors related to users& groups").register(registry);

        this.pendingInvitations = registry.gauge("app_pending_group_invitations", new AtomicInteger(0));

        this.userLoginTimer = Timer.builder("app_user_login_duration_seconds")
            .description("Time taken to process user login").register(registry);

        this.notificationsCreatedCounter = Counter.builder("app_notifications_created_total")
            .description("Total number of notifications created").register(registry);
        
        this.notificationReadTimer = Timer.builder("app_notification_read_duration_seconds")
            .description("Time taken to process notification reading").register(registry);

        this.notificationsErrorCounter = Counter.builder("app_notifications_errors_total")
            .description("Total errors related to notifications").register(registry);

        this.messagesCreatedCounter = Counter.builder("app_messagess_total")
            .description("Total number of messagess").register(registry);

        this.unreadNotifications = registry.gauge("app_unread_notifications", new AtomicInteger(0));

        this.postsCreatedCounter = Counter.builder("app_posts_created_total")
            .description("Total number of posts created").register(registry);

        this.commentsCreatedCounter = Counter.builder("app_comments_created_total")
            .description("Total number of comments created").register(registry);

        this.likesGivenCounter = Counter.builder("app_likes_given_total")
            .description("Total number of likes given")
            .tag("target", "unknown")
            .register(registry);

        this.likesRemovedCounter = Counter.builder("app_likes_removed_total")
            .description("Total number of likes removed").register(registry);

        this.postCreationTimer = Timer.builder("app_post_creation_duration_seconds")
            .description("Time taken to create a post").register(registry);
    }

    public void recordUserCreated() { usersCreatedCounter.increment(); }
    public void recordGroupCreated() { groupsCreatedCounter.increment(); }
    public void recordFollow() { followsTotalCounter.increment(); }
    public void recordMemberKicked() { membersKickedCounter.increment(); }
    public void recordError() { domainErrorsCounter.increment(); }
    public void recordNotificationsCreated() { notificationsCreatedCounter.increment(); }
    public void recordMessagesCreated() { messagesCreatedCounter.increment(); }
    public void recordNotificationError() { notificationsErrorCounter.increment(); }

    public void incrementPendingInvitations() { pendingInvitations.incrementAndGet(); }
    public void decrementPendingInvitations() { pendingInvitations.decrementAndGet(); }

    public void incrementUnreadNotifications() { unreadNotifications.incrementAndGet(); }
    public void decrementUnreadNotifications() { unreadNotifications.decrementAndGet(); }

    public Timer.Sample startTimer() { return Timer.start(); }
    public void stopLoginTimer(Timer.Sample sample) { sample.stop(userLoginTimer); }
    public void stopNotificationsReadTimer(Timer.Sample sample) { sample.stop(notificationReadTimer); }
    
    public void recordPostCreated() { postsCreatedCounter.increment(); }
    public void recordCommentCreated() { commentsCreatedCounter.increment(); }
    public void recordLikeGiven() { likesGivenCounter.increment(); }
    public void recordLikeRemoved() { likesRemovedCounter.increment(); }
    public void stopPostCreationTimer(Timer.Sample sample) { sample.stop(postCreationTimer); }
}