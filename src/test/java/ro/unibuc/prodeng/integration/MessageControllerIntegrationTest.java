package ro.unibuc.prodeng.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.unibuc.prodeng.IntegrationTestBase;
import ro.unibuc.prodeng.repository.*;
import ro.unibuc.prodeng.request.CreateUserRequest;
import ro.unibuc.prodeng.request.SendMessageRequest;
import ro.unibuc.prodeng.util.JwtUtil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@DisplayName("MessageController Integration Tests")
class MessageControllerIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;
    @Autowired private FollowRepository followRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private ConversationRepository conversationRepository;

    @BeforeEach
    void cleanUp() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        followRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String createUser(String username, String email, boolean isPrivate) throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                username, email, "password123", "bio", "url", isPrivate
        );

        String response = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void testSendMessage_publicUsers_messageSentSuccessfully() throws Exception {

        String user1 = createUser("user1", "u1@test.com", false);
        String user2 = createUser("user2", "u2@test.com", false);

        String token = JwtUtil.generateToken(user1);

        SendMessageRequest request = new SendMessageRequest("Hello!");

        mockMvc.perform(post("/api/messages/" + user2)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("Hello!")))
                .andExpect(jsonPath("$.senderId", is(user1)));
    }

    @Test
    void testSendMessage_privateUserNotFollowed_returnsUnauthorized() throws Exception {

        String user1 = createUser("user1", "u1@test.com", false);
        String user2 = createUser("user2", "u2@test.com", true); // private

        String token = JwtUtil.generateToken(user1);

        SendMessageRequest request = new SendMessageRequest("Hello!");

        mockMvc.perform(post("/api/messages/" + user2)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testSendMessage_privateUserFollowed_messageSentSuccessfully() throws Exception {

        String user1 = createUser("user1", "u1@test.com", false);
        String user2 = createUser("user2", "u2@test.com", true);

        String token = JwtUtil.generateToken(user1);

        mockMvc.perform(post("/api/users/" + user2 + "/follow")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        SendMessageRequest request = new SendMessageRequest("Hello!");

        mockMvc.perform(post("/api/messages/" + user2)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("Hello!")));
    }

    @Test
    void testGetConversationMessages_existingConversation_returnsMessages() throws Exception {

        String user1 = createUser("user1", "u1@test.com", false);
        String user2 = createUser("user2", "u2@test.com", false);

        String token1 = JwtUtil.generateToken(user1);

        SendMessageRequest request = new SendMessageRequest("Hello!");

        String response = mockMvc.perform(post("/api/messages/" + user2)
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        String conversationId = objectMapper.readTree(response).get("conversationId").asText();

        mockMvc.perform(get("/api/messages/conversation/" + conversationId)
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].content", is("Hello!")));
    }

    @Test
    void testMarkMessageRead_validMessage_marksAsRead() throws Exception {

        String user1 = createUser("user1", "u1@test.com", false);
        String user2 = createUser("user2", "u2@test.com", false);

        String token1 = JwtUtil.generateToken(user1);
        String token2 = JwtUtil.generateToken(user2);

        SendMessageRequest request = new SendMessageRequest("Hello!");

        String response = mockMvc.perform(post("/api/messages/" + user2)
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        String messageId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(patch("/api/messages/" + messageId + "/read")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read", is(true)));
    }
}