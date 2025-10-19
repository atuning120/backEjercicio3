package ucn.cl.factous.backArquitectura.shared.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ucn.cl.factous.backArquitectura.shared.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUserId(Long userId);
    List<Ticket> findByEventId(Long eventId);

    // Funcion para obtener los usuarios que compren entrada a cierto evento
    @Query("SELECT DISTINCT t.user.id FROM Ticket t WHERE t.event.id = :eventId")
    List<Long> findDistinctUserIdsByEventId(@Param("eventId") Long eventId);
}
