package ucn.cl.factous.backArquitectura.modules.event.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ucn.cl.factous.backArquitectura.modules.event.dto.EventDTO;
import ucn.cl.factous.backArquitectura.modules.event.service.EventService;

@RestController
@RequestMapping("/events")
@CrossOrigin(origins = {"${FRONT_URI}", "${FRONT_URI_ALTERNATIVE}"})
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping
    public List<EventDTO> getAllEvents() {
        return eventService.getAllEvents();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id) {
        EventDTO eventDTO = eventService.getEventById(id);
        if (eventDTO != null) {;
            return ResponseEntity.ok(eventDTO); // Código 200 OK
        } else {
            return ResponseEntity.notFound().build(); // Código 404 Not Found
        }
    }

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@RequestBody EventDTO eventDTO) {
        EventDTO createdEvent = eventService.createEvent(eventDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent); // Código 201 Created
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(@PathVariable Long id, @RequestBody EventDTO eventDTO) {
        EventDTO updatedEvent = eventService.updateEvent(id, eventDTO);
        if (updatedEvent != null) {
            return ResponseEntity.ok(updatedEvent);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        try {
            System.out.println("=== CONTROLADOR: Recibida petición DELETE para evento ID: " + id + " ===");
            boolean isDeleted = eventService.deleteEvent(id);
            if (isDeleted) {
                System.out.println("=== CONTROLADOR: Evento eliminado exitosamente ===");
                return ResponseEntity.noContent().build();
            } else {
                System.out.println("=== CONTROLADOR: Evento no encontrado ===");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("=== CONTROLADOR: Error en eliminación ===");
            System.err.println("Error en controlador: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=== CONTROLADOR: Fin error ===");
            return ResponseEntity.status(500).build();
        }
    }

    // Endpoints específicos para organizadores
    @GetMapping("/organizer/{organizerId}")
    public List<EventDTO> getEventsByOrganizer(@PathVariable Long organizerId) {
        return eventService.getEventsByOrganizer(organizerId);
    }

    // Endpoints específicos para propietarios de spots
    @GetMapping("/spot/{spotId}")
    public List<EventDTO> getEventsBySpot(@PathVariable Long spotId) {
        return eventService.getEventsBySpot(spotId);
    }
}