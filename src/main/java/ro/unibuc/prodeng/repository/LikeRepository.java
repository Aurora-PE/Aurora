package ro.unibuc.prodeng.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;


import ro.unibuc.prodeng.model.LikeEntity;
import ro.unibuc.prodeng.model.LikeTargetTypeEnum;

public interface LikeRepository
    extends MongoRepository<LikeEntity, String> {

    Optional<LikeEntity> findByUserIdAndTargetIdAndTargetType(
        String userId, String targetId, LikeTargetTypeEnum targetType
    );

    void deleteAllByTargetIdAndTargetType(
        String targetId, LikeTargetTypeEnum targetType
    );
}