package ucn.cl.factous.backArquitectura.modules.spot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ucn.cl.factous.backArquitectura.modules.spot.entity.Spot;

public interface SpotRepository extends JpaRepository<Spot, Long> {
    List<Spot> findByOwnerId(Long ownerId);
}