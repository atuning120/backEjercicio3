package ucn.cl.factous.backArquitectura.modules.event;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ucn.cl.factous.backArquitectura.modules.event.controller.EventController;
import ucn.cl.factous.backArquitectura.modules.event.dto.EventDTO;
import ucn.cl.factous.backArquitectura.modules.event.service.EventService;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    private EventDTO createTestEventDTO(Long id, String name, Long organizerId, Long spotId) {
        EventDTO dto = new EventDTO();
        dto.setId(id);
        dto.setEventName(name);
        dto.setDescription("Description for " + name);
        dto.setOrganizerId(organizerId);
        dto.setSpotId(spotId);
        dto.setEventDate(Date.from(LocalDateTime.now().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()));
        dto.setCategory("Music");
        dto.setTicketPrice(5000.0);
        dto.setCapacity(100);
        return dto;
    }

    @Test
    void shouldReturnAllEventsSuccessfully() throws Exception {
        List<EventDTO> allEvents = Arrays.asList(
            createTestEventDTO(1L, "Concert A", 10L, 20L),
            createTestEventDTO(2L, "Festival B", 11L, 21L)
        );

        when(eventService.getAllEvents()).thenReturn(allEvents);

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].eventName").value("Concert A"));

        verify(eventService, times(1)).getAllEvents();
    }

    @Test
    void shouldReturnEventByIdSuccessfully() throws Exception {
        Long eventId = 10L;
        EventDTO expectedDto = createTestEventDTO(eventId, "Test Event", 1L, 2L);

        when(eventService.getEventById(eventId)).thenReturn(expectedDto);

        mockMvc.perform(get("/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.eventName").value("Test Event"));

        verify(eventService, times(1)).getEventById(eventId);
    }

    @Test
    void shouldReturn404WhenEventNotFoundById() throws Exception {
        Long eventId = 99L;

        when(eventService.getEventById(eventId)).thenReturn(null);

        mockMvc.perform(get("/events/{id}", eventId))
                .andExpect(status().isNotFound());

        verify(eventService, times(1)).getEventById(eventId);
    }

    @Test
    void shouldCreateEventSuccessfully() throws Exception {
        EventDTO inputDto = createTestEventDTO(null, "New Event", 1L, 2L);
        EventDTO createdDto = createTestEventDTO(50L, "New Event", 1L, 2L);

        when(eventService.createEvent(any(EventDTO.class))).thenReturn(createdDto);

        mockMvc.perform(post("/events")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50L))
                .andExpect(jsonPath("$.eventName").value("New Event"));

        verify(eventService, times(1)).createEvent(any(EventDTO.class));
    }

    @Test
    void shouldUpdateEventSuccessfully() throws Exception {
        Long eventId = 10L;
        EventDTO inputDto = createTestEventDTO(null, "Updated Event", 1L, 2L);
        EventDTO updatedDto = createTestEventDTO(eventId, "Updated Event", 1L, 2L);

        when(eventService.updateEvent(eq(eventId), any(EventDTO.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/events/{id}", eventId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventName").value("Updated Event"));

        verify(eventService, times(1)).updateEvent(eq(eventId), any(EventDTO.class));
    }

    @Test
    void shouldReturn404OnUpdateWhenEventNotFound() throws Exception {
        Long eventId = 99L;
        EventDTO inputDto = createTestEventDTO(null, "NonExistent", 1L, 2L);

        when(eventService.updateEvent(eq(eventId), any(EventDTO.class))).thenReturn(null);

        mockMvc.perform(put("/events/{id}", eventId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound());

        verify(eventService, times(1)).updateEvent(eq(eventId), any(EventDTO.class));
    }

    @Test
    void shouldDeleteEventSuccessfully() throws Exception {
        Long eventId = 6L;

        when(eventService.deleteEvent(eventId)).thenReturn(true);

        mockMvc.perform(delete("/events/{id}", eventId))
                .andExpect(status().isNoContent());

        verify(eventService, times(1)).deleteEvent(eventId);
    }

    @Test
    void shouldReturn404OnDeleteWhenEventNotFound() throws Exception {
        Long eventId = 99L;

        when(eventService.deleteEvent(eventId)).thenReturn(false);

        mockMvc.perform(delete("/events/{id}", eventId))
                .andExpect(status().isNotFound());

        verify(eventService, times(1)).deleteEvent(eventId);
    }

    @Test
    void shouldReturn500OnDeleteWhenServiceThrowsException() throws Exception {
        Long eventId = 6L;

        when(eventService.deleteEvent(eventId)).thenThrow(new RuntimeException("DB Error"));

        mockMvc.perform(delete("/events/{id}", eventId))
                .andExpect(status().isInternalServerError());

        verify(eventService, times(1)).deleteEvent(eventId);
    }

    @Test
    void shouldReturnEventsByOrganizerIdSuccessfully() throws Exception {
        Long organizerId = 1L;
        List<EventDTO> events = Arrays.asList(createTestEventDTO(1L, "Org Event 1", organizerId, 2L));

        when(eventService.getEventsByOrganizer(organizerId)).thenReturn(events);

        mockMvc.perform(get("/events/organizer/{organizerId}", organizerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].organizerId").value(organizerId));

        verify(eventService, times(1)).getEventsByOrganizer(organizerId);
    }

    @Test
    void shouldReturnEventsBySpotIdSuccessfully() throws Exception {
        Long spotId = 5L;
        List<EventDTO> events = Arrays.asList(createTestEventDTO(1L, "Spot Event 1", 1L, spotId));

        when(eventService.getEventsBySpot(spotId)).thenReturn(events);

        mockMvc.perform(get("/events/spot/{spotId}", spotId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].spotId").value(spotId));

        verify(eventService, times(1)).getEventsBySpot(spotId);
    }
}