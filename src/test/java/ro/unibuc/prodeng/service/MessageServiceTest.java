package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.UnauthorizedException;
import ro.unibuc.prodeng.model.ConversationEntity;
import ro.unibuc.prodeng.model.MessageEntity;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.FollowRepository;
import ro.unibuc.prodeng.repository.MessageRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.SendMessageRequest;
import ro.unibuc.prodeng.response.ConversationResponse;
import ro.unibuc.prodeng.response.MessageResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock private MessageRepository messageRepository;
    @Mock private ConversationService conversationService;
    @Mock private NotificationService notificationService;
    @Mock private UserRepository userRepository;
    @Mock private FollowRepository followRepository;

    @InjectMocks private MessageService messageService;

    @Test
    void testSendMessage_publicUser_sendsSuccessfully() {

        UserEntity receiver = new UserEntity("user2", "u2", "e", "p", "", "", LocalDateTime.now(), false);
        ConversationEntity conversation = new ConversationEntity("c1", "user1", "user2", LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.findById("user2")).thenReturn(Optional.of(receiver));
        when(conversationService.getOrCreateConversation("user1", "user2")).thenReturn(conversation);
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc("c1")).thenReturn(List.of());

        MessageResponse result = messageService.sendMessage(
                "user1",
                "user2",
                new SendMessageRequest("hello")
        );

        assertEquals("hello", result.content());
        verify(notificationService).createNotification(eq("user2"), anyString(), eq("user1"));
        verify(conversationService).updateLastMessageAt(eq("c1"));
    }

    @Test
    void testSendMessage_privateUserNotFollowed_throwsUnauthorizedException() {

        UserEntity receiver = new UserEntity("user2", "u2", "e", "p", "", "", LocalDateTime.now(), true);

        when(userRepository.findById("user2")).thenReturn(Optional.of(receiver));
        when(followRepository.existsByFollowerIdAndFollowingId("user1", "user2"))
                .thenReturn(false);

        assertThrows(UnauthorizedException.class, () ->
                messageService.sendMessage("user1", "user2", new SendMessageRequest("hello"))
        );
    }

    @Test
    void testSendMessage_privateUserFollowed_sendsSuccessfully() {

        UserEntity receiver = new UserEntity("user2", "u2", "e", "p", "", "", LocalDateTime.now(), true);
        ConversationEntity conversation = new ConversationEntity("c1", "user1", "user2", LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.findById("user2")).thenReturn(Optional.of(receiver));
        when(followRepository.existsByFollowerIdAndFollowingId("user1", "user2"))
                .thenReturn(true);
        when(conversationService.getOrCreateConversation("user1", "user2")).thenReturn(conversation);
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc("c1")).thenReturn(List.of());

        MessageResponse result = messageService.sendMessage(
                "user1",
                "user2",
                new SendMessageRequest("hello")
        );

        assertEquals("hello", result.content());
        verify(notificationService).createNotification(eq("user2"), anyString(), eq("user1"));
        verify(conversationService).updateLastMessageAt(eq("c1"));
    }

    @Test
    void testSendMessage_selfMessage_throwsIllegalArgumentException() {

        assertThrows(IllegalArgumentException.class, () ->
                messageService.sendMessage("user1", "user1", new SendMessageRequest("hello"))
        );
    }

    @Test
    void testSendMessage_userNotFound_throwsEntityNotFoundException() {

        when(userRepository.findById("user2")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                messageService.sendMessage("user1", "user2", new SendMessageRequest("hello"))
        );
    }

    @Test
    void testSendMessage_lastMessageFromSender_noNotification() {

        UserEntity receiver = new UserEntity("user2", "u2", "e", "p", "", "", LocalDateTime.now(), false);
        ConversationEntity conversation = new ConversationEntity("c1", "user1", "user2", LocalDateTime.now(), LocalDateTime.now());

        MessageEntity lastMessage = new MessageEntity(
                "m1", "c1", "user1", "old", false, LocalDateTime.now()
        );

        when(userRepository.findById("user2")).thenReturn(Optional.of(receiver));
        when(conversationService.getOrCreateConversation("user1", "user2")).thenReturn(conversation);
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc("c1"))
                .thenReturn(List.of(lastMessage));

        MessageResponse result = messageService.sendMessage(
                "user1",
                "user2",
                new SendMessageRequest("hello")
        );

        assertEquals("hello", result.content());
        verify(conversationService).updateLastMessageAt(eq("c1"));
        verify(notificationService, never()).createNotification(any(), any(), any());
    }

    @Test
    void testGetConversationMessages_validUser_returnsMessages() {

        ConversationResponse conv = new ConversationResponse("c1", "user1", "user2", LocalDateTime.now(), LocalDateTime.now());
        MessageEntity message = new MessageEntity("m1", "c1", "user1", "hello", false, LocalDateTime.now());

        when(conversationService.getConversation("user1", "c1")).thenReturn(conv);
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc("c1"))
                .thenReturn(List.of(message));

        List<MessageResponse> result =
                messageService.getConversationMessages("user1", "c1");

        assertEquals(1, result.size());
    }

    @Test
    void testGetConversationMessages_notParticipant_throwsUnauthorizedException() {

        ConversationResponse conv = new ConversationResponse("c1", "user1", "user2", LocalDateTime.now(), LocalDateTime.now());

        when(conversationService.getConversation("user3", "c1")).thenReturn(conv);

        assertThrows(UnauthorizedException.class, () ->
                messageService.getConversationMessages("user3", "c1")
        );
    }

    @Test
    void testMarkMessageRead_validRequest_updatesMessage() {

        MessageEntity message = new MessageEntity("m1", "c1", "user1", "hello", false, LocalDateTime.now());
        ConversationResponse conv = new ConversationResponse("c1", "user1", "user2", LocalDateTime.now(), LocalDateTime.now());

        when(messageRepository.findById("m1")).thenReturn(Optional.of(message));
        when(conversationService.getConversation("user1", "c1")).thenReturn(conv);
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MessageResponse result =
                messageService.markMessageRead("user1", "m1");

        assertTrue(result.read());
    }

    @Test
    void testMarkMessageRead_notFound_throwsEntityNotFoundException() {

        when(messageRepository.findById("m1")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                messageService.markMessageRead("user1", "m1")
        );
    }

    @Test
    void testMarkMessageRead_notParticipant_throwsUnauthorizedException() {

        MessageEntity message = new MessageEntity("m1", "c1", "user1", "hello", false, LocalDateTime.now());
        ConversationResponse conv = new ConversationResponse("c1", "user1", "user2", LocalDateTime.now(), LocalDateTime.now());

        when(messageRepository.findById("m1")).thenReturn(Optional.of(message));
        when(conversationService.getConversation("user3", "c1")).thenReturn(conv);

        assertThrows(UnauthorizedException.class, () ->
                messageService.markMessageRead("user3", "m1")
        );
    }
}