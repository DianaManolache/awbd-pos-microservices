package ro.facultate.pos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    private static final String CACHE = "categorii";

    private static final Logger log = LoggerFactory.getLogger(CategorieService.class);

    private final CategorieRepository categorieRepository;
    private final ProdusRepository produsRepository;

    public CategorieService(CategorieRepository categorieRepository, ProdusRepository produsRepository) {
        this.categorieRepository = categorieRepository;
        this.produsRepository = produsRepository;
    }

    @CacheEvict(value = CACHE, allEntries = true)
    public Categorie create(CreateCategorieRequest req) {
        log.debug("Creare categorie cu nume '{}'", req.getNume());
        Categorie c = new Categorie();
        c.setNume(req.getNume());
        Categorie saved = categorieRepository.save(c);
        log.info("Categorie creata cu id {}", saved.getId());
        return saved;
    }

    @Cacheable(value = CACHE, key = "'all'")
    public List<Categorie> getAll() {
        return categorieRepository.findAll();
    }

    @Cacheable(value = CACHE, key = "#id")
    public Categorie getById(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categorie not found"));
    }

    @CacheEvict(value = CACHE, allEntries = true)
    public Categorie update(Long id, UpdateCategorieRequest req) {
        Categorie c = getById(id);
        c.setNume(req.getNume());
        Categorie saved = categorieRepository.save(c);
        log.info("Categorie actualizata cu id {}", saved.getId());
        return saved;
    }

    @CacheEvict(value = CACHE, allEntries = true)
    public void delete(Long id) {
        Categorie c = getById(id);
        if (produsRepository.existsByCategorieId(id)) {
            log.info("Stergere categorie {} respinsa: are produse asociate", id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria are produse asociate");
        }
        categorieRepository.delete(c);
        log.info("Categorie stearsa cu id {}", id);
    }
}
