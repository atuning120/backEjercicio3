package ucn.cl.factous.backArquitectura.modules.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import ucn.cl.factous.backArquitectura.modules.event.repository.EventRepository;
import ucn.cl.factous.backArquitectura.modules.notification.dto.NotificationDTO;
import ucn.cl.factous.backArquitectura.modules.notification.entity.Notification;
import ucn.cl.factous.backArquitectura.modules.notification.repository.NotificationRepository;
import ucn.cl.factous.backArquitectura.modules.notification.service.NotificationService;
import ucn.cl.factous.backArquitectura.shared.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock(name = "brokerMessagingTemplate")
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification("general", "Test Title", "Test Message", 1L, 100L);
        testNotification.setId(1L);
        testNotification.setTimestamp(LocalDateTime.now());
    }

    @Test
    void shouldReturnUserAndGeneralNotifications() {
        Long userId = 100L;
        Notification userNotification = new Notification("user_specific", "Hello", "Msg", 1L, userId);
        userNotification.setTimestamp(LocalDateTime.now());
        Notification generalNotification = new Notification("general", "All users", "Msg", null, null);
        generalNotification.setTimestamp(LocalDateTime.now());

        List<Notification> userNotifications = Arrays.asList(userNotification);
        List<Notification> generalNotifications = Arrays.asList(generalNotification);

        when(notificationRepository.findByUserId(userId)) .thenAnswer(invocation -> new ArrayList<>(userNotifications));
        when(notificationRepository.findByUserIdIsNull()).thenReturn(generalNotifications);

        List<NotificationDTO> result = notificationService.getNotificationsByUser(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(notificationRepository, times(1)).findByUserId(userId);
        verify(notificationRepository, times(1)).findByUserIdIsNull();
    }

    @Test
    void shouldMarkNotificationAsReadWhenUnread() {
        Long notificationId = 1L;
        testNotification.setRead(false);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        notificationService.markAsRead(notificationId);

        assertTrue(testNotification.isRead());
        verify(notificationRepository, times(1)).findById(notificationId);
        verify(notificationRepository, times(1)).save(testNotification);
    }

    @Test
    void shouldNotSaveWhenAlreadyRead() {
        Long notificationId = 1L;
        testNotification.setRead(true);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(testNotification));

        notificationService.markAsRead(notificationId);

        verify(notificationRepository, times(1)).findById(notificationId);
        verify(notificationRepository, times(0)).save(any(Notification.class));
    }

    @Test
    void shouldDoNothingWhenNotificationNotFound() {
        Long notificationId = 99L;

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        notificationService.markAsRead(notificationId);

        verify(notificationRepository, times(1)).findById(notificationId);
        verify(notificationRepository, times(0)).save(any(Notification.class));
    }

    @Test
    void shouldSendPurchaseSuccessNotification() {
        Long userId = 1L;
        Long eventId = 5L;
        
        when(eventRepository.findTitleById(eventId)).thenReturn("Summer Fest");
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = i.getArgument(0);
            n.setId(2L);
            n.setTimestamp(LocalDateTime.now());
            return n;
        });

        notificationService.sendPurchaseSuccessNotification(userId, eventId);

        verify(eventRepository, times(1)).findTitleById(eventId);
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(
            eq("{\"user\":" + userId + "}"), 
            eq("/queue/notifications"), 
            any(NotificationDTO.class));
    }

    @Test
    void shouldSendEventDeletedNotificationToTicketHolders() {
        Long eventId = 5L;
        List<Long> userIds = Arrays.asList(1L, 2L);

        when(eventRepository.findTitleById(eventId)).thenReturn("Canceled Event");
        when(ticketRepository.findDistinctUserIdsByEventId(eventId)).thenReturn(userIds);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = i.getArgument(0);
            n.setId(n.getUserId());
            n.setTimestamp(LocalDateTime.now());
            return n;
        });

        notificationService.sendEventDeletedNotification(eventId);

        verify(eventRepository, times(1)).findTitleById(eventId);
        verify(ticketRepository, times(1)).findDistinctUserIdsByEventId(eventId);
        verify(notificationRepository, times(2)).save(any(Notification.class)); // Saved for each user
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq("{\"user\":1}"), eq("/queue/notifications"), any(NotificationDTO.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq("{\"user\":2}"), eq("/queue/notifications"), any(NotificationDTO.class));
    }

    @Test
    void shouldNotSendEventDeletedNotificationWhenNoTickets() {
        Long eventId = 5L;

        when(eventRepository.findTitleById(eventId)).thenReturn("Empty Event");
        when(ticketRepository.findDistinctUserIdsByEventId(eventId)).thenReturn(Collections.emptyList());

        notificationService.sendEventDeletedNotification(eventId);

        verify(eventRepository, times(1)).findTitleById(eventId);
        verify(ticketRepository, times(1)).findDistinctUserIdsByEventId(eventId);
        verify(notificationRepository, times(0)).save(any(Notification.class));
        verify(messagingTemplate, times(0)).convertAndSendToUser(anyString(), anyString(), any(NotificationDTO.class));
    }

    @Test
    void shouldSendOrganizerMessageNotification() {
        Long eventId = 5L;
        String message = "Heads up!";
        List<Long> userIds = Arrays.asList(1L);

        when(eventRepository.findTitleById(eventId)).thenReturn("Organized Event");
        when(ticketRepository.findDistinctUserIdsByEventId(eventId)).thenReturn(userIds);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = i.getArgument(0);
            n.setId(1L);
            n.setTimestamp(LocalDateTime.now());
            return n;
        });

        notificationService.sendOrganizerMessageNotification(eventId, message);

        verify(eventRepository, times(1)).findTitleById(eventId);
        verify(ticketRepository, times(1)).findDistinctUserIdsByEventId(eventId);
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq("{\"user\":1}"), eq("/queue/notifications"), any(NotificationDTO.class));
    }

    @Test
    void shouldSendGeneralNotificationToTopic() {
        String type = "general";
        String title = "System Alert";
        String message = "Maintenance planned.";

        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = i.getArgument(0);
            n.setId(3L);
            n.setTimestamp(LocalDateTime.now());
            return n;
        });

        notificationService.sendGeneralNotification(type, title, message);

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications"), any(NotificationDTO.class));
    }
}