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
import ro.unibuc.prodeng.request.CreateUserRequest;
import ro.unibuc.prodeng.request.LoginRequest;
import ro.unibuc.prodeng.request.UpdateUserRequest;
import ro.unibuc.prodeng.response.LoginResponse;
import ro.unibuc.prodeng.response.UserResponse;
import ro.unibuc.prodeng.response.UserSummaryResponse;
import ro.unibuc.prodeng.service.MetricsService;
import ro.unibuc.prodeng.service.UserService;
import ro.unibuc.prodeng.util.JwtUtil;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private UserService userService;
    @Mock private MetricsService metricsService;
    @InjectMocks private UserController userController;

    private MockedStatic<JwtUtil> mockedJwtUtil;
    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockedJwtUtil = mockStatic(JwtUtil.class);
        mockedJwtUtil.when(() -> JwtUtil.extractRequesterId(anyString())).thenReturn("1");
        
        mockUserResponse = new UserResponse("1", "user1", "u@test.com", "bio", "url", false, LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        mockedJwtUtil.close();
    }

    @Test
    void testCreateUser_validRequest_returnsCreatedStatus() throws Exception {
        CreateUserRequest request = new CreateUserRequest("user1", "u@test.com", "pass", "bio", "url", false);
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(mockUserResponse);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("user1"));
    }

    @Test
    void testLogin_validCredentials_returnsOkStatusAndToken() throws Exception {
        LoginRequest request = new LoginRequest("u@test.com", "pass");
        LoginResponse response = new LoginResponse("token", mockUserResponse);
        when(userService.login("u@test.com", "pass")).thenReturn(response);

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void testSearchUsers_validUsername_returnsUserSummaries() throws Exception {
        UserSummaryResponse summary = new UserSummaryResponse("1", "user1", "url");
        when(userService.searchUsersByUsername("user1")).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/users").param("username", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user1"));
    }

    @Test
    void testGetUserById_validId_returnsUserResponse() throws Exception {
        when(userService.getUserById("1", "2")).thenReturn(mockUserResponse);

        mockMvc.perform(get("/api/users/2")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateMyProfile_validRequest_returnsUpdatedUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("new bio", "url", false);
        when(userService.updateUser(eq("1"), any(UpdateUserRequest.class))).thenReturn(mockUserResponse);

        mockMvc.perform(put("/api/users/self")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteMyProfile_validToken_returnsNoContentStatus() throws Exception {
        doNothing().when(userService).deleteUser("1");

        mockMvc.perform(delete("/api/users/self")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());
        
        verify(userService).deleteUser("1");
    }

    @Test
    void testCreateUser_invalidRequest_returnsBadRequest() throws Exception {
        CreateUserRequest invalidRequest = new CreateUserRequest("", "not-an-email", "", "bio", "url", false);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void testLogin_missingCredentials_returnsBadRequest() throws Exception {
        LoginRequest invalidRequest = new LoginRequest("", "");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }
}