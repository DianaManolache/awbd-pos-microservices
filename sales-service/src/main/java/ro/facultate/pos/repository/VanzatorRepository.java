package ro.facultate.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.facultate.pos.entity.Vanzator;

public interface VanzatorRepository extends JpaRepository<Vanzator, Long> {}