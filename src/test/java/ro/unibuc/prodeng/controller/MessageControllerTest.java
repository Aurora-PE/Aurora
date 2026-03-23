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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.prodeng.request.SendMessageRequest;
import ro.unibuc.prodeng.response.MessageResponse;
import ro.unibuc.prodeng.service.MessageService;
import ro.unibuc.prodeng.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private MessageService messageService;
    @InjectMocks private MessageController messageController;

    private MockedStatic<JwtUtil> mockedJwtUtil;

    private MessageResponse mockMessage;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(messageController).build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockedJwtUtil = mockStatic(JwtUtil.class);
        mockedJwtUtil.when(() -> JwtUtil.extractRequesterId(anyString()))
                .thenReturn("user1");

        mockMessage = new MessageResponse(
                "m1",
                "c1",
                "user1",
                "Hello",
                false,
                LocalDateTime.now()
        );
    }

    @AfterEach
    void tearDown() {
        mockedJwtUtil.close();
    }

    @Test
    void testSendMessage_validRequest_returnsMessage() throws Exception {

        SendMessageRequest request = new SendMessageRequest("Hello");

        when(messageService.sendMessage(eq("user1"), eq("user2"), any()))
                .thenReturn(mockMessage);

        mockMvc.perform(post("/api/messages/user2")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello"));
    }

    @Test
    void testGetConversationMessages_validRequest_returnsMessages() throws Exception {

        when(messageService.getConversationMessages("user1", "c1"))
                .thenReturn(List.of(mockMessage));

        mockMvc.perform(get("/api/messages/conversation/c1")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hello"));
    }

    @Test
    void testMarkMessageRead_validRequest_returnsUpdatedMessage() throws Exception {

        MessageResponse updated = new MessageResponse(
                "m1",
                "c1",
                "user1",
                "Hello",
                true,
                LocalDateTime.now()
        );

        when(messageService.markMessageRead("user1", "m1"))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/messages/m1/read")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void testSendMessage_missingAuthHeader_returnsServerError() throws Exception {

        SendMessageRequest request = new SendMessageRequest("Hello");

        mockMvc.perform(post("/api/messages/user2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(messageService);
    }
}