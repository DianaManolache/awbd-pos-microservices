package ro.facultate.pos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import ro.facultate.pos.dto.UpdateProdusRequest;
import ro.facultate.pos.entity.Categorie;
import ro.facultate.pos.entity.Produs;
import ro.facultate.pos.repository.CategorieRepository;
import ro.facultate.pos.repository.ProdusRepository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Verifica efectiv comportamentul de caching (nu doar ca aplicatia porneste
 * cu Redis pe classpath) - un al doilea apel la getById nu trebuie sa mai
 * cheme repository-ul, iar un write trebuie sa invalideze cache-ul.
 */
@SpringBootTest
@ActiveProfiles("test")
class ProdusServiceCachingTest {

    @Autowired
    private ProdusService produsService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private ProdusRepository produsRepository;

    @MockBean
    private CategorieRepository categorieRepository;

    @BeforeEach
    void clearCache() {
        cacheManager.getCache("produse").clear();
    }

    @Test
    void getById_secondCall_doesNotHitRepository() {
        Produs produs = new Produs();
        produs.setId(1L);
        produs.setNume("Paine");
        Mockito.when(produsRepository.findById(1L)).thenReturn(Optional.of(produs));

        produsService.getById(1L);
        produsService.getById(1L);

        Mockito.verify(produsRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    void update_evictsCache_soNextGetByIdHitsRepositoryAgain() {
        Produs produs = new Produs();
        produs.setId(1L);
        produs.setNume("Paine");
        produs.setPret(BigDecimal.valueOf(3.5));
        produs.setStoc(10);

        Categorie categorie = new Categorie();
        categorie.setId(1L);

        Mockito.when(produsRepository.findById(1L)).thenReturn(Optional.of(produs));
        Mockito.when(categorieRepository.findById(1L)).thenReturn(Optional.of(categorie));
        Mockito.when(produsRepository.save(Mockito.any(Produs.class))).thenAnswer(inv -> inv.getArgument(0));

        produsService.getById(1L);

        UpdateProdusRequest req = new UpdateProdusRequest();
        req.setNume("Paine integrala");
        req.setPret(BigDecimal.valueOf(4.0));
        req.setStoc(5);
        req.setCategorieId(1L);
        produsService.update(1L, req);

        produsService.getById(1L);

        // 1 pentru primul getById + 1 pentru getById intern din update() (auto-invocare,
        // nu trece prin proxy-ul de cache) + 1 pentru getById-ul final, dupa evictie
        Mockito.verify(produsRepository, Mockito.times(3)).findById(1L);
    }
}
