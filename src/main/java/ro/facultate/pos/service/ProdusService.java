package ro.facultate.pos.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.dto.CreateProdusRequest;
import ro.facultate.pos.dto.UpdateProdusRequest;
import ro.facultate.pos.dto.UpdateStocRequest;
import ro.facultate.pos.entity.Categorie;
import ro.facultate.pos.entity.Produs;
import ro.facultate.pos.repository.BonProdusRepository;
import ro.facultate.pos.repository.CategorieRepository;
import ro.facultate.pos.repository.ProdusRepository;
import ro.facultate.pos.repository.PromotieRepository;

import java.util.List;

@Service
public class ProdusService {

    private final ProdusRepository produsRepository;
    private final CategorieRepository categorieRepository;
    private final BonProdusRepository bonProdusRepository;
    private final PromotieRepository promotieRepository;

    public ProdusService(ProdusRepository produsRepository,
                          CategorieRepository categorieRepository,
                          BonProdusRepository bonProdusRepository,
                          PromotieRepository promotieRepository) {
        this.produsRepository = produsRepository;
        this.categorieRepository = categorieRepository;
        this.bonProdusRepository = bonProdusRepository;
        this.promotieRepository = promotieRepository;
    }

    public Produs create(CreateProdusRequest req) {
        Categorie categorie = categorieRepository.findById(req.getCategorieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categorie not found"));

        Produs p = new Produs();
        p.setNume(req.getNume());
        p.setPret(req.getPret());
        p.setStoc(req.getStoc());
        p.setCategorie(categorie);

        return produsRepository.save(p);
    }

    public List<Produs> getAll() {
        return produsRepository.findAll();
    }

    public List<Produs> getByCategorie(Long categorieId) {
        return produsRepository.findByCategorieId(categorieId);
    }

    public Produs updateStoc(Long produsId, UpdateStocRequest req) {
        Produs produs = produsRepository.findById(produsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produs not found"));

        produs.setStoc(req.getStoc());
        return produsRepository.save(produs);
    }

    public Produs getById(Long id) {
        return produsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produs not found"));
    }

    public Produs update(Long id, UpdateProdusRequest req) {
        Produs produs = getById(id);

        Categorie categorie = categorieRepository.findById(req.getCategorieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categorie not found"));

        produs.setNume(req.getNume());
        produs.setPret(req.getPret());
        produs.setStoc(req.getStoc());
        produs.setCategorie(categorie);

        return produsRepository.save(produs);
    }

    public void delete(Long id) {
        Produs produs = getById(id);

        if (bonProdusRepository.existsByProdusId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produsul este pe cel putin un bon");
        }
        if (promotieRepository.existsByProduseId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produsul este intr-o promotie activa");
        }

        produsRepository.delete(produs);
    }
}
