package ro.facultate.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.facultate.pos.entity.Produs;

import java.util.List;

public interface ProdusRepository extends JpaRepository<Produs, Long> {
    List<Produs> findByCategorieId(Long categorieId);
    boolean existsByCategorieId(Long categorieId);
}
