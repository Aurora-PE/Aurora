package ro.unibuc.prodeng.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.unibuc.prodeng.IntegrationTestBase;
import ro.unibuc.prodeng.repository.FollowRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.CreateUserRequest;
import ro.unibuc.prodeng.request.LoginRequest;
import ro.unibuc.prodeng.request.UpdateUserRequest;
import ro.unibuc.prodeng.util.JwtUtil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

// Triggering CI build 3
@DisplayName("UserController Integration Tests")
class UserControllerIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private FollowRepository followRepository;

    @BeforeEach
    void cleanUp() {
        followRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String createUserAndGetId(String username, String email) throws Exception {
        CreateUserRequest request = new CreateUserRequest(username, email, "password123", "Bio", "url", false);
        
        String response = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
                
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void testCreateAndGetUser_validUserCreation_retrievesUserFromDatabase() throws Exception {
        String userId = createUserAndGetId("integrationUser", "int@example.com");
        String token = JwtUtil.generateToken(userId);

        mockMvc.perform(get("/api/users/" + userId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("integrationUser")))
                .andExpect(jsonPath("$.email", is("int@example.com")));
    }

    @Test
    void testLogin_validCredentials_returnsToken() throws Exception {
        createUserAndGetId("loginUser", "login@example.com");
        LoginRequest loginRequest = new LoginRequest("login@example.com", "password123");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.username", is("loginUser")));
    }

    @Test
    void testUpdateUser_validData_updatesUserInDatabase() throws Exception {
        String userId = createUserAndGetId("updateUser", "update@example.com");
        String token = JwtUtil.generateToken(userId);
        UpdateUserRequest updateRequest = new UpdateUserRequest("Updated Bio", "new_url", true);

        mockMvc.perform(put("/api/users/self")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio", is("Updated Bio")))
                .andExpect(jsonPath("$.isPrivate", is(true)));
    }

    @Test
    void testFollowUser_validTarget_savesFollowToDatabase() throws Exception {
        String followerId = createUserAndGetId("follower", "follower@example.com");
        String targetId = createUserAndGetId("target", "target@example.com");
        String token = JwtUtil.generateToken(followerId);

        mockMvc.perform(post("/api/users/" + targetId + "/follow")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/following")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(targetId)));
    }
}