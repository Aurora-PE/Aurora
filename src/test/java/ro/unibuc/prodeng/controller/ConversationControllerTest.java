package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.prodeng.response.ConversationResponse;
import ro.unibuc.prodeng.service.ConversationService;
import ro.unibuc.prodeng.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ConversationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private ConversationService conversationService;
    @InjectMocks private ConversationController conversationController;

    private MockedStatic<JwtUtil> mockedJwtUtil;

    private ConversationResponse mockConversation;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(conversationController).build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockedJwtUtil = mockStatic(JwtUtil.class);
        mockedJwtUtil.when(() -> JwtUtil.extractRequesterId(anyString()))
                .thenReturn("user1");

        mockConversation = new ConversationResponse(
                "c1",
                "user1",
                "user2",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @AfterEach
    void tearDown() {
        mockedJwtUtil.close();
    }

    @Test
    void testGetMyConversations_validToken_returnsConversations() throws Exception {

        when(conversationService.getUserConversations("user1"))
                .thenReturn(List.of(mockConversation));

        mockMvc.perform(get("/api/conversations")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("c1"));
    }

    @Test
    void testGetConversation_validRequest_returnsConversation() throws Exception {

        when(conversationService.getConversation("user1", "c1"))
                .thenReturn(mockConversation);

        mockMvc.perform(get("/api/conversations/c1")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c1"));
    }

    @Test
    void testGetMyConversations_missingAuthHeader_returnsServerError() throws Exception {

        mockMvc.perform(get("/api/conversations"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(conversationService);
    }
}