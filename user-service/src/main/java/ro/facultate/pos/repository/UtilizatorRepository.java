package ro.facultate.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.facultate.pos.entity.Utilizator;

import java.util.Optional;

public interface UtilizatorRepository extends JpaRepository<Utilizator, Long> {
    Optional<Utilizator> findByUsername(String username);
    Optional<Utilizator> findByVanzatorId(Long vanzatorId);
}
