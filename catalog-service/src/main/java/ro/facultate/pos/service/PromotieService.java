package ro.facultate.pos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
import java.util.HashSet;
import java.util.List;

@Service
public class PromotieService {

    private static final String CACHE = "promotii";

    private static final Logger log = LoggerFactory.getLogger(PromotieService.class);

    private final PromotieRepository promotieRepository;
    private final ProdusRepository produsRepository;

    public PromotieService(PromotieRepository promotieRepository, ProdusRepository produsRepository) {
        this.promotieRepository = promotieRepository;
        this.produsRepository = produsRepository;
    }

    @CacheEvict(value = CACHE, allEntries = true)
    public Promotie create(CreatePromotieRequest req) {
        validateInterval(req.getDataStart(), req.getDataFinal());

        Promotie p = new Promotie();
        p.setNume(req.getNume());
        p.setProcentReducere(req.getProcentReducere());
        p.setDataStart(req.getDataStart());
        p.setDataFinal(req.getDataFinal());
        p.setActiva(true);

        Promotie saved = promotieRepository.save(p);
        log.info("Promotie creata cu id {}", saved.getId());
        return saved;
    }

    @Cacheable(value = CACHE, key = "'all'")
    public List<Promotie> getAll() {
        List<Promotie> promotii = promotieRepository.findAll();
        promotii.forEach(this::detachProduse);
        return promotii;
    }

    @Cacheable(value = CACHE, key = "#id")
    public Promotie getById(Long id) {
        Promotie promotie = promotieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotie not found"));
        detachProduse(promotie);
        return promotie;
    }

    /**
     * Inlocuieste colectia lazy Hibernate (PersistentSet) cu un HashSet
     * simplu, inainte ca entitatea sa fie scrisa in cache-ul Redis.
     * GenericJackson2JsonRedisSerializer salveaza clasa CONCRETA a
     * colectiei ca type-hint ("@class") - pentru o colectie lazy
     * neconvertita, asta e org.hibernate.collection.spi.PersistentSet,
     * o clasa interna Hibernate care nu poate fi reconstruita corect in
     * afara unei sesiuni active. La citire din cache, Jackson incearca
     * sa instantieze exact acea clasa, rezultand un PersistentSet "fara
     * sesiune" care arunca LazyInitializationException la prima
     * iterare (ex. in formularul de editare promotie).
     */
    private void detachProduse(Promotie promotie) {
        promotie.setProduse(new HashSet<>(promotie.getProduse()));
    }

    @CacheEvict(value = CACHE, allEntries = true)
    public Promotie update(Long id, UpdatePromotieRequest req) {
        validateInterval(req.getDataStart(), req.getDataFinal());

        Promotie p = getById(id);
        p.setNume(req.getNume());
        p.setProcentReducere(req.getProcentReducere());
        p.setDataStart(req.getDataStart());
        p.setDataFinal(req.getDataFinal());
        p.setActiva(req.getActiva());

        Promotie saved = promotieRepository.save(p);
        log.info("Promotie actualizata cu id {}", saved.getId());
        return saved;
    }

    @CacheEvict(value = CACHE, allEntries = true)
    public void delete(Long id) {
        Promotie p = getById(id);
        promotieRepository.delete(p);
        log.info("Promotie stearsa cu id {}", id);
    }

    @CacheEvict(value = CACHE, allEntries = true)
    public Promotie addProdus(Long promotieId, Long produsId) {
        Promotie promotie = getById(promotieId);
        Produs produs = produsRepository.findById(produsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produs not found"));

        if (promotie.getProduse().contains(produs)) {
            log.info("Asociere produs {} la promotia {} respinsa: deja asociat", produsId, promotieId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produsul este deja in promotie");
        }

        promotie.getProduse().add(produs);
        Promotie saved = promotieRepository.save(promotie);
        log.debug("Produs {} asociat promotiei {}", produsId, promotieId);
        return saved;
    }

    @CacheEvict(value = CACHE, allEntries = true)
    public Promotie removeProdus(Long promotieId, Long produsId) {
        Promotie promotie = getById(promotieId);
        Produs produs = produsRepository.findById(produsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produs not found"));

        if (!promotie.getProduse().remove(produs)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produsul nu este in aceasta promotie");
        }

        Promotie saved = promotieRepository.save(promotie);
        log.debug("Produs {} dezasociat de promotia {}", produsId, promotieId);
        return saved;
    }

    private void validateInterval(LocalDateTime dataStart, LocalDateTime dataFinal) {
        if (!dataFinal.isAfter(dataStart)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data finala trebuie sa fie dupa data start");
        }
    }
}
