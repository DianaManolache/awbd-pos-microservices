package ro.facultate.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.facultate.pos.entity.BonProdus;

import java.util.List;

public interface BonProdusRepository extends JpaRepository<BonProdus, Long> {
    List<BonProdus> findByBonId(Long bonId);
    boolean existsByProdusId(Long produsId);
}
