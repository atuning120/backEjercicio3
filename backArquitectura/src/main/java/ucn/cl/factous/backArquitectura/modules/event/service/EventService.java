package ucn.cl.factous.backArquitectura.modules.event.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ucn.cl.factous.backArquitectura.modules.event.dto.EventDTO;
import ucn.cl.factous.backArquitectura.modules.event.entity.Event;
import ucn.cl.factous.backArquitectura.modules.event.repository.EventRepository;
import ucn.cl.factous.backArquitectura.modules.notification.service.NotificationService;
import ucn.cl.factous.backArquitectura.modules.spot.entity.Spot;
import ucn.cl.factous.backArquitectura.modules.spot.repository.SpotRepository;
import ucn.cl.factous.backArquitectura.modules.user.entity.User;
import ucn.cl.factous.backArquitectura.modules.user.repository.UserRepository;
import ucn.cl.factous.backArquitectura.shared.entity.Ticket;
import ucn.cl.factous.backArquitectura.shared.repository.TicketRepository;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SpotRepository spotRepository;
    @Autowired(required = false)
    private NotificationService notificationService;
    @Autowired
    private TicketRepository ticketRepository;

    public List<EventDTO> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public EventDTO getEventById(Long id) {
        Optional<Event> optionalEvent = eventRepository.findById(id);
        return optionalEvent.map(this::convertToDto).orElse(null);
    }

    public EventDTO createEvent(EventDTO eventDTO) {
        User foundedOrganizer = userRepository.findById(eventDTO.getOrganizerId()).orElse(null);
        if (foundedOrganizer == null) {
            throw new IllegalArgumentException("Organizador con ID " + eventDTO.getOrganizerId() + " no existe.");
        }

        Spot foundedSpot = spotRepository.findById(eventDTO.getSpotId()).orElse(null);
        if (foundedSpot == null) {
            throw new IllegalArgumentException("Spot con ID " + eventDTO.getSpotId() + " no existe.");
        }

        Event convertedEvent = new Event(eventDTO.getEventName(), foundedOrganizer, foundedSpot, eventDTO.getEventDate(), eventDTO.getDescription(), eventDTO.getCategory(), eventDTO.getImageUrl(), eventDTO.getTicketPrice(), eventDTO.getCapacity());
        Event savedEvent = eventRepository.save(convertedEvent);
        return convertToDto(savedEvent);
    }

    public EventDTO updateEvent(Long id, EventDTO eventDTO) {
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if (optionalEvent.isPresent()) {
            Event existingEvent = optionalEvent.get();
            existingEvent.setEventName(eventDTO.getEventName());
            existingEvent.setOrganizer(userRepository.findById(eventDTO.getOrganizerId()).orElse(null));
            existingEvent.setSpot(spotRepository.findById(eventDTO.getSpotId()).orElse(null));
            existingEvent.setEventDate(eventDTO.getEventDate());
            existingEvent.setDescription(eventDTO.getDescription());
            existingEvent.setCategory(eventDTO.getCategory());
            existingEvent.setImageUrl(eventDTO.getImageUrl());
            existingEvent.setTicketPrice(eventDTO.getTicketPrice());
            existingEvent.setCapacity(eventDTO.getCapacity());
            Event updatedEvent = eventRepository.save(existingEvent);
            return convertToDto(updatedEvent);
        }
        return null;
    }

    public boolean deleteEvent(Long id) {
        try {
            System.out.println("=== ELIMINANDO EVENTO ID: " + id + " ===");
            
            // Verificar si el evento existe
            if (!eventRepository.existsById(id)) {
                System.out.println("❌ Evento con ID " + id + " no encontrado");
                return false;
            }
            
            // Obtener título del evento para la notificación
            Optional<Event> eventOptional = eventRepository.findById(id);
            String eventTitle = eventOptional.map(Event::getEventName).orElse("Evento desconocido");
            System.out.println("📍 Evento encontrado: \"" + eventTitle + "\"");
            
            // 1. PRIMERO: Obtener los IDs de usuarios afectados ANTES de eliminar tickets
            List<Long> affectedUserIds = ticketRepository.findDistinctUserIdsByEventId(id);
            if (!affectedUserIds.isEmpty()) {
                System.out.println("👥 Usuarios afectados: " + affectedUserIds.size());
            }
            
            // 2. Eliminar tickets asociados
            List<Ticket> tickets = ticketRepository.findByEventId(id);
            if (!tickets.isEmpty()) {
                System.out.println("🎫 Eliminando " + tickets.size() + " tickets...");
                ticketRepository.deleteAll(tickets);
                System.out.println("✅ Tickets eliminados");
            }
            
            // 3. Eliminar el evento
            eventRepository.deleteById(id);
            System.out.println("✅ Evento eliminado de BD");
            
            // 4. Enviar notificaciones a los usuarios afectados
            if (notificationService != null && !affectedUserIds.isEmpty()) {
                System.out.println("📡 Enviando notificaciones a " + affectedUserIds.size() + " usuarios...");
                notificationService.sendEventDeletedNotificationToUsers(id, eventTitle, affectedUserIds);
                System.out.println("✅ Notificaciones enviadas");
            }
            
            System.out.println("🎉 ELIMINACIÓN COMPLETA - ID: " + id);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ ERROR eliminando evento " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar el evento: " + e.getMessage(), e);
        }
    }

    // Métodos específicos para diferentes tipos de usuario
    public List<EventDTO> getEventsByOrganizer(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<EventDTO> getEventsBySpot(Long spotId) {
        return eventRepository.findBySpotId(spotId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private EventDTO convertToDto(Event event) {
        return new EventDTO(
            event.getId(),
            event.getEventName(),
            event.getDescription(),
            event.getOrganizer().getId(),
            event.getSpot().getId(),
            event.getEventDate(),
            event.getCategory(),
            event.getImageUrl(),
            event.getTicketPrice(),
            event.getCapacity()
        );
    }

    private Event convertToEntity(EventDTO eventDTO) {
        Event event = new Event();
        event.setEventName(eventDTO.getEventName());
        event.setDescription(eventDTO.getDescription());
        event.setSpot(spotRepository.findById(eventDTO.getSpotId()).orElse(null));
        event.setEventDate(eventDTO.getEventDate());
        event.setCategory(eventDTO.getCategory());
        event.setImageUrl(eventDTO.getImageUrl());
        return event;
    }
}