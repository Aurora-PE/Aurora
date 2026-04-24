package ro.unibuc.prodeng.controller;
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
import ro.unibuc.prodeng.service.FollowService;
import ro.unibuc.prodeng.service.MetricsService;
import ro.unibuc.prodeng.util.JwtUtil;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FollowControllerTest {

    private MockMvc mockMvc;

    @Mock private FollowService followService;
    @Mock private MetricsService metricsService;
    @InjectMocks private FollowController followController;

    private MockedStatic<JwtUtil> mockedJwtUtil;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(followController).build();

        mockedJwtUtil = mockStatic(JwtUtil.class);
        mockedJwtUtil.when(() -> JwtUtil.extractRequesterId(anyString())).thenReturn("1");
    }

    @AfterEach
    void tearDown() {
        mockedJwtUtil.close();
    }

    @Test
    void testFollowUser_validTargetId_returnsCreatedStatus() throws Exception {
        doNothing().when(followService).followUser("1", "2");

        mockMvc.perform(post("/api/users/2/follow")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isCreated());
        verify(followService).followUser("1", "2");
    }

    @Test
    void testUnfollowUser_validTargetId_returnsNoContentStatus() throws Exception {
        doNothing().when(followService).unfollowUser("1", "2");

        mockMvc.perform(delete("/api/users/2/unfollow")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());
        verify(followService).unfollowUser("1", "2");
    }

    @Test
    void testGetFollowers_validToken_returnsFollowersList() throws Exception {
        when(followService.getFollowers("1")).thenReturn(List.of());

        mockMvc.perform(get("/api/users/followers")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetFollowing_validToken_returnsFollowingList() throws Exception {
        when(followService.getFollowing("1")).thenReturn(List.of());

        mockMvc.perform(get("/api/users/following")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void testFollowUser_missingAuthHeader_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users/2/follow"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(followService);
    }
}