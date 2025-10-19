package ucn.cl.factous.backArquitectura.shared.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import ucn.cl.factous.backArquitectura.modules.user.entity.User;

@Entity
@Table(name = "sales")
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "sale")
    private List<Ticket> tickets;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private User buyer;
    private String saleDate;
    private Double amount;

    public Sale() {}

    public Sale(User buyer, String saleDate, Double amount) {
        this.buyer = buyer;
        this.saleDate = saleDate;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTicket(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Sale [id=" + id + ", tickets=" + tickets + ", buyer=" + buyer.toString() + ", saleDate=" + saleDate + ", amount=" + amount + "]";
    }
}
