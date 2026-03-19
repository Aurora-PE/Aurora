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
import ro.unibuc.prodeng.request.CreateInviteRequest;
import ro.unibuc.prodeng.response.GroupInvitationResponse;
import ro.unibuc.prodeng.service.GroupInvitationService;
import ro.unibuc.prodeng.util.JwtUtil;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GroupInvitationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private GroupInvitationService groupInviteService;
    @InjectMocks private GroupInvitationController groupInvitationController;

    private MockedStatic<JwtUtil> mockedJwtUtil;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupInvitationController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockedJwtUtil = mockStatic(JwtUtil.class);
        mockedJwtUtil.when(() -> JwtUtil.extractRequesterId(anyString())).thenReturn("1");
    }

    @AfterEach
    void tearDown() {
        mockedJwtUtil.close();
    }

    @Test
    void testCreateInvitation_validRequest_returnsCreatedStatus() throws Exception {
        CreateInviteRequest request = new CreateInviteRequest("2");
        GroupInvitationResponse response = new GroupInvitationResponse("inv1", "g1", "1", "2", LocalDateTime.now());
        when(groupInviteService.createInvitation(eq("1"), eq("g1"), any(CreateInviteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/groups/g1/invitations")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetMyInvitations_validToken_returnsInvitationsList() throws Exception {
        when(groupInviteService.getMyInvitations("1")).thenReturn(List.of());

        mockMvc.perform(get("/api/groups/invitations")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void testAcceptInvitation_validId_returnsOkStatus() throws Exception {
        doNothing().when(groupInviteService).acceptInvitation("1", "inv1");

        mockMvc.perform(post("/api/groups/invitations/inv1/accept")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());

        verify(groupInviteService).acceptInvitation("1", "inv1");
    }

    @Test
    void testDeclineInvitation_validId_returnsOkStatus() throws Exception {
        doNothing().when(groupInviteService).declineInvitation("1", "inv1");

        mockMvc.perform(post("/api/groups/invitations/inv1/decline")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());

        verify(groupInviteService).declineInvitation("1", "inv1");
    }

    @Test
    void testCreateInvitation_missingInviteeId_returnsBadRequest() throws Exception {
        CreateInviteRequest invalidRequest = new CreateInviteRequest("");

        mockMvc.perform(post("/api/groups/g1/invitations")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(groupInviteService);
    }
}