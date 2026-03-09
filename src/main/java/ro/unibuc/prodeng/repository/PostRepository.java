package ro.unibuc.prodeng.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ro.unibuc.prodeng.model.PostEntity;
import ro.unibuc.prodeng.model.VisibilityEnum;

public interface PostRepository extends MongoRepository<PostEntity, String> {

    List<PostEntity> findByAuthorIdOrderByLocalDateTimeDesc(String authorId);

    List<PostEntity> findByAuthorIdInAndVisibilityOrderByLocalDateTimeDesc(
        List<String> authorIds, VisibilityEnum visibility
    );

    List<PostEntity> findByAuthorIdInOrderByLocalDateTimeDesc(
        List<String> authorIds
    );
}