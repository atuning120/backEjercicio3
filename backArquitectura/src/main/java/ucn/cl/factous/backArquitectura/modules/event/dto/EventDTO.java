package ucn.cl.factous.backArquitectura.modules.event.dto;

import java.util.Date;

public class EventDTO {
    private Long id;
    private String eventName;
    private Long organizerId;
    private Long spotId;
    private Date eventDate;
    private String description;
    private String category;
    private String imageUrl;
    private Double ticketPrice;
    private Integer capacity;

    // Constructor
    public EventDTO() {}

    public EventDTO(Long id, String eventName, String description, Long organizerId, Long spotId, Date eventDate, String category, String imageUrl, Double ticketPrice, Integer capacity) {
        this.id = id;
        this.eventName = eventName;
        this.description = description;
        this.organizerId = organizerId;
        this.spotId = spotId;
        this.eventDate = eventDate;
        this.category = category;
        this.imageUrl = imageUrl;
        this.ticketPrice = ticketPrice;
        this.capacity = capacity;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        if (id != null) {
            this.id = id;
        }
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        if (eventName != null && !eventName.trim().isEmpty()) {
            this.eventName = eventName;
        }
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        if (organizerId != null) {
            this.organizerId = organizerId  ;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description != null && !description.trim().isEmpty()) {
            this.description = description;
        }
    }

    public Long getSpotId() {
        return spotId;
    }

    public void setSpotId(Long spotId) {
        if (spotId != null) {
            this.spotId = spotId;
        }
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        if (eventDate != null) {
            this.eventDate = eventDate;
        }
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        if (category != null && !category.trim().isEmpty()) {
            this.category = category;
        }
    }
    
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            this.imageUrl = imageUrl;
        }
    }

    public Double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(Double ticketPrice) {
        if (ticketPrice != null && ticketPrice >= 0) {
            this.ticketPrice = ticketPrice;
        }
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        if (capacity != null && capacity > 0) {
            this.capacity = capacity;
        }
    }
}