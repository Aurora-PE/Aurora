package ro.unibuc.prodeng.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import ro.unibuc.prodeng.request.UpdateRoleRequest;
import ro.unibuc.prodeng.service.GroupMemberService;
import ro.unibuc.prodeng.util.JwtUtil;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GroupMemberControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private GroupMemberService groupMemberService;
    @InjectMocks private GroupMemberController groupMemberController;

    private MockedStatic<JwtUtil> mockedJwtUtil;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupMemberController).build();
        objectMapper = new ObjectMapper();

        mockedJwtUtil = mockStatic(JwtUtil.class);
        mockedJwtUtil.when(() -> JwtUtil.extractRequesterId(anyString())).thenReturn("1");
    }

    @AfterEach
    void tearDown() {
        mockedJwtUtil.close();
    }

    @Test
    void testGetGroupMembers_validGroupId_returnsMembersList() throws Exception {
        when(groupMemberService.getGroupMembers("1", "g1")).thenReturn(List.of());

        mockMvc.perform(get("/api/groups/g1/members")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void testKickMember_validTargetId_returnsNoContentStatus() throws Exception {
        doNothing().when(groupMemberService).kickMember("1", "g1", "2");

        mockMvc.perform(delete("/api/groups/g1/members/2")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());

        verify(groupMemberService).kickMember("1", "g1", "2");
    }

    @Test
    void testChangeMemberRole_validRequest_returnsOkStatus() throws Exception {
        UpdateRoleRequest request = new UpdateRoleRequest("ADMIN");
        doNothing().when(groupMemberService).changeMemberRole("1", "g1", "2", "ADMIN");

        mockMvc.perform(patch("/api/groups/g1/members/2/role")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(groupMemberService).changeMemberRole("1", "g1", "2", "ADMIN");
    }
}