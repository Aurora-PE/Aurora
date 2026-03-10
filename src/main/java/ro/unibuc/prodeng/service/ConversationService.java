package ro.unibuc.prodeng.service;

import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.model.ConversationEntity;
import ro.unibuc.prodeng.repository.ConversationRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.repository.MessageRepository;
import ro.unibuc.prodeng.response.ConversationResponse;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.UnauthorizedException;


import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public ConversationService(ConversationRepository conversationRepository, UserRepository userRepository, MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    public List<ConversationResponse> getUserConversations(String userId) {

        return conversationRepository
                .findByParticipant1IdOrParticipant2IdOrderByLastMessageAtDesc(userId, userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ConversationResponse getConversation(String requesterId, String conversationId) {
        ConversationEntity conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation " + conversationId));

        if (!conversation.participant1Id().equals(requesterId)
                && !conversation.participant2Id().equals(requesterId)) {
            throw new UnauthorizedException("You are not part of this conversation.");
        }

        return toResponse(conversation);
    }

    public ConversationEntity getOrCreateConversation(String userA, String userB) {
        if (userA.equals(userB)) {
            throw new IllegalArgumentException("Cannot create conversation with yourself.");
        }

        return conversationRepository
                .findByParticipant1IdAndParticipant2Id(userA, userB)
                .or(() -> conversationRepository.findByParticipant2IdAndParticipant1Id(userA, userB))
                .orElseGet(() -> conversationRepository.save(
                        new ConversationEntity(userA, userB)
                ));
    }

    public void updateLastMessageAt(String conversationId) {
        ConversationEntity conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation " + conversationId));

        ConversationEntity updatedConversation = new ConversationEntity(
                conversation.id(),
                conversation.participant1Id(),
                conversation.participant2Id(),
                conversation.createdAt(),
                java.time.LocalDateTime.now()
        );

        conversationRepository.save(updatedConversation);
    }

    public void deleteConversation(String deletedUserId) {
        List<ConversationEntity> conversations =
                conversationRepository.findByParticipant1IdOrParticipant2IdOrderByLastMessageAtDesc(deletedUserId, deletedUserId);

        for (ConversationEntity conversation : conversations) {

            String otherUser =
                    conversation.participant1Id().equals(deletedUserId)
                            ? conversation.participant2Id()
                            : conversation.participant1Id();

            if (!userRepository.existsById(otherUser)) {
                messageRepository.deleteByConversationId(conversation.id());
                conversationRepository.deleteById(conversation.id());
            }
        }
    }

    private ConversationResponse toResponse(ConversationEntity conversation) {

        return new ConversationResponse(
                conversation.id(),
                conversation.participant1Id(),
                conversation.participant2Id(),
                conversation.createdAt(),
                conversation.lastMessageAt()
        );
    }
}