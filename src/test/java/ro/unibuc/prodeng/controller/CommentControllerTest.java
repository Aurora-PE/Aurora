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
import ro.unibuc.prodeng.request.CreateCommentRequest;
import ro.unibuc.prodeng.response.CommentResponse;
import ro.unibuc.prodeng.service.CommentService;
import ro.unibuc.prodeng.service.MetricsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;
    
    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final String AUTH_HEADER = "Bearer test-token";
    private CommentResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(commentController)
            .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        sampleResponse = new CommentResponse(
            "comment1", "post1", "user1",
            "Nice post!", 0, LocalDateTime.now()
        );
    }


    @Test
    void getComments_success() throws Exception {
        CommentResponse second = new CommentResponse(
            "comment2", "post1", "user2",
            "Great!", 2, LocalDateTime.now()
        );

        when(commentService.getCommentsByPostId("post1"))
            .thenReturn(List.of(sampleResponse, second));

        mockMvc.perform(get("/api/posts/post1/comments")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value("comment1"))
            .andExpect(jsonPath("$[0].postId").value("post1"))
            .andExpect(jsonPath("$[0].authorId").value("user1"))
            .andExpect(jsonPath("$[0].content")
                .value("Nice post!"))
            .andExpect(jsonPath("$[1].id").value("comment2"));

        verify(commentService).getCommentsByPostId("post1");
    }

    @Test
    void getComments_empty() throws Exception {
        when(commentService.getCommentsByPostId("post1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/posts/post1/comments")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        verify(commentService).getCommentsByPostId("post1");
    }

    @Test
    void getComments_missingAuthHeader() throws Exception {
        mockMvc.perform(get("/api/posts/post1/comments"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }


    @Test
    void createComment_success() throws Exception {
        CreateCommentRequest request =
            new CreateCommentRequest("user1", "Nice post!");

        when(commentService.createComment(
            eq("post1"), any(CreateCommentRequest.class)
        )).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/posts/post1/comments")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(request)
                ))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("comment1"))
            .andExpect(jsonPath("$.postId").value("post1"))
            .andExpect(jsonPath("$.authorId").value("user1"))
            .andExpect(jsonPath("$.content")
                .value("Nice post!"))
            .andExpect(jsonPath("$.likesCount").value(0));

        verify(commentService).createComment(
            eq("post1"), any(CreateCommentRequest.class)
        );
    }

    @Test
    void createComment_postNotFound() throws Exception {
        CreateCommentRequest request =
            new CreateCommentRequest("user1", "Nice post!");

        when(commentService.createComment(
            eq("invalid"), any(CreateCommentRequest.class)
        )).thenThrow(
            new EntityNotFoundException("Post invalid")
        );

        mockMvc.perform(post("/api/posts/invalid/comments")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(request)
                ))
            .andExpect(status().isNotFound());

        verify(commentService).createComment(
            eq("invalid"), any(CreateCommentRequest.class)
        );
    }

    @Test
    void createComment_missingAuthHeader() throws Exception {
        CreateCommentRequest request =
            new CreateCommentRequest("user1", "Nice post!");

        mockMvc.perform(post("/api/posts/post1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(request)
                ))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }

    @Test
    void deleteComment_success() throws Exception {
        doNothing().when(commentService)
            .deleteComment("comment1");

        mockMvc.perform(
                delete("/api/posts/post1/comments/comment1")
                    .header("Authorization", AUTH_HEADER))
            .andExpect(status().isNoContent());

        verify(commentService).deleteComment("comment1");
    }

    @Test
    void deleteComment_notFound() throws Exception {
        doThrow(new EntityNotFoundException("Comment invalid"))
            .when(commentService).deleteComment("invalid");

        mockMvc.perform(
                delete("/api/posts/post1/comments/invalid")
                    .header("Authorization", AUTH_HEADER))
            .andExpect(status().isNotFound());

        verify(commentService).deleteComment("invalid");
    }

    @Test
    void deleteComment_missingAuthHeader() throws Exception {
        mockMvc.perform(
                delete("/api/posts/post1/comments/comment1"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }
}