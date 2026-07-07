package ro.facultate.pos.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ro.facultate.pos.entity.Notificare;

public interface NotificareRepository extends MongoRepository<Notificare, String> {
}
