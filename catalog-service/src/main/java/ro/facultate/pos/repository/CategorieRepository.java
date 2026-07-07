package ro.facultate.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.facultate.pos.entity.Categorie;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {}