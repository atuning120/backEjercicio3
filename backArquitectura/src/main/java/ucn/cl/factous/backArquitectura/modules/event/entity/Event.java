package ucn.cl.factous.backArquitectura.modules.event.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import ucn.cl.factous.backArquitectura.modules.spot.entity.Spot;
import ucn.cl.factous.backArquitectura.modules.user.entity.User;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventName;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @ManyToOne
    @JoinColumn(name = "spot_id")
    private Spot spot;

    private Date eventDate;
    private String description;
    private String category;
    private String imageUrl;
    private Double ticketPrice;
    private Integer capacity;

    public Event() {}

    public Event(String eventName, User organizer, Spot spot, Date eventDate, String description, String category, String imageUrl, Double ticketPrice, Integer capacity) {
        this.eventName = eventName;
        this.organizer = organizer;
        this.spot = spot;
        this.eventDate = eventDate;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.ticketPrice = ticketPrice;
        this.capacity = capacity;
    }

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

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        if (organizer != null) {
            this.organizer = organizer;
        }
    }

    public Spot getSpot() {
        return spot;
    }

    public void setSpot(Spot spot) {
        if (spot != null) {
            this.spot = spot;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description != null && !description.trim().isEmpty()) {
            this.description = description;
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

    @Override
    public String toString() {
        return "Event [id=" + id + ", organizer=" + organizer.toString() + ", eventName=" + eventName + ", spot=" + spot.toString() +
               ", eventDate=" + eventDate + ", description=" + description + ", category=" + category + ", imageUrl=" + imageUrl + 
               ", ticketPrice=" + ticketPrice + ", capacity=" + capacity + "]";
    }
}