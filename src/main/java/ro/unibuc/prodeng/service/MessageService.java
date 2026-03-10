package ro.unibuc.prodeng.service;

import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.model.ConversationEntity;
import ro.unibuc.prodeng.model.MessageEntity;
import ro.unibuc.prodeng.repository.MessageRepository;
import ro.unibuc.prodeng.response.ConversationResponse;
import ro.unibuc.prodeng.response.MessageResponse;
import ro.unibuc.prodeng.request.SendMessageRequest;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.UnauthorizedException;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationService conversationService;
    private final NotificationService notificationService;

    public MessageService(
            MessageRepository messageRepository,
            ConversationService conversationService,
            NotificationService notificationService
    ) {
        this.messageRepository = messageRepository;
        this.conversationService = conversationService;
        this.notificationService = notificationService;
    }

    public MessageResponse sendMessage(String senderId, String receiverId, SendMessageRequest request) {

        ConversationEntity conversation =
                conversationService.getOrCreateConversation(senderId, receiverId);

        MessageEntity message = new MessageEntity(
                conversation.id(),
                senderId,
                request.content()
        );

        MessageEntity savedMessage = messageRepository.save(message);

        conversationService.updateLastMessageAt(conversation.id());

        notificationService.createNotification(receiverId, "You received a new message", senderId);

        return toResponse(savedMessage);
    }

    public List<MessageResponse> getConversationMessages(String requesterId, String conversationId) {

        ConversationResponse conversation =
                conversationService.getConversation(requesterId, conversationId);

        if (!conversation.participant1Id().equals(requesterId)
                && !conversation.participant2Id().equals(requesterId)) {
            throw new UnauthorizedException("You are not part of this conversation.");
        }

        return messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public MessageResponse markMessageRead(String requesterId, String messageId) {

        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message"));

        ConversationResponse conversation =
                conversationService.getConversation(requesterId, message.conversationId());

        if (!conversation.participant1Id().equals(requesterId)
                && !conversation.participant2Id().equals(requesterId)) {
            throw new UnauthorizedException("Cannot modify message.");
        }

        MessageEntity updated = new MessageEntity(
                message.id(),
                message.conversationId(),
                message.senderId(),
                message.content(),
                true,
                message.createdAt()
        );

        return toResponse(messageRepository.save(updated));
    }

    private MessageResponse toResponse(MessageEntity message) {

        return new MessageResponse(
                message.id(),
                message.conversationId(),
                message.senderId(),
                message.content(),
                message.read(),
                message.createdAt()
        );
    }
}