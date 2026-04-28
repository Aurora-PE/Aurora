package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.VisibilityEnum;
import ro.unibuc.prodeng.request.CreatePostRequest;
import ro.unibuc.prodeng.request.UpdatePostRequest;
import ro.unibuc.prodeng.response.PostResponse;
import ro.unibuc.prodeng.service.MetricsService;
import ro.unibuc.prodeng.service.PostService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private PostController postController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private PostResponse sampleResponse;
    private final String AUTH_HEADER = "Bearer test-token";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(postController)
            .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        sampleResponse = new PostResponse(
            "post1", "user1", "Test content",
            "https://example.com/img.png",
            VisibilityEnum.PUBLIC, 0, LocalDateTime.now()
        );
    }


    @Test
    void getAllPosts_success() throws Exception {
        when(postService.getAllPosts())
            .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/posts")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value("post1"))
            .andExpect(jsonPath("$[0].authorId").value("user1"))
            .andExpect(jsonPath("$[0].content")
                .value("Test content"));

        verify(postService).getAllPosts();
    }

    @Test
    void getAllPosts_empty() throws Exception {
        when(postService.getAllPosts())
            .thenReturn(List.of());

        mockMvc.perform(get("/api/posts")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }


    @Test
    void getPost_success() throws Exception {
        when(postService.getPost("post1"))
            .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/posts/post1")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("post1"))
            .andExpect(jsonPath("$.authorId").value("user1"))
            .andExpect(jsonPath("$.content")
                .value("Test content"))
            .andExpect(jsonPath("$.visibility")
                .value("PUBLIC"))
            .andExpect(jsonPath("$.likesCount").value(0));

        verify(postService).getPost("post1");
    }

    @Test
    void getPost_notFound() throws Exception {
        when(postService.getPost("invalid"))
            .thenThrow(
                new EntityNotFoundException("Post invalid")
            );

        mockMvc.perform(get("/api/posts/invalid")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isNotFound());
    }

    @Test
    void createPost_success() throws Exception {
        CreatePostRequest request = new CreatePostRequest(
            "user1", "Test content",
            "https://example.com/img.png",
            VisibilityEnum.PUBLIC
        );

        when(postService.createPost(any(CreatePostRequest.class)))
            .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/posts")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("post1"))
            .andExpect(jsonPath("$.authorId").value("user1"))
            .andExpect(jsonPath("$.content")
                .value("Test content"));

        verify(postService)
            .createPost(any(CreatePostRequest.class));
    }

    @Test
    void updatePost_success() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest(
            "Updated content",
            "https://example.com/new.png",
            VisibilityEnum.PRIVATE
        );

        PostResponse updatedResponse = new PostResponse(
            "post1", "user1", "Updated content",
            "https://example.com/new.png",
            VisibilityEnum.PRIVATE, 0, LocalDateTime.now()
        );

        when(postService.updatePost(
            eq("post1"), any(UpdatePostRequest.class)
        )).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/posts/post1")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content")
                .value("Updated content"))
            .andExpect(jsonPath("$.visibility")
                .value("PRIVATE"));

        verify(postService)
            .updatePost(eq("post1"),
                any(UpdatePostRequest.class));
    }

    @Test
    void updatePost_notFound() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest(
            "content", null, VisibilityEnum.PUBLIC
        );

        when(postService.updatePost(
            eq("invalid"), any(UpdatePostRequest.class)
        )).thenThrow(
            new EntityNotFoundException("Post invalid")
        );

        mockMvc.perform(put("/api/posts/invalid")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void deletePost_success() throws Exception {
        doNothing().when(postService).deletePost("post1");

        mockMvc.perform(delete("/api/posts/post1")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isNoContent());

        verify(postService).deletePost("post1");
    }

    @Test
    void deletePost_notFound() throws Exception {
        doThrow(new EntityNotFoundException("Post invalid"))
            .when(postService).deletePost("invalid");

        mockMvc.perform(delete("/api/posts/invalid")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isNotFound());
    }
}