package ro.unibuc.prodeng.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.prodeng.model.ConversationEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<ConversationEntity, String> {
    List<ConversationEntity> findByParticipant1IdOrParticipant2IdOrderByLastMessageAtDesc(String participant1Id,String participant2Id);
    Optional<ConversationEntity> findByParticipant1IdAndParticipant2Id(String participant1Id,String participant2Id);
    Optional<ConversationEntity> findByParticipant2IdAndParticipant1Id(String participant2Id,String participant1Id);
}