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
import ro.unibuc.prodeng.request.CreateGroupRequest;
import ro.unibuc.prodeng.request.CreateInviteRequest;
import ro.unibuc.prodeng.request.CreateUserRequest;
import ro.unibuc.prodeng.util.JwtUtil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@DisplayName("Group Workflow Integration Tests")
class GroupIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    
    @Autowired private UserRepository userRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private GroupMemberRepository groupMemberRepository;
    @Autowired private GroupInvitationRepository groupInvitationRepository;
    @Autowired private NotificationRepository notificationRepository;

    @BeforeEach
    void cleanUp() {
        groupInvitationRepository.deleteAll();
        groupMemberRepository.deleteAll();
        groupRepository.deleteAll();
        notificationRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String createUser(String username, String email) throws Exception {
        CreateUserRequest request = new CreateUserRequest(username, email, "password123", "Bio", "url", false);
        String response = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void testGroupWorkflow_createInviteAccept_worksEndToEnd() throws Exception {
        // Setup two users
        String creatorId = createUser("Creator", "creator@example.com");
        String inviteeId = createUser("Invitee", "invitee@example.com");
        
        String creatorToken = JwtUtil.generateToken(creatorId);
        String inviteeToken = JwtUtil.generateToken(inviteeId);

        // Creator creates a group
        CreateGroupRequest groupRequest = new CreateGroupRequest("Test Group", "Integration Test Description");
        String groupResponse = mockMvc.perform(post("/api/groups")
                .header("Authorization", "Bearer " + creatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Test Group")))
                .andReturn().getResponse().getContentAsString();
        
        String groupId = objectMapper.readTree(groupResponse).get("id").asText();

        // Creator sends an invitation to the Invitee
        CreateInviteRequest inviteRequest = new CreateInviteRequest(inviteeId);
        String inviteResponse = mockMvc.perform(post("/api/groups/" + groupId + "/invitations")
                .header("Authorization", "Bearer " + creatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String invitationId = objectMapper.readTree(inviteResponse).get("id").asText();

        // Invitee verifies they received the invitation
        mockMvc.perform(get("/api/groups/invitations")
                .header("Authorization", "Bearer " + inviteeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(invitationId)));

        // Invitee accepts the invitation
        mockMvc.perform(post("/api/groups/invitations/" + invitationId + "/accept")
                .header("Authorization", "Bearer " + inviteeToken))
                .andExpect(status().isOk());

        // Creator checks the member list to verify the Invitee joined successfully!
        mockMvc.perform(get("/api/groups/" + groupId + "/members")
                .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].role", is("CREATOR")))
                .andExpect(jsonPath("$[1].username", is("Invitee")))
                .andExpect(jsonPath("$[1].role", is("MEMBER")));
    }
}