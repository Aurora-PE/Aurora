package ro.unibuc.prodeng.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ro.unibuc.prodeng.model.CommentEntity;

public interface CommentRepository
    extends MongoRepository<CommentEntity, String> {

    List<CommentEntity> findByPostIdOrderByCreatedAtDesc(String postId);

    void deleteAllByPostId(String postId);
}