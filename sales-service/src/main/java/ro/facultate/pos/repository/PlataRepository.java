package ro.facultate.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.facultate.pos.entity.Plata;

import java.util.List;

public interface PlataRepository extends JpaRepository<Plata, Long> {
    List<Plata> findByBonId(Long bonId);
}
