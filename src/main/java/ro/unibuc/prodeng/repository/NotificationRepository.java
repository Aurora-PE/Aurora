package ro.unibuc.prodeng.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.prodeng.model.NotificationEntity;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationEntity, String> {
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(String userId);
    List<NotificationEntity> findByUserIdAndReadFalseOrderByCreatedAtDesc(String userId);
    void deleteByUserId(String userId);
}