package ro.facultate.pos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.dto.CreateUtilizatorRequest;
import ro.facultate.pos.dto.UpdateUtilizatorRequest;
import ro.facultate.pos.entity.Utilizator;
import ro.facultate.pos.entity.Vanzator;
import ro.facultate.pos.repository.UtilizatorRepository;
import ro.facultate.pos.repository.VanzatorRepository;

import java.util.List;

@Service
public class UtilizatorService {

    private static final Logger log = LoggerFactory.getLogger(UtilizatorService.class);

    private final UtilizatorRepository utilizatorRepository;
    private final VanzatorRepository vanzatorRepository;

    public UtilizatorService(UtilizatorRepository utilizatorRepository, VanzatorRepository vanzatorRepository) {
        this.utilizatorRepository = utilizatorRepository;
        this.vanzatorRepository = vanzatorRepository;
    }

    public Utilizator create(CreateUtilizatorRequest req) {
        if (utilizatorRepository.findByUsername(req.getUsername()).isPresent()) {
            log.info("Creare utilizator respinsa: username '{}' deja folosit", req.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username deja folosit");
        }

        Vanzator vanzator = vanzatorRepository.findById(req.getVanzatorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vanzator not found"));

        if (utilizatorRepository.findByVanzatorId(req.getVanzatorId()).isPresent()) {
            log.info("Creare utilizator respinsa: vanzatorul {} are deja un cont", req.getVanzatorId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vanzatorul are deja un cont de utilizator");
        }

        Utilizator u = new Utilizator();
        u.setUsername(req.getUsername());
        u.setPasswordHash(req.getPassword());
        u.setRol(req.getRol());
        u.setActiv(true);
        u.setVanzator(vanzator);

        Utilizator saved = utilizatorRepository.save(u);
        log.info("Utilizator creat cu id {} (username '{}')", saved.getId(), saved.getUsername());
        return saved;
    }

    public List<Utilizator> getAll() {
        return utilizatorRepository.findAll();
    }

    public Utilizator getById(Long id) {
        return utilizatorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilizator not found"));
    }

    public Utilizator update(Long id, UpdateUtilizatorRequest req) {
        Utilizator u = getById(id);

        utilizatorRepository.findByUsername(req.getUsername())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username deja folosit");
                });

        u.setUsername(req.getUsername());
        u.setPasswordHash(req.getPassword());
        u.setRol(req.getRol());
        u.setActiv(req.getActiv());

        Utilizator saved = utilizatorRepository.save(u);
        log.info("Utilizator actualizat cu id {}", saved.getId());
        return saved;
    }

    public void delete(Long id) {
        Utilizator u = getById(id);
        utilizatorRepository.delete(u);
        log.info("Utilizator sters cu id {}", id);
    }
}
