package ro.facultate.pos.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.dto.CreatePromotieRequest;
import ro.facultate.pos.dto.UpdatePromotieRequest;
import ro.facultate.pos.entity.Produs;
import ro.facultate.pos.entity.Promotie;
import ro.facultate.pos.repository.ProdusRepository;
import ro.facultate.pos.repository.PromotieRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotieService {

    private final PromotieRepository promotieRepository;
    private final ProdusRepository produsRepository;

    public PromotieService(PromotieRepository promotieRepository, ProdusRepository produsRepository) {
        this.promotieRepository = promotieRepository;
        this.produsRepository = produsRepository;
    }

    public Promotie create(CreatePromotieRequest req) {
        validateInterval(req.getDataStart(), req.getDataFinal());

        Promotie p = new Promotie();
        p.setNume(req.getNume());
        p.setProcentReducere(req.getProcentReducere());
        p.setDataStart(req.getDataStart());
        p.setDataFinal(req.getDataFinal());
        p.setActiva(true);

        return promotieRepository.save(p);
    }

    public List<Promotie> getAll() {
        return promotieRepository.findAll();
    }

    public Promotie getById(Long id) {
        return promotieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotie not found"));
    }

    public Promotie update(Long id, UpdatePromotieRequest req) {
        validateInterval(req.getDataStart(), req.getDataFinal());

        Promotie p = getById(id);
        p.setNume(req.getNume());
        p.setProcentReducere(req.getProcentReducere());
        p.setDataStart(req.getDataStart());
        p.setDataFinal(req.getDataFinal());
        p.setActiva(req.getActiva());

        return promotieRepository.save(p);
    }

    public void delete(Long id) {
        Promotie p = getById(id);
        promotieRepository.delete(p);
    }

    public Promotie addProdus(Long promotieId, Long produsId) {
        Promotie promotie = getById(promotieId);
        Produs produs = produsRepository.findById(produsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produs not found"));

        if (promotie.getProduse().contains(produs)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produsul este deja in promotie");
        }

        promotie.getProduse().add(produs);
        return promotieRepository.save(promotie);
    }

    public Promotie removeProdus(Long promotieId, Long produsId) {
        Promotie promotie = getById(promotieId);
        Produs produs = produsRepository.findById(produsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produs not found"));

        if (!promotie.getProduse().remove(produs)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produsul nu este in aceasta promotie");
        }

        return promotieRepository.save(promotie);
    }

    private void validateInterval(LocalDateTime dataStart, LocalDateTime dataFinal) {
        if (!dataFinal.isAfter(dataStart)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data finala trebuie sa fie dupa data start");
        }
    }
}
