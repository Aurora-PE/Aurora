package ro.unibuc.prodeng.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.unibuc.prodeng.IntegrationTestBase;
import ro.unibuc.prodeng.model.VisibilityEnum;
import ro.unibuc.prodeng.repository.CommentRepository;
import ro.unibuc.prodeng.repository.LikeRepository;
import ro.unibuc.prodeng.repository.PostRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.CreatePostRequest;
import ro.unibuc.prodeng.request.CreateUserRequest;
import ro.unibuc.prodeng.request.UpdatePostRequest;
import ro.unibuc.prodeng.util.JwtUtil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@DisplayName("Post Workflow Integration Tests")
class PostIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private LikeRepository likeRepository;
    @Autowired private CommentRepository commentRepository;

    @BeforeEach
    void cleanUp() {
        likeRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String createUser(String username, String email) throws Exception {
        CreateUserRequest request = new CreateUserRequest(username, email, "password123", "Bio", "url", false);
        String response = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    private String createPost(String token, String authorId, String content, VisibilityEnum visibility) throws Exception {
        CreatePostRequest request = new CreatePostRequest(authorId, content, null, visibility);
        String response = mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void testCreateAndGetPost_validPost_persistsAndRetrievesFromDatabase() throws Exception {
        String userId = createUser("postAuthor", "author@example.com");
        String token = JwtUtil.generateToken(userId);

        String postId = createPost(token, userId, "Hello, this is my first post!", VisibilityEnum.PUBLIC);

        mockMvc.perform(get("/api/posts/" + postId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(postId)))
                .andExpect(jsonPath("$.authorId", is(userId)))
                .andExpect(jsonPath("$.content", is("Hello, this is my first post!")))
                .andExpect(jsonPath("$.likesCount", is(0)));

        assert postRepository.findById(postId).isPresent();
    }

    @Test
    void testUpdatePost_validData_updatesPostInDatabase() throws Exception {
        String userId = createUser("updateAuthor", "update@example.com");
        String token = JwtUtil.generateToken(userId);
        String postId = createPost(token, userId, "Original content", VisibilityEnum.PUBLIC);

        UpdatePostRequest updateRequest = new UpdatePostRequest("Updated content", "https://img.example.com/new.png", VisibilityEnum.PRIVATE);
        mockMvc.perform(put("/api/posts/" + postId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("Updated content")))
                .andExpect(jsonPath("$.imageUrl", is("https://img.example.com/new.png")));

        mockMvc.perform(get("/api/posts/" + postId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("Updated content")));
    }

    @Test
    void testDeletePost_existingPost_removesPostFromDatabase() throws Exception {
        
        String userId = createUser("deleteAuthor", "delete@example.com");
        String token = JwtUtil.generateToken(userId);
        String postId = createPost(token, userId, "Post to be deleted", VisibilityEnum.PUBLIC);

        
        assert postRepository.findById(postId).isPresent();

        
        mockMvc.perform(delete("/api/posts/" + postId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assert postRepository.findById(postId).isEmpty();
    }

    @Test
    void testPostWorkflow_createGetUpdateDelete_worksEndToEnd() throws Exception {
        
        String userId = createUser("workflowUser", "workflow@example.com");
        String token = JwtUtil.generateToken(userId);

        String postId1 = createPost(token, userId, "First post", VisibilityEnum.PUBLIC);
        String postId2 = createPost(token, userId, "Second post", VisibilityEnum.PUBLIC);

        mockMvc.perform(get("/api/posts")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        UpdatePostRequest updateRequest = new UpdatePostRequest("First post - edited", null, VisibilityEnum.PUBLIC);
        mockMvc.perform(put("/api/posts/" + postId1)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("First post - edited")));

        
        mockMvc.perform(delete("/api/posts/" + postId2)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].content", is("First post - edited")));

        assert postRepository.findById(postId1).isPresent();
        assert postRepository.findById(postId2).isEmpty();
    }
}