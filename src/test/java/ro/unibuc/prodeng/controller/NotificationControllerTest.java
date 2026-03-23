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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.prodeng.response.NotificationResponse;
import ro.unibuc.prodeng.service.NotificationService;
import ro.unibuc.prodeng.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private NotificationService notificationService;
    @InjectMocks private NotificationController notificationController;

    private MockedStatic<JwtUtil> mockedJwtUtil;

    private NotificationResponse mockNotification;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockedJwtUtil = mockStatic(JwtUtil.class);
        mockedJwtUtil.when(() -> JwtUtil.extractRequesterId(anyString()))
                .thenReturn("user1");

        mockNotification = new NotificationResponse(
                "1",
                "user1",
                "Test notification",
                "user2",
                false,
                LocalDateTime.now()
        );
    }

    @AfterEach
    void tearDown() {
        mockedJwtUtil.close();
    }

    @Test
    void testGetUserNotifications_validToken_returnsNotifications() throws Exception {

        when(notificationService.getUserNotifications("user1"))
                .thenReturn(List.of(mockNotification));

        mockMvc.perform(get("/api/notifications/all")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Test notification"));
    }

    @Test
    void testGetUnreadNotifications_validToken_returnsUnreadNotifications() throws Exception {

        when(notificationService.getUnreadUserNotifications("user1"))
                .thenReturn(List.of(mockNotification));

        mockMvc.perform(get("/api/notifications/unread")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    void testMarkNotificationRead_validRequest_returnsUpdatedNotification() throws Exception {

        NotificationResponse updated = new NotificationResponse(
                "1", "user1", "Test notification", "user2", true, LocalDateTime.now()
        );

        when(notificationService.markNotificationRead("user1", "1"))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/notifications/1/read")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void testMarkAllNotificationsRead_validRequest_returnsNoContent() throws Exception {

        doNothing().when(notificationService).markAllNotificationsRead("user1");

        mockMvc.perform(patch("/api/notifications/read-all")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());

        verify(notificationService).markAllNotificationsRead("user1");
    }

    @Test
    void testGetUserNotifications_missingAuthHeader_returnsServerError() throws Exception {

        mockMvc.perform(get("/api/notifications/all"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(notificationService);
    }
}