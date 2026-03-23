package ro.unibuc.prodeng.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.prodeng.model.VisibilityEnum;
import ro.unibuc.prodeng.response.PostResponse;
import ro.unibuc.prodeng.service.FeedService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FeedControllerTest {

    @Mock
    private FeedService feedService;

    @InjectMocks
    private FeedController feedController;

    private MockMvc mockMvc;

    private final String AUTH_HEADER = "Bearer test-token";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(feedController)
            .build();
    }


    @Test
    void getFeed_success() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        PostResponse post1 = new PostResponse(
            "post1", "author1", "First post",
            null, VisibilityEnum.PUBLIC, 3, now
        );
        PostResponse post2 = new PostResponse(
            "post2", "author2", "Second post",
            "https://example.com/img.png",
            VisibilityEnum.PUBLIC, 1, now
        );

        when(feedService.getFeed("user1"))
            .thenReturn(List.of(post1, post2));

        mockMvc.perform(get("/api/users/user1/feed")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value("post1"))
            .andExpect(jsonPath("$[0].authorId")
                .value("author1"))
            .andExpect(jsonPath("$[0].content")
                .value("First post"))
            .andExpect(jsonPath("$[0].likesCount").value(3))
            .andExpect(jsonPath("$[1].id").value("post2"))
            .andExpect(jsonPath("$[1].authorId")
                .value("author2"));

        verify(feedService).getFeed("user1");
    }

    @Test
    void getFeed_empty() throws Exception {
        when(feedService.getFeed("user1"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/users/user1/feed")
                .header("Authorization", AUTH_HEADER))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        verify(feedService).getFeed("user1");
    }

    @Test
    void getFeed_missingAuthHeader() throws Exception {
        mockMvc.perform(get("/api/users/user1/feed"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(feedService);
    }
}