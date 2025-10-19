package ucn.cl.factous.backArquitectura.modules.payment.dto;

public class PaymentPreferenceDTO {
    private Long eventId;
    private Long userId;
    private Integer quantity;
    private String eventName;
    private Double unitPrice;
    private Double totalAmount;

    public PaymentPreferenceDTO() {}

    public PaymentPreferenceDTO(Long eventId, Long userId, Integer quantity, String eventName, Double unitPrice, Double totalAmount) {
        this.eventId = eventId;
        this.userId = userId;
        this.quantity = quantity;
        this.eventName = eventName;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
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

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
