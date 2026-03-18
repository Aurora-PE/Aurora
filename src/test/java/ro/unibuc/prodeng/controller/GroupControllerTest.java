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
import ro.unibuc.prodeng.request.CreateGroupRequest;
import ro.unibuc.prodeng.response.GroupResponse;
import ro.unibuc.prodeng.service.GroupService;
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
class GroupControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private GroupService groupService;
    @InjectMocks private GroupController groupController;

    private MockedStatic<JwtUtil> mockedJwtUtil;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupController).build();
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
    void testCreateGroup_validRequest_returnsCreatedStatus() throws Exception {
        CreateGroupRequest request = new CreateGroupRequest("Group Name", "Desc");
        GroupResponse response = new GroupResponse("g1", "Group Name", "Desc", "1", LocalDateTime.now());
        when(groupService.createGroup(eq("1"), any(CreateGroupRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/groups")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetMyGroups_validToken_returnsGroupList() throws Exception {
        when(groupService.getMyGroups("1")).thenReturn(List.of());

        mockMvc.perform(get("/api/groups")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetGroupById_validId_returnsGroupResponse() throws Exception {
        GroupResponse response = new GroupResponse("g1", "Group Name", "Desc", "1", LocalDateTime.now());
        when(groupService.getGroupById("g1")).thenReturn(response);

        mockMvc.perform(get("/api/groups/g1"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteGroup_validId_returnsNoContentStatus() throws Exception {
        doNothing().when(groupService).deleteGroup("1", "g1");

        mockMvc.perform(delete("/api/groups/g1")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());
        
        verify(groupService).deleteGroup("1", "g1");
    }
}