package ucn.cl.factous.backArquitectura.shared.dto;

public class TicketDTO {
    private Long id;
    private Double price;
    private Long eventId;
    private Long userId;
    private Long saleId;
    
    // Información adicional del evento para mostrar al usuario
    private String eventName;
    private String eventDate;
    private String qrCode;

    public TicketDTO() {}

    public TicketDTO(Long id, Double price, Long eventId, Long userId, Long saleId, String eventName, String eventDate, String qrCode) {
        this.id = id;
        this.price = price;
        this.eventId = eventId;
        this.userId = userId;
        this.saleId = saleId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.qrCode = qrCode;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

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

    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        this.saleId = saleId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
}
