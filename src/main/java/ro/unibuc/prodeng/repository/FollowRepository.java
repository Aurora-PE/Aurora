package ro.unibuc.prodeng.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.prodeng.model.FollowEntity;
import java.util.List;

@Repository
public interface FollowRepository extends MongoRepository<FollowEntity, String> {
    
    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);
    void deleteByFollowerIdAndFollowingId(String followerId, String followingId);
    List<FollowEntity> findByFollowingId(String followingId);
    List<FollowEntity> findByFollowerId(String followerId);
}