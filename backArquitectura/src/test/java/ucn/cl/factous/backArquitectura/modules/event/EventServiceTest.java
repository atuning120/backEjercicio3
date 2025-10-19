package ucn.cl.factous.backArquitectura.modules.event;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ucn.cl.factous.backArquitectura.modules.event.dto.EventDTO;
import ucn.cl.factous.backArquitectura.modules.event.entity.Event;
import ucn.cl.factous.backArquitectura.modules.event.repository.EventRepository;
import ucn.cl.factous.backArquitectura.modules.event.service.EventService;
import ucn.cl.factous.backArquitectura.modules.notification.service.NotificationService;
import ucn.cl.factous.backArquitectura.modules.spot.entity.Spot;
import ucn.cl.factous.backArquitectura.modules.spot.repository.SpotRepository;
import ucn.cl.factous.backArquitectura.modules.user.entity.User;
import ucn.cl.factous.backArquitectura.modules.user.repository.UserRepository;
import ucn.cl.factous.backArquitectura.shared.entity.Ticket;
import ucn.cl.factous.backArquitectura.shared.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SpotRepository spotRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private EventService eventService;

    private User testOrganizer;
    private Spot testSpot;
    private EventDTO testEventDTO;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testOrganizer = new User();
        testOrganizer.setId(1L);

        testSpot = new Spot();
        testSpot.setId(2L);

        testEventDTO = new EventDTO();
        testEventDTO.setEventName("Test Event");
        testEventDTO.setOrganizerId(1L);
        testEventDTO.setSpotId(2L);
        testEventDTO.setEventDate(Date.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
        testEventDTO.setTicketPrice(100.0);
        testEventDTO.setCapacity(50);

        testEvent = new Event("Test Event", testOrganizer, testSpot, Date.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant()), "Desc", "Cat", "URL", 100.0, 50);
        testEvent.setId(3L);
        testEvent.setOrganizer(testOrganizer);
        testEvent.setSpot(testSpot);
    }

    @Test
    void shouldReturnAllEvents() {
        when(eventRepository.findAll()).thenReturn(Arrays.asList(testEvent));

        List<EventDTO> result = eventService.getAllEvents();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Event", result.get(0).getEventName());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnEventById() {
        when(eventRepository.findById(3L)).thenReturn(Optional.of(testEvent));

        EventDTO result = eventService.getEventById(3L);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        verify(eventRepository, times(1)).findById(3L);
    }

    @Test
    void shouldReturnNullWhenEventByIdNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        EventDTO result = eventService.getEventById(99L);

        assertNull(result);
        verify(eventRepository, times(1)).findById(99L);
    }

    @Test
    void shouldCreateEventSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testOrganizer));
        when(spotRepository.findById(2L)).thenReturn(Optional.of(testSpot));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        EventDTO result = eventService.createEvent(testEventDTO);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void shouldThrowExceptionWhenOrganizerNotFoundOnCreate() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(testEventDTO);
        });
        verify(eventRepository, times(0)).save(any());
    }

    @Test
    void shouldThrowExceptionWhenSpotNotFoundOnCreate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testOrganizer));
        when(spotRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(testEventDTO);
        });
        verify(eventRepository, times(0)).save(any());
    }

    @Test
    void shouldUpdateEventSuccessfully() {
        EventDTO updateDto = new EventDTO();
        updateDto.setEventName("Updated Name");
        updateDto.setOrganizerId(1L);
        updateDto.setSpotId(2L);
        updateDto.setEventDate(Date.from(LocalDateTime.now().plusDays(2).atZone(ZoneId.systemDefault()).toInstant()));

        when(eventRepository.findById(3L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testOrganizer));
        when(spotRepository.findById(2L)).thenReturn(Optional.of(testSpot));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> {
            Event event = i.getArgument(0);
            event.setId(3L);
            return event;
        });

        EventDTO result = eventService.updateEvent(3L, updateDto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getEventName());
        verify(eventRepository, times(1)).findById(3L);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void shouldReturnNullWhenUpdateEventNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        EventDTO result = eventService.updateEvent(99L, testEventDTO);

        assertNull(result);
        verify(eventRepository, times(1)).findById(99L);
        verify(eventRepository, times(0)).save(any());
    }

    @Test
    void shouldDeleteEventSuccessfullyAndSendNotification() {
        Long eventId = 3L;
        Ticket ticket = new Ticket();
        List<Long> userIds = Arrays.asList(1L, 2L);
        
        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(ticketRepository.findDistinctUserIdsByEventId(eventId)).thenReturn(userIds);
        when(ticketRepository.findByEventId(eventId)).thenReturn(Arrays.asList(ticket));
        doNothing().when(ticketRepository).deleteAll(anyList());
        doNothing().when(eventRepository).deleteById(eventId);
        doNothing().when(notificationService).sendEventDeletedNotificationToUsers(eq(eventId), anyString(), eq(userIds));

        boolean isDeleted = eventService.deleteEvent(eventId);

        assertTrue(isDeleted);
        verify(eventRepository, times(1)).existsById(eventId);
        verify(ticketRepository, times(1)).findDistinctUserIdsByEventId(eventId);
        verify(ticketRepository, times(1)).findByEventId(eventId);
        verify(ticketRepository, times(1)).deleteAll(anyList());
        verify(eventRepository, times(1)).deleteById(eventId);
        verify(notificationService, times(1)).sendEventDeletedNotificationToUsers(eq(eventId), anyString(), eq(userIds));
    }

    @Test
    void shouldReturnFalseWhenDeleteEventNotFound() {
        Long eventId = 99L;

        when(eventRepository.existsById(eventId)).thenReturn(false);

        boolean isDeleted = eventService.deleteEvent(eventId);

        assertFalse(isDeleted);
        verify(eventRepository, times(1)).existsById(eventId);
        verify(eventRepository, times(0)).deleteById(eventId);
    }

    @Test
    void shouldReturnEventsByOrganizer() {
        Long organizerId = 1L;
        when(eventRepository.findByOrganizerId(organizerId)).thenReturn(Arrays.asList(testEvent));

        List<EventDTO> result = eventService.getEventsByOrganizer(organizerId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(organizerId, result.get(0).getOrganizerId());
        verify(eventRepository, times(1)).findByOrganizerId(organizerId);
    }

    @Test
    void shouldReturnEventsBySpot() {
        Long spotId = 2L;
        when(eventRepository.findBySpotId(spotId)).thenReturn(Arrays.asList(testEvent));

        List<EventDTO> result = eventService.getEventsBySpot(spotId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(spotId, result.get(0).getSpotId());
        verify(eventRepository, times(1)).findBySpotId(spotId);
    }
}