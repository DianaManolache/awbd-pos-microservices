package ro.facultate.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.facultate.pos.entity.Bon;

public interface BonRepository extends JpaRepository<Bon, Long> {
    boolean existsByClientId(Long clientId);
    boolean existsByVanzatorId(Long vanzatorId);
}
