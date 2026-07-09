package ro.facultate.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.facultate.pos.entity.Promotie;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PromotieRepository extends JpaRepository<Promotie, Long> {
    boolean existsByProduseId(Long produsId);

    @Query("SELECT MAX(p.procentReducere) FROM Promotie p JOIN p.produse pr "
            + "WHERE pr.id = :produsId AND p.activa = true "
            + "AND :acum BETWEEN p.dataStart AND p.dataFinal")
    Optional<BigDecimal> findReducereMaximaActiva(@Param("produsId") Long produsId, @Param("acum") LocalDateTime acum);
}
