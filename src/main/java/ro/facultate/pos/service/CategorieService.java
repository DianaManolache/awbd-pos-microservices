package ro.facultate.pos.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.dto.CreateCategorieRequest;
import ro.facultate.pos.dto.UpdateCategorieRequest;
import ro.facultate.pos.entity.Categorie;
import ro.facultate.pos.repository.CategorieRepository;
import ro.facultate.pos.repository.ProdusRepository;

import java.util.List;

@Service
public class CategorieService {

    private final CategorieRepository categorieRepository;
    private final ProdusRepository produsRepository;

    public CategorieService(CategorieRepository categorieRepository, ProdusRepository produsRepository) {
        this.categorieRepository = categorieRepository;
        this.produsRepository = produsRepository;
    }

    public Categorie create(CreateCategorieRequest req) {
        Categorie c = new Categorie();
        c.setNume(req.getNume());
        return categorieRepository.save(c);
    }

    public List<Categorie> getAll() {
        return categorieRepository.findAll();
    }

    public Categorie getById(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categorie not found"));
    }

    public Categorie update(Long id, UpdateCategorieRequest req) {
        Categorie c = getById(id);
        c.setNume(req.getNume());
        return categorieRepository.save(c);
    }

    public void delete(Long id) {
        Categorie c = getById(id);
        if (produsRepository.existsByCategorieId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria are produse asociate");
        }
        categorieRepository.delete(c);
    }
}
