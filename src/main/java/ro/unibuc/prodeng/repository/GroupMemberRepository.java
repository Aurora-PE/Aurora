package ro.unibuc.prodeng.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.prodeng.model.GroupMemberEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends MongoRepository<GroupMemberEntity, String> {
    List<GroupMemberEntity> findByGroupId(String groupId);
    List<GroupMemberEntity> findByUserId(String userId);
    Optional<GroupMemberEntity> findByGroupIdAndUserId(String groupId, String userId);
    boolean existsByGroupIdAndUserId(String groupId, String userId);
    void deleteByGroupIdAndUserId(String groupId, String userId);
    void deleteByGroupId(String groupId);
    void deleteByUserId(String userId);
}