package ro.facultate.pos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.client.UserClient;
import ro.facultate.pos.dto.CreateVanzatorRequest;
import ro.facultate.pos.dto.UpdateVanzatorRequest;
import ro.facultate.pos.entity.Vanzator;
import ro.facultate.pos.repository.BonRepository;
import ro.facultate.pos.repository.VanzatorRepository;

import java.util.List;

@Service
public class VanzatorService {

    private static final Logger log = LoggerFactory.getLogger(VanzatorService.class);

    private final VanzatorRepository vanzatorRepository;
    private final BonRepository bonRepository;
    private final UserClient userClient;

    public VanzatorService(VanzatorRepository vanzatorRepository, BonRepository bonRepository, UserClient userClient) {
        this.vanzatorRepository = vanzatorRepository;
        this.bonRepository = bonRepository;
        this.userClient = userClient;
    }

    public Vanzator create(CreateVanzatorRequest req) {
        Vanzator v = new Vanzator();
        v.setNume(req.getNume());
        Vanzator saved = vanzatorRepository.save(v);
        log.info("Vanzator creat cu id {}", saved.getId());
        return saved;
    }

    public List<Vanzator> getAll() {
        return vanzatorRepository.findAll();
    }

    public Vanzator getById(Long id) {
        return vanzatorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vanzator not found"));
    }

    public Vanzator update(Long id, UpdateVanzatorRequest req) {
        Vanzator v = getById(id);
        v.setNume(req.getNume());
        Vanzator saved = vanzatorRepository.save(v);
        log.info("Vanzator actualizat cu id {}", saved.getId());
        return saved;
    }

    public void delete(Long id) {
        Vanzator v = getById(id);

        if (bonRepository.existsByVanzatorId(id)) {
            log.info("Stergere vanzator {} respinsa: are bonuri asociate", id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vanzatorul are bonuri asociate");
        }
        if (userClient.utilizatorExistaPentruVanzator(id)) {
            log.info("Stergere vanzator {} respinsa: are cont de utilizator asociat", id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vanzatorul are un cont de utilizator asociat");
        }

        vanzatorRepository.delete(v);
        log.info("Vanzator sters cu id {}", id);
    }
}
