package ro.unibuc.prodeng.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.prodeng.model.GroupEntity;

@Repository
public interface GroupRepository extends MongoRepository<GroupEntity, String> {
    Optional<GroupEntity> findByName(String name);
    List<GroupEntity> findByCreatorId(String creatorId);
}