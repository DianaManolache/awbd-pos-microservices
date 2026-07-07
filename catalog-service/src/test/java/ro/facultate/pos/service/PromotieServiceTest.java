package ro.facultate.pos.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.dto.CreatePromotieRequest;
import ro.facultate.pos.entity.Produs;
import ro.facultate.pos.entity.Promotie;
import ro.facultate.pos.repository.ProdusRepository;
import ro.facultate.pos.repository.PromotieRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PromotieServiceTest {

    private final PromotieRepository promotieRepository = Mockito.mock(PromotieRepository.class);
    private final ProdusRepository produsRepository = Mockito.mock(ProdusRepository.class);
    private final PromotieService promotieService = new PromotieService(promotieRepository, produsRepository);

    private CreatePromotieRequest createRequest(LocalDateTime start, LocalDateTime end) {
        CreatePromotieRequest req = new CreatePromotieRequest();
        req.setNume("Reduceri de vara");
        req.setProcentReducere(BigDecimal.valueOf(15));
        req.setDataStart(start);
        req.setDataFinal(end);
        return req;
    }

    @Test
    void create_shouldSavePromotie() {
        LocalDateTime start = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 7, 31, 0, 0);

        Promotie saved = new Promotie();
        saved.setId(1L);
        saved.setNume("Reduceri de vara");
        Mockito.when(promotieRepository.save(Mockito.any(Promotie.class))).thenReturn(saved);

        Promotie result = promotieService.create(createRequest(start, end));

        assertEquals(1L, result.getId());
        assertEquals("Reduceri de vara", result.getNume());
    }

    @Test
    void create_dataFinalBeforeDataStart_throwsBadRequest() {
        LocalDateTime start = LocalDateTime.of(2026, 7, 31, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 7, 1, 0, 0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> promotieService.create(createRequest(start, end)));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void getAll_shouldReturnList() {
        Mockito.when(promotieRepository.findAll()).thenReturn(List.of(new Promotie()));

        List<Promotie> result = promotieService.getAll();

        assertEquals(1, result.size());
    }

    @Test
    void getById_notFound_throwsNotFound() {
        Mockito.when(promotieRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> promotieService.getById(99L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void delete_shouldRemovePromotie() {
        Promotie existing = new Promotie();
        existing.setId(1L);
        Mockito.when(promotieRepository.findById(1L)).thenReturn(Optional.of(existing));

        promotieService.delete(1L);

        Mockito.verify(promotieRepository).delete(existing);
    }

    @Test
    void addProdus_shouldAddToPromotie() {
        Promotie promotie = new Promotie();
        promotie.setId(1L);

        Produs produs = new Produs();
        produs.setId(2L);

        Mockito.when(promotieRepository.findById(1L)).thenReturn(Optional.of(promotie));
        Mockito.when(produsRepository.findById(2L)).thenReturn(Optional.of(produs));
        Mockito.when(promotieRepository.save(Mockito.any(Promotie.class))).thenAnswer(inv -> inv.getArgument(0));

        Promotie result = promotieService.addProdus(1L, 2L);

        assertTrue(result.getProduse().contains(produs));
    }

    @Test
    void addProdus_alreadyInPromotie_throwsBadRequest() {
        Produs produs = new Produs();
        produs.setId(2L);

        Promotie promotie = new Promotie();
        promotie.setId(1L);
        promotie.getProduse().add(produs);

        Mockito.when(promotieRepository.findById(1L)).thenReturn(Optional.of(promotie));
        Mockito.when(produsRepository.findById(2L)).thenReturn(Optional.of(produs));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> promotieService.addProdus(1L, 2L));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void removeProdus_notInPromotie_throwsNotFound() {
        Produs produs = new Produs();
        produs.setId(2L);

        Promotie promotie = new Promotie();
        promotie.setId(1L);

        Mockito.when(promotieRepository.findById(1L)).thenReturn(Optional.of(promotie));
        Mockito.when(produsRepository.findById(2L)).thenReturn(Optional.of(produs));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> promotieService.removeProdus(1L, 2L));

        assertEquals(404, ex.getStatusCode().value());
    }
}
