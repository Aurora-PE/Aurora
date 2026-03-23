package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.UnauthorizedException;
import ro.unibuc.prodeng.model.ConversationEntity;
import ro.unibuc.prodeng.repository.ConversationRepository;
import ro.unibuc.prodeng.repository.MessageRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.response.ConversationResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock private ConversationRepository conversationRepository;
    @Mock private UserRepository userRepository;
    @Mock private MessageRepository messageRepository;

    @InjectMocks private ConversationService conversationService;

    private ConversationEntity testConversation =
            new ConversationEntity("c1", "user1", "user2",
                    LocalDateTime.now(), LocalDateTime.now());

    @Test
    void testGetUserConversations_existingUser_returnsConversations() {

        when(conversationRepository
                .findByParticipant1IdOrParticipant2IdOrderByLastMessageAtDesc("user1", "user1"))
                .thenReturn(List.of(testConversation));

        List<ConversationResponse> result =
                conversationService.getUserConversations("user1");

        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).participant1Id());
    }

    @Test
    void testGetConversation_userIsParticipant_returnsConversation() {

        when(conversationRepository.findById("c1"))
                .thenReturn(Optional.of(testConversation));

        ConversationResponse result =
                conversationService.getConversation("user1", "c1");

        assertEquals("c1", result.id());
    }

    @Test
    void testGetConversation_notParticipant_throwsUnauthorizedException() {

        when(conversationRepository.findById("c1"))
                .thenReturn(Optional.of(testConversation));

        assertThrows(UnauthorizedException.class,
                () -> conversationService.getConversation("user3", "c1"));
    }

    @Test
    void testGetConversation_notFound_throwsEntityNotFoundException() {

        when(conversationRepository.findById("c1"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> conversationService.getConversation("user1", "c1"));
    }

    @Test
    void testGetOrCreateConversation_existingConversation_returnsExisting() {

        when(conversationRepository
                .findByParticipant1IdAndParticipant2Id("user1", "user2"))
                .thenReturn(Optional.of(testConversation));

        ConversationEntity result =
                conversationService.getOrCreateConversation("user1", "user2");

        assertEquals("c1", result.id());
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void testGetOrCreateConversation_reverseOrder_returnsExisting() {

        when(conversationRepository
                .findByParticipant1IdAndParticipant2Id("user1", "user2"))
                .thenReturn(Optional.empty());

        when(conversationRepository
                .findByParticipant2IdAndParticipant1Id("user1", "user2"))
                .thenReturn(Optional.of(testConversation));

        ConversationEntity result =
                conversationService.getOrCreateConversation("user1", "user2");

        assertEquals("c1", result.id());
    }

    @Test
    void testGetOrCreateConversation_notExisting_createsNewConversation() {

        when(conversationRepository
                .findByParticipant1IdAndParticipant2Id("user1", "user2"))
                .thenReturn(Optional.empty());

        when(conversationRepository
                .findByParticipant2IdAndParticipant1Id("user1", "user2"))
                .thenReturn(Optional.empty());

        when(conversationRepository.save(any(ConversationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationEntity result =
                conversationService.getOrCreateConversation("user1", "user2");

        assertNotNull(result);
        verify(conversationRepository).save(any());
    }

    @Test
    void testGetOrCreateConversation_sameUser_throwsIllegalArgumentException() {

        assertThrows(IllegalArgumentException.class,
                () -> conversationService.getOrCreateConversation("user1", "user1"));
    }

    @Test
    void testUpdateLastMessageAt_existingConversation_updatesSuccessfully() {

        when(conversationRepository.findById("c1"))
                .thenReturn(Optional.of(testConversation));

        when(conversationRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        conversationService.updateLastMessageAt("c1");

        verify(conversationRepository).save(any());
    }

    @Test
    void testUpdateLastMessageAt_notFound_throwsEntityNotFoundException() {

        when(conversationRepository.findById("c1"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> conversationService.updateLastMessageAt("c1"));
    }

    @Test
    void testDeleteConversation_otherUserDeleted_deletesConversationAndMessages() {

        when(conversationRepository
                .findByParticipant1IdOrParticipant2IdOrderByLastMessageAtDesc("user1", "user1"))
                .thenReturn(List.of(testConversation));

        when(userRepository.existsById("user2")).thenReturn(false);

        conversationService.deleteConversation("user1");

        verify(messageRepository).deleteByConversationId("c1");
        verify(conversationRepository).deleteById("c1");
    }

    @Test
    void testDeleteConversation_otherUserExists_doesNotDelete() {

        when(conversationRepository
                .findByParticipant1IdOrParticipant2IdOrderByLastMessageAtDesc("user1", "user1"))
                .thenReturn(List.of(testConversation));

        when(userRepository.existsById("user2")).thenReturn(true);

        conversationService.deleteConversation("user1");

        verify(messageRepository, never()).deleteByConversationId(any());
        verify(conversationRepository, never()).deleteById(any());
    }
}