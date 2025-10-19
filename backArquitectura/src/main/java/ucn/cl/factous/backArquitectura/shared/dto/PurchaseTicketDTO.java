package ucn.cl.factous.backArquitectura.shared.dto;

public class PurchaseTicketDTO {
    private Long eventId;
    private Long userId;
    private Integer quantity;

    public PurchaseTicketDTO() {}

    public PurchaseTicketDTO(Long eventId, Long userId, Integer quantity) {
        this.eventId = eventId;
        this.userId = userId;
        this.quantity = quantity;
    }

    // Getters y Setters
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
