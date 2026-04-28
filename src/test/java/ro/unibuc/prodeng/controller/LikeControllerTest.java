package ro.unibuc.prodeng.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.prodeng.exception.DuplicateActionException;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.GlobalExceptionHandler;
import ro.unibuc.prodeng.exception.UnauthorizedException;
import ro.unibuc.prodeng.service.LikeService;
import ro.unibuc.prodeng.service.MetricsService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LikeControllerTest {

    @Mock
    private LikeService likeService;

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private LikeController likeController;

    private MockMvc mockMvc;

    private final String AUTH_HEADER = "Bearer test-token";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
        .standaloneSetup(likeController)
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
    }



    @Test
    void likePost_success() throws Exception {
        doNothing().when(likeService)
            .likePost("post1", "user1");

        mockMvc.perform(post("/api/posts/post1/like")
                .param("userId", "user1")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isOk());

        verify(likeService).likePost("post1", "user1");
    }

    @Test
    void likePost_postNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Post post1"))
            .when(likeService).likePost("post1", "user1");

        mockMvc.perform(post("/api/posts/post1/like")
                .param("userId", "user1")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isNotFound());

        verify(likeService).likePost("post1", "user1");
    }

    @Test
    void likePost_duplicate() throws Exception {
        doThrow(new DuplicateActionException(
                "User already liked this post"))
            .when(likeService).likePost("post1", "user1");

        mockMvc.perform(post("/api/posts/post1/like")
                .param("userId", "user1")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isConflict());

        verify(likeService).likePost("post1", "user1");
    }

    @Test
    void likePost_unauthorized() throws Exception {
        doThrow(new UnauthorizedException(
                "Cannot like a private post without following the author"))
            .when(likeService).likePost("post1", "user1");

        mockMvc.perform(post("/api/posts/post1/like")
                .param("userId", "user1")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isUnauthorized());

        verify(likeService).likePost("post1", "user1");
    }

    @Test
    void likePost_missingUserId() throws Exception {
        mockMvc.perform(post("/api/posts/post1/like")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(likeService);
    }

    @Test
    void likePost_missingAuthHeader() throws Exception {
        mockMvc.perform(post("/api/posts/post1/like")
                .param("userId", "user1"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(likeService);
    }


    @Test
    void unlikePost_success() throws Exception {
        doNothing().when(likeService)
            .unlikePost("post1", "user1");

        mockMvc.perform(delete("/api/posts/post1/unlike")
                .param("userId", "user1")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isNoContent());

        verify(likeService).unlikePost("post1", "user1");
    }

    @Test
    void unlikePost_postNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Post post1"))
            .when(likeService).unlikePost("post1", "user1");

        mockMvc.perform(delete("/api/posts/post1/unlike")
                .param("userId", "user1")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isNotFound());

        verify(likeService).unlikePost("post1", "user1");
    }

    @Test
    void unlikePost_likeNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Like"))
            .when(likeService).unlikePost("post1", "user1");

        mockMvc.perform(delete("/api/posts/post1/unlike")
                .param("userId", "user1")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isNotFound());

        verify(likeService).unlikePost("post1", "user1");
    }

    @Test
    void unlikePost_missingUserId() throws Exception {
        mockMvc.perform(delete("/api/posts/post1/unlike")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(likeService);
    }

    @Test
    void unlikePost_missingAuthHeader() throws Exception {
        mockMvc.perform(delete("/api/posts/post1/unlike")
                .param("userId", "user1"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(likeService);
    }


    @Test
    void likeComment_success() throws Exception {
        doNothing().when(likeService)
            .likeComment("comment1", "user1");

        mockMvc.perform(
                post("/api/posts/post1/comments/comment1/like")
                    .param("userId", "user1")
                    .header("Authorization", AUTH_HEADER))
            .andExpect(status().isOk());

        verify(likeService).likeComment("comment1", "user1");
    }

    @Test
    void likeComment_commentNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Comment comment1"))
            .when(likeService)
            .likeComment("comment1", "user1");

        mockMvc.perform(
                post("/api/posts/post1/comments/comment1/like")
                    .param("userId", "user1")
                    .header("Authorization", AUTH_HEADER))
            .andExpect(status().isNotFound());

        verify(likeService).likeComment("comment1", "user1");
    }

    @Test
    void likeComment_duplicate() throws Exception {
        doThrow(new DuplicateActionException(
                "User already liked this comment"))
            .when(likeService)
            .likeComment("comment1", "user1");

        mockMvc.perform(
                post("/api/posts/post1/comments/comment1/like")
                    .param("userId", "user1")
                    .header("Authorization", AUTH_HEADER))
            .andExpect(status().isConflict());

        verify(likeService).likeComment("comment1", "user1");
    }

    @Test
    void likeComment_missingUserId() throws Exception {
        mockMvc.perform(
                post("/api/posts/post1/comments/comment1/like")
                    .header("Authorization", AUTH_HEADER))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(likeService);
    }

    @Test
    void likeComment_missingAuthHeader() throws Exception {
        mockMvc.perform(
                post("/api/posts/post1/comments/comment1/like")
                    .param("userId", "user1"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(likeService);
    }
}