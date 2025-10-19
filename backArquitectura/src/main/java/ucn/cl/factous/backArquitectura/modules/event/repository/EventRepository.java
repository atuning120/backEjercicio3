package ucn.cl.factous.backArquitectura.modules.event.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ucn.cl.factous.backArquitectura.modules.event.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizerId(Long organizerId);
    List<Event> findBySpotId(Long spotId);
    
    @Query("SELECT e.eventName FROM Event e WHERE e.id = :eventId")
    String findTitleById(@Param("eventId") Long eventId);
}