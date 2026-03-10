package ro.unibuc.prodeng.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ro.unibuc.prodeng.model.Like;
import ro.unibuc.prodeng.model.LikeTargetTypeEnum;

public interface LikeRepository
    extends MongoRepository<Like, String> {

    Optional<Like> findByUserIdAndTargetIdAndTargetType(
        String userId, String targetId, LikeTargetTypeEnum targetType
    );

    void deleteAllByTargetIdAndTargetType(
        String targetId, LikeTargetTypeEnum targetType
    );
}