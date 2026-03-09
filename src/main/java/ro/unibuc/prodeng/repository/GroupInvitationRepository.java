package ro.unibuc.prodeng.repository;
import java.util.*;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.prodeng.model.GroupInvitationEntity;

@Repository
public interface GroupInvitationRepository extends MongoRepository<GroupInvitationEntity, String>{
   boolean existsByGroupIdAndInviteeId(String groupId, String inviteeId);
    List<GroupInvitationEntity> findByInviteeId(String inviteeId);
    void deleteByGroupId(String groupId);
    void deleteByInviterId(String inviterId);
    void deleteByInviteeId(String inviteeId);
}
