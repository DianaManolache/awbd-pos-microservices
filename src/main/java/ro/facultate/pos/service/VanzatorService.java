package ro.facultate.pos.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.dto.CreateVanzatorRequest;
import ro.facultate.pos.dto.UpdateVanzatorRequest;
import ro.facultate.pos.entity.Vanzator;
import ro.facultate.pos.repository.BonRepository;
import ro.facultate.pos.repository.UtilizatorRepository;
import ro.facultate.pos.repository.VanzatorRepository;

import java.util.List;

@Service
public class VanzatorService {

    private final VanzatorRepository vanzatorRepository;
    private final BonRepository bonRepository;
    private final UtilizatorRepository utilizatorRepository;

    public VanzatorService(VanzatorRepository vanzatorRepository, BonRepository bonRepository, UtilizatorRepository utilizatorRepository) {
        this.vanzatorRepository = vanzatorRepository;
        this.bonRepository = bonRepository;
        this.utilizatorRepository = utilizatorRepository;
    }

    public Vanzator create(CreateVanzatorRequest req) {
        Vanzator v = new Vanzator();
        v.setNume(req.getNume());
        return vanzatorRepository.save(v);
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
        return vanzatorRepository.save(v);
    }

    public void delete(Long id) {
        Vanzator v = getById(id);

        if (bonRepository.existsByVanzatorId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vanzatorul are bonuri asociate");
        }
        if (utilizatorRepository.findByVanzatorId(id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vanzatorul are un cont de utilizator asociat");
        }

        vanzatorRepository.delete(v);
    }
}
