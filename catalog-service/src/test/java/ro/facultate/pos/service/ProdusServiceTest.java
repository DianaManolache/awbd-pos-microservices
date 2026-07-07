package ro.facultate.pos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.client.SalesGateway;
import ro.facultate.pos.messaging.NotificationEventPublisher;
import ro.facultate.pos.dto.AjusteazaStocRequest;
import ro.facultate.pos.dto.CreateProdusRequest;
import ro.facultate.pos.dto.UpdateProdusRequest;
import ro.facultate.pos.dto.UpdateStocRequest;
import ro.facultate.pos.entity.Categorie;
import ro.facultate.pos.entity.Produs;
import ro.facultate.pos.repository.CategorieRepository;
import ro.facultate.pos.repository.ProdusRepository;
import ro.facultate.pos.repository.PromotieRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProdusServiceTest {

    private ProdusRepository produsRepository;
    private CategorieRepository categorieRepository;
    private SalesGateway salesClient;
    private PromotieRepository promotieRepository;
    private NotificationEventPublisher notificationEventPublisher;
    private ProdusService produsService;

    @BeforeEach
    void setUp() {
        produsRepository = Mockito.mock(ProdusRepository.class);
        categorieRepository = Mockito.mock(CategorieRepository.class);
        salesClient = Mockito.mock(SalesGateway.class);
        promotieRepository = Mockito.mock(PromotieRepository.class);
        notificationEventPublisher = Mockito.mock(NotificationEventPublisher.class);
        produsService = new ProdusService(produsRepository, categorieRepository, promotieRepository, salesClient, notificationEventPublisher);
    }

    @Test
    void create_shouldSaveProdus_withCategorie() {
        CreateProdusRequest req = new CreateProdusRequest();
        req.setNume("Paine");
        req.setPret(BigDecimal.valueOf(3.5));
        req.setStoc(10);
        req.setCategorieId(1L);

        Categorie cat = new Categorie();
        cat.setId(1L);
        cat.setNume("Panificatie");

        Mockito.when(categorieRepository.findById(1L)).thenReturn(Optional.of(cat));

        Produs saved = new Produs();
        saved.setId(5L);
        saved.setNume("Paine");
        saved.setPret(BigDecimal.valueOf(3.5));
        saved.setStoc(10);
        saved.setCategorie(cat);

        Mockito.when(produsRepository.save(Mockito.any(Produs.class))).thenReturn(saved);

        Produs result = produsService.create(req);

        assertEquals(5L, result.getId());
        assertEquals("Paine", result.getNume());

        ArgumentCaptor<Produs> captor = ArgumentCaptor.forClass(Produs.class);
        Mockito.verify(produsRepository).save(captor.capture());
        assertEquals(cat, captor.getValue().getCategorie());
    }

    @Test
    void create_shouldThrow404_whenCategorieNotFound() {
        CreateProdusRequest req = new CreateProdusRequest();
        req.setNume("Paine");
        req.setPret(BigDecimal.valueOf(3.5));
        req.setStoc(10);
        req.setCategorieId(999L);

        Mockito.when(categorieRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> produsService.create(req));

        Mockito.verify(produsRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void updateStoc_shouldUpdate_whenProdusExists() {
        Produs produs = new Produs();
        produs.setId(1L);
        produs.setNume("Paine");
        produs.setPret(BigDecimal.valueOf(3.5));
        produs.setStoc(10);

        Mockito.when(produsRepository.findById(1L)).thenReturn(Optional.of(produs));
        Mockito.when(produsRepository.save(Mockito.any(Produs.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateStocRequest req = new UpdateStocRequest();
        req.setStoc(100);

        Produs updated = produsService.updateStoc(1L, req);

        assertEquals(100, updated.getStoc());
        Mockito.verify(produsRepository).save(Mockito.any(Produs.class));
    }

    @Test
    void updateStoc_shouldThrow404_whenProdusNotFound() {
        Mockito.when(produsRepository.findById(999L)).thenReturn(Optional.empty());

        UpdateStocRequest req = new UpdateStocRequest();
        req.setStoc(100);

        assertThrows(ResponseStatusException.class, () -> produsService.updateStoc(999L, req));
        Mockito.verify(produsRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void getById_notFound_throwsNotFound() {
        Mockito.when(produsRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> produsService.getById(99L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void update_shouldReplaceFields() {
        Produs existing = new Produs();
        existing.setId(1L);
        existing.setNume("Paine");
        existing.setPret(BigDecimal.valueOf(3.5));
        existing.setStoc(10);

        Categorie cat = new Categorie();
        cat.setId(2L);

        Mockito.when(produsRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(categorieRepository.findById(2L)).thenReturn(Optional.of(cat));
        Mockito.when(produsRepository.save(Mockito.any(Produs.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProdusRequest req = new UpdateProdusRequest();
        req.setNume("Paine integrala");
        req.setPret(BigDecimal.valueOf(4.5));
        req.setStoc(20);
        req.setCategorieId(2L);

        Produs result = produsService.update(1L, req);

        assertEquals("Paine integrala", result.getNume());
        assertEquals(BigDecimal.valueOf(4.5), result.getPret());
        assertEquals(20, result.getStoc());
        assertEquals(cat, result.getCategorie());
    }

    @Test
    void delete_shouldRemoveProdus() {
        Produs existing = new Produs();
        existing.setId(1L);

        Mockito.when(produsRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(salesClient.produsExistaPeBon(1L)).thenReturn(false);
        Mockito.when(promotieRepository.existsByProduseId(1L)).thenReturn(false);

        produsService.delete(1L);

        Mockito.verify(produsRepository).delete(existing);
    }

    @Test
    void delete_referencedByBonProdus_throwsBadRequest() {
        Produs existing = new Produs();
        existing.setId(1L);

        Mockito.when(produsRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(salesClient.produsExistaPeBon(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> produsService.delete(1L));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void delete_referencedByPromotie_throwsBadRequest() {
        Produs existing = new Produs();
        existing.setId(1L);

        Mockito.when(produsRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(salesClient.produsExistaPeBon(1L)).thenReturn(false);
        Mockito.when(promotieRepository.existsByProduseId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> produsService.delete(1L));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void ajusteazaStoc_deltaNegativValid_scadeStocul() {
        Produs p = new Produs();
        p.setId(1L);
        p.setStoc(10);
        Mockito.when(produsRepository.findById(1L)).thenReturn(Optional.of(p));
        Mockito.when(produsRepository.save(Mockito.any(Produs.class))).thenAnswer(inv -> inv.getArgument(0));

        AjusteazaStocRequest req = new AjusteazaStocRequest();
        req.setDelta(-3);

        Produs result = produsService.ajusteazaStoc(1L, req);

        assertEquals(7, result.getStoc());
    }

    @Test
    void ajusteazaStoc_deltaArDuceLaStocNegativ_aruncaBadRequest() {
        Produs p = new Produs();
        p.setId(1L);
        p.setStoc(2);
        Mockito.when(produsRepository.findById(1L)).thenReturn(Optional.of(p));

        AjusteazaStocRequest req = new AjusteazaStocRequest();
        req.setDelta(-5);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> produsService.ajusteazaStoc(1L, req));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void ajusteazaStoc_stocAjungeLaZero_publicaEvenimentStocEpuizat() {
        Produs p = new Produs();
        p.setId(1L);
        p.setNume("Paine");
        p.setStoc(3);
        Mockito.when(produsRepository.findById(1L)).thenReturn(Optional.of(p));
        Mockito.when(produsRepository.save(Mockito.any(Produs.class))).thenAnswer(inv -> inv.getArgument(0));

        AjusteazaStocRequest req = new AjusteazaStocRequest();
        req.setDelta(-3);

        produsService.ajusteazaStoc(1L, req);

        ArgumentCaptor<ro.facultate.pos.event.StocEpuizatEvent> captor =
                ArgumentCaptor.forClass(ro.facultate.pos.event.StocEpuizatEvent.class);
        Mockito.verify(notificationEventPublisher).publishStocEpuizat(captor.capture());
        assertEquals(1L, captor.getValue().getProdusId());
        assertEquals("Paine", captor.getValue().getNume());
        assertEquals(0, captor.getValue().getStoc());
    }

    @Test
    void ajusteazaStoc_stocRamanePozitiv_nuPublicaEvenimentStocEpuizat() {
        Produs p = new Produs();
        p.setId(1L);
        p.setStoc(10);
        Mockito.when(produsRepository.findById(1L)).thenReturn(Optional.of(p));
        Mockito.when(produsRepository.save(Mockito.any(Produs.class))).thenAnswer(inv -> inv.getArgument(0));

        AjusteazaStocRequest req = new AjusteazaStocRequest();
        req.setDelta(-3);

        produsService.ajusteazaStoc(1L, req);

        Mockito.verify(notificationEventPublisher, Mockito.never()).publishStocEpuizat(Mockito.any());
    }
}
