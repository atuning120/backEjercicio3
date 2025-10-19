package ucn.cl.factous.backArquitectura.shared.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ucn.cl.factous.backArquitectura.modules.event.entity.Event;
import ucn.cl.factous.backArquitectura.modules.event.repository.EventRepository;
import ucn.cl.factous.backArquitectura.modules.user.entity.User;
import ucn.cl.factous.backArquitectura.modules.user.repository.UserRepository;
import ucn.cl.factous.backArquitectura.shared.dto.PurchaseTicketDTO;
import ucn.cl.factous.backArquitectura.shared.dto.TicketDTO;
import ucn.cl.factous.backArquitectura.shared.entity.Sale;
import ucn.cl.factous.backArquitectura.shared.entity.Ticket;
import ucn.cl.factous.backArquitectura.shared.repository.SaleRepository;
import ucn.cl.factous.backArquitectura.shared.repository.TicketRepository;

// Este es un service para la lógica de tickets.
@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private SaleRepository saleRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QRCodeService qrCodeService;

    public List<TicketDTO> getTicketsByUser(Long userId) {
        return ticketRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TicketDTO> getTicketsByEvent(Long eventId) {
        return ticketRepository.findByEventId(eventId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public TicketDTO purchaseTicket(PurchaseTicketDTO purchaseDTO) {
        // Validar evento y usuario
        Optional<Event> eventOpt = eventRepository.findById(purchaseDTO.getEventId());
        Optional<User> userOpt = userRepository.findById(purchaseDTO.getUserId());
        
        if (!eventOpt.isPresent()) {
            throw new IllegalArgumentException("Evento no encontrado");
        }
        
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        Event event = eventOpt.get();
        User user = userOpt.get();

        // Verificar capacidad disponible
        List<Ticket> existingTickets = ticketRepository.findByEventId(event.getId());
        if (event.getCapacity() != null && existingTickets.size() >= event.getCapacity()) {
            throw new IllegalArgumentException("No hay capacidad disponible para este evento");
        }

        // Crear la venta
        String saleDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Double totalAmount = event.getTicketPrice() * purchaseDTO.getQuantity();
        
        Sale sale = new Sale(user, saleDate, totalAmount);
        Sale savedSale = saleRepository.save(sale);

        // Crear los tickets
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < purchaseDTO.getQuantity(); i++) {
            Ticket ticket = new Ticket(event.getTicketPrice(), event, user);
            ticket.setSale(savedSale);
            
            // Guardar primero para obtener el ID
            Ticket savedTicket = ticketRepository.save(ticket);
            
            // Generar QR después de tener el ID
            String qrData = qrCodeService.generateTicketQRData(
                savedTicket.getId(),
                event.getEventName(),
                user.getName(),
                event.getEventDate().toString()
            );
            String qrCode = qrCodeService.generateQRCode(qrData);
            
            // Actualizar ticket con QR
            savedTicket.setQrCode(qrCode);
            tickets.add(ticketRepository.save(savedTicket));
        }

        // Retornar el primer ticket (o podrías retornar todos)
        return convertToDto(tickets.get(0));
    }

    public Optional<Ticket> getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId);
    }

    public TicketDTO convertToDto(Ticket ticket) {
        String eventDate = ticket.getEvent().getEventDate() != null 
            ? ticket.getEvent().getEventDate().toString() 
            : "";
            
        return new TicketDTO(
            ticket.getId(),
            ticket.getPrice(),
            ticket.getEvent().getId(),
            ticket.getuser().getId(),
            ticket.getSale() != null ? ticket.getSale().getId() : null,
            ticket.getEvent().getEventName(),
            eventDate,
            ticket.getQrCode()
        );
    }
}
