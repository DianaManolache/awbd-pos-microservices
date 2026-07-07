package ro.facultate.pos.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ro.facultate.pos.entity.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {}