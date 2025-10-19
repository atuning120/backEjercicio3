package ucn.cl.factous.backArquitectura.modules.notification;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ucn.cl.factous.backArquitectura.modules.notification.controller.NotificationController;
import ucn.cl.factous.backArquitectura.modules.notification.dto.NotificationDTO;
import ucn.cl.factous.backArquitectura.modules.notification.service.NotificationService;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private NotificationDTO createTestNotificationDTO(String type, String title, Long userId) {
        return new NotificationDTO(type, title, "Test Message", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), 1L, userId, false);
    }

    @Test
    void shouldReturnNotificationsForValidUser() throws Exception {
        Long userId = 1L;
        List<NotificationDTO> notifications = Arrays.asList(createTestNotificationDTO("general", "Welcome", userId));

        when(notificationService.getNotificationsByUser(userId)).thenReturn(notifications);

        mockMvc.perform(get("/notifications/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Welcome"));

        verify(notificationService, times(1)).getNotificationsByUser(userId);
    }

    @Test
    void shouldReturn400ForInvalidUserId() throws Exception {
        Long userId = 0L;

        mockMvc.perform(get("/notifications/user/{userId}", userId))
                .andExpect(status().isBadRequest());

        verify(notificationService, times(0)).getNotificationsByUser(anyLong());
    }

    @Test
    void shouldMarkNotificationAsReadSuccessfully() throws Exception {
        Long notificationId = 10L;

        doNothing().when(notificationService).markAsRead(notificationId);

        mockMvc.perform(post("/notifications/{id}/read", notificationId))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).markAsRead(notificationId);
    }

    @Test
    void shouldSendPurchaseSuccessNotificationSuccessfully() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", 1L);
        payload.put("eventId", 5L);

        doNothing().when(notificationService).sendPurchaseSuccessNotification(1L, 5L);

        mockMvc.perform(post("/notifications/purchase-success")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Notificación de compra enviada"));

        verify(notificationService, times(1)).sendPurchaseSuccessNotification(1L, 5L);
    }

    @Test
    void shouldReturn500WhenPurchaseSuccessThrowsException() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", 1L);
        payload.put("eventId", 5L);

        doThrow(new RuntimeException("Service failure")).when(notificationService).sendPurchaseSuccessNotification(1L, 5L);

        mockMvc.perform(post("/notifications/purchase-success")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error al enviar la notificación: Service failure")));
    }

    @Test
    void shouldSendEventDeletedNotificationSuccessfully() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", 5L);

        doNothing().when(notificationService).sendEventDeletedNotification(5L);

        mockMvc.perform(post("/notifications/event-deleted")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Notificación de evento eliminado enviada"));

        verify(notificationService, times(1)).sendEventDeletedNotification(5L);
    }

    @Test
    void shouldSendOrganizerMessageNotificationSuccessfully() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", 5L);
        payload.put("customMessage", "Importante: Cambio de hora.");

        doNothing().when(notificationService).sendOrganizerMessageNotification(5L, "Importante: Cambio de hora.");

        mockMvc.perform(post("/notifications/organizer-message")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Notificación de mensaje de organizador enviada"));

        verify(notificationService, times(1)).sendOrganizerMessageNotification(5L, "Importante: Cambio de hora.");
    }

    @Test
    void shouldSendGeneralNotificationSuccessfully() throws Exception {
        NotificationDTO dto = createTestNotificationDTO("general", "General Title", null);
        
        doNothing().when(notificationService).sendGeneralNotification(dto.getType(), dto.getTitle(), dto.getMessage());

        mockMvc.perform(post("/notifications/general")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Notificación general enviada y persistida."));

        verify(notificationService, times(1)).sendGeneralNotification(dto.getType(), dto.getTitle(), dto.getMessage());
    }
}