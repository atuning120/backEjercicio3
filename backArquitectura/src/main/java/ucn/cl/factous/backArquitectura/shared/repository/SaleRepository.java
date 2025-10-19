package ucn.cl.factous.backArquitectura.shared.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ucn.cl.factous.backArquitectura.shared.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByBuyerId(Long buyerId);
}
