package ucn.cl.factous.backArquitectura.shared.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ucn.cl.factous.backArquitectura.shared.dto.PurchaseTicketDTO;
import ucn.cl.factous.backArquitectura.shared.dto.TicketDTO;
import ucn.cl.factous.backArquitectura.shared.entity.Ticket;
import ucn.cl.factous.backArquitectura.shared.service.TicketService;

@RestController
@RequestMapping("/tickets")
@CrossOrigin(origins = {"${FRONT_URI}", "${FRONT_URI_ALTERNATIVE}"})
public class TicketController {

    @Autowired
    private TicketService ticketService;

    // Endpoints para clientes - gestionar sus tickets
    @GetMapping("/user/{userId}")
    public List<TicketDTO> getTicketsByUser(@PathVariable Long userId) {
        return ticketService.getTicketsByUser(userId);
    }

    // Endpoint para que los clientes compren tickets
    @PostMapping("/purchase")
    public ResponseEntity<?> purchaseTicket(@RequestBody PurchaseTicketDTO purchaseDTO) {
        try {
            TicketDTO ticket = ticketService.purchaseTicket(purchaseDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al procesar la compra");
        }
    }

    // Endpoint para organizadores/propietarios - ver tickets vendidos de un evento
    @GetMapping("/event/{eventId}")
    public List<TicketDTO> getTicketsByEvent(@PathVariable Long eventId) {
        return ticketService.getTicketsByEvent(eventId);
    }

    // Endpoint para validar QR codes
    @PostMapping("/validate-qr")
    public ResponseEntity<?> validateQRCode(@RequestParam String qrData) {
        try {
            // Extraer ticket ID del QR
            String[] parts = qrData.split("\\|");
            String ticketIdPart = parts[0]; // "TICKET_ID:123"
            Long ticketId = Long.parseLong(ticketIdPart.split(":")[1]);
            
            Optional<Ticket> ticketOpt = ticketService.getTicketById(ticketId);
            if (!ticketOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Ticket no válido");
            }
            
            Ticket ticket = ticketOpt.get();
            return ResponseEntity.ok(ticketService.convertToDto(ticket));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("QR Code inválido: " + e.getMessage());
        }
    }
}
