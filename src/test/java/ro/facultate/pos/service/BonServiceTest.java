package ro.facultate.pos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ro.facultate.pos.dto.AddBonProdusRequest;
import ro.facultate.pos.dto.UpdateBonRequest;
import ro.facultate.pos.entity.*;
import ro.facultate.pos.entity.enums.BonStatus;
import ro.facultate.pos.entity.enums.StatusPlata;
import ro.facultate.pos.entity.enums.TipPlata;
import ro.facultate.pos.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BonServiceTest {

    private BonRepository bonRepository;
    private ClientRepository clientRepository;
    private VanzatorRepository vanzatorRepository;
    private BonProdusRepository bonProdusRepository;
    private ProdusRepository produsRepository;
    private PlataRepository plataRepository;

    private BonService bonService;

    @BeforeEach
    void setUp() {
        bonRepository = Mockito.mock(BonRepository.class);
        clientRepository = Mockito.mock(ClientRepository.class);
        vanzatorRepository = Mockito.mock(VanzatorRepository.class);
        bonProdusRepository = Mockito.mock(BonProdusRepository.class);
        produsRepository = Mockito.mock(ProdusRepository.class);
        plataRepository = Mockito.mock(PlataRepository.class);

        bonService = new BonService(
                bonRepository,
                clientRepository,
                vanzatorRepository,
                bonProdusRepository,
                produsRepository,
                plataRepository
        );
    }

    @Test
    void addProdus_shouldDecreaseStock_andSaveBonProdus() {
        // bon OPEN
        Bon bon = new Bon();
        bon.setId(1L);
        bon.setStatus(BonStatus.OPEN);

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));

        // produs cu stoc 10
        Produs produs = new Produs();
        produs.setId(2L);
        produs.setNume("Paine");
        produs.setPret(BigDecimal.valueOf(3.5));
        produs.setStoc(10);

        Mockito.when(produsRepository.findById(2L)).thenReturn(Optional.of(produs));
        Mockito.when(produsRepository.save(Mockito.any(Produs.class))).thenAnswer(inv -> inv.getArgument(0));
        Mockito.when(bonProdusRepository.save(Mockito.any(BonProdus.class))).thenAnswer(inv -> inv.getArgument(0));

        AddBonProdusRequest req = new AddBonProdusRequest();
        req.setProdusId(2L);
        req.setCantitate(3);

        BonProdus saved = bonService.addProdus(1L, req);

        // stoc scazut
        assertEquals(7, produs.getStoc());

        // linia salvata cu pretUnitar = pret produs
        assertEquals(3, saved.getCantitate());
        assertEquals(BigDecimal.valueOf(3.5), saved.getPretUnitar());
        assertEquals(1L, saved.getBon().getId());
        assertEquals(2L, saved.getProdus().getId());

        // verificam ca s a facut save la produs si la bonProdus
        Mockito.verify(produsRepository).save(Mockito.any(Produs.class));
        Mockito.verify(bonProdusRepository).save(Mockito.any(BonProdus.class));
    }

    @Test
    void addProdus_shouldThrow_whenStockInsufficient() {
        Bon bon = new Bon();
        bon.setId(1L);
        bon.setStatus(BonStatus.OPEN);

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));

        Produs produs = new Produs();
        produs.setId(2L);
        produs.setPret(BigDecimal.valueOf(3.5));
        produs.setStoc(2); // insuficient

        Mockito.when(produsRepository.findById(2L)).thenReturn(Optional.of(produs));

        AddBonProdusRequest req = new AddBonProdusRequest();
        req.setProdusId(2L);
        req.setCantitate(5);

        assertThrows(RuntimeException.class, () -> bonService.addProdus(1L, req));

        // n ar trebui sa salveze nimic
        Mockito.verify(bonProdusRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(produsRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void payBon_shouldCreatePayment_andMarkBonPaid() {
        Bon bon = new Bon();
        bon.setId(1L);
        bon.setStatus(BonStatus.OPEN);

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));

        // bon are 2 linii: 2 * 3.5 = 7.0, 1 * 5.0 = 5.0, total 12.0
        Produs p1 = new Produs();
        p1.setId(10L);
        p1.setNume("Paine");
        p1.setPret(BigDecimal.valueOf(3.5));

        BonProdus l1 = new BonProdus();
        l1.setBon(bon);
        l1.setProdus(p1);
        l1.setCantitate(2);
        l1.setPretUnitar(BigDecimal.valueOf(3.5));

        Produs p2 = new Produs();
        p2.setId(11L);
        p2.setNume("Suc");
        p2.setPret(BigDecimal.valueOf(5.0));

        BonProdus l2 = new BonProdus();
        l2.setBon(bon);
        l2.setProdus(p2);
        l2.setCantitate(1);
        l2.setPretUnitar(BigDecimal.valueOf(5.0));

        Mockito.when(bonProdusRepository.findByBonId(1L)).thenReturn(List.of(l1, l2));
        Mockito.when(plataRepository.save(Mockito.any(Plata.class))).thenAnswer(inv -> inv.getArgument(0));
        Mockito.when(bonRepository.save(Mockito.any(Bon.class))).thenAnswer(inv -> inv.getArgument(0));

        Plata plata = bonService.payBon(1L, TipPlata.CASH);

        assertEquals(TipPlata.CASH, plata.getTip());
        assertEquals(BigDecimal.valueOf(12.0), plata.getSuma());
        assertEquals(StatusPlata.SUCCESS, plata.getStatus());
        assertNotNull(plata.getData());

        assertEquals(BonStatus.PAID, bon.getStatus());

        // retinem ce s-a salvat in plataRepository
        ArgumentCaptor<Plata> captor = ArgumentCaptor.forClass(Plata.class);
        Mockito.verify(plataRepository).save(captor.capture());
        assertEquals(BigDecimal.valueOf(12.0), captor.getValue().getSuma());

        Mockito.verify(bonRepository).save(Mockito.any(Bon.class));
    }

    @Test
    void payBon_shouldThrowResponseStatusException_whenBusinessExceptionOccurs() {
        Mockito.when(bonRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> bonService.payBon(999L, TipPlata.CASH));
    }

    @Test
    void getAll_shouldReturnList() {
        Mockito.when(bonRepository.findAll()).thenReturn(List.of(new Bon()));

        List<Bon> result = bonService.getAll();

        assertEquals(1, result.size());
    }

    @Test
    void update_shouldUpdateClientAndVanzator_whenOpenAndEmpty() {
        Bon bon = new Bon();
        bon.setId(1L);
        bon.setStatus(BonStatus.OPEN);

        Client newClient = new Client();
        newClient.setId(2L);

        Vanzator newVanzator = new Vanzator();
        newVanzator.setId(3L);

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));
        Mockito.when(bonProdusRepository.findByBonId(1L)).thenReturn(List.of());
        Mockito.when(clientRepository.findById(2L)).thenReturn(Optional.of(newClient));
        Mockito.when(vanzatorRepository.findById(3L)).thenReturn(Optional.of(newVanzator));
        Mockito.when(bonRepository.save(Mockito.any(Bon.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateBonRequest req = new UpdateBonRequest();
        req.setClientId(2L);
        req.setVanzatorId(3L);

        Bon result = bonService.update(1L, req);

        assertEquals(2L, result.getClient().getId());
        assertEquals(3L, result.getVanzator().getId());
    }

    @Test
    void update_withExistingLines_throwsBadRequest() {
        Bon bon = new Bon();
        bon.setId(1L);
        bon.setStatus(BonStatus.OPEN);

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));
        Mockito.when(bonProdusRepository.findByBonId(1L)).thenReturn(List.of(new BonProdus()));

        UpdateBonRequest req = new UpdateBonRequest();
        req.setClientId(2L);
        req.setVanzatorId(3L);

        org.springframework.web.server.ResponseStatusException ex = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> bonService.update(1L, req));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void delete_shouldRemoveEmptyOpenBon() {
        Bon bon = new Bon();
        bon.setId(1L);
        bon.setStatus(BonStatus.OPEN);

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));
        Mockito.when(bonProdusRepository.findByBonId(1L)).thenReturn(List.of());
        Mockito.when(plataRepository.findByBonId(1L)).thenReturn(List.of());

        bonService.delete(1L);

        Mockito.verify(bonRepository).delete(bon);
    }

    @Test
    void delete_paidBon_throwsBadRequest() {
        Bon bon = new Bon();
        bon.setId(1L);
        bon.setStatus(BonStatus.PAID);

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));

        org.springframework.web.server.ResponseStatusException ex = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> bonService.delete(1L));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void updateBonProdus_shouldAdjustStock_whenQuantityIncreases() {
        Bon bon = new Bon();
        bon.setId(1L);
        bon.setStatus(BonStatus.OPEN);

        Produs produs = new Produs();
        produs.setId(2L);
        produs.setStoc(10);

        BonProdus line = new BonProdus();
        line.setId(5L);
        line.setBon(bon);
        line.setProdus(produs);
        line.setCantitate(3);
        line.setPretUnitar(BigDecimal.valueOf(3.5));

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));
        Mockito.when(bonProdusRepository.findById(5L)).thenReturn(Optional.of(line));
        Mockito.when(produsRepository.save(Mockito.any(Produs.class))).thenAnswer(inv -> inv.getArgument(0));
        Mockito.when(bonProdusRepository.save(Mockito.any(BonProdus.class))).thenAnswer(inv -> inv.getArgument(0));

        ro.facultate.pos.dto.UpdateBonProdusRequest req = new ro.facultate.pos.dto.UpdateBonProdusRequest();
        req.setCantitate(5);

        BonProdus result = bonService.updateBonProdus(1L, 5L, req);

        assertEquals(5, result.getCantitate());
        assertEquals(8, produs.getStoc());
    }

    @Test
    void updateBonProdus_insufficientStock_throwsBadRequest() {
        Bon bon = new Bon();
        bon.setId(1L);
        bon.setStatus(BonStatus.OPEN);

        Produs produs = new Produs();
        produs.setId(2L);
        produs.setStoc(1);

        BonProdus line = new BonProdus();
        line.setId(5L);
        line.setBon(bon);
        line.setProdus(produs);
        line.setCantitate(3);
        line.setPretUnitar(BigDecimal.valueOf(3.5));

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));
        Mockito.when(bonProdusRepository.findById(5L)).thenReturn(Optional.of(line));

        ro.facultate.pos.dto.UpdateBonProdusRequest req = new ro.facultate.pos.dto.UpdateBonProdusRequest();
        req.setCantitate(10);

        org.springframework.web.server.ResponseStatusException ex = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> bonService.updateBonProdus(1L, 5L, req));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void deleteBonProdus_shouldRestoreStock() {
        Bon bon = new Bon();
        bon.setId(1L);
        bon.setStatus(BonStatus.OPEN);

        Produs produs = new Produs();
        produs.setId(2L);
        produs.setStoc(7);

        BonProdus line = new BonProdus();
        line.setId(5L);
        line.setBon(bon);
        line.setProdus(produs);
        line.setCantitate(3);
        line.setPretUnitar(BigDecimal.valueOf(3.5));

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));
        Mockito.when(bonProdusRepository.findById(5L)).thenReturn(Optional.of(line));
        Mockito.when(produsRepository.save(Mockito.any(Produs.class))).thenAnswer(inv -> inv.getArgument(0));

        bonService.deleteBonProdus(1L, 5L);

        assertEquals(10, produs.getStoc());
        Mockito.verify(bonProdusRepository).delete(line);
    }

    @Test
    void updateBonProdus_bonNotOpen_throwsBadRequest() {
        Bon bon = new Bon();
        bon.setId(1L);
        bon.setStatus(BonStatus.PAID);

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));

        ro.facultate.pos.dto.UpdateBonProdusRequest req = new ro.facultate.pos.dto.UpdateBonProdusRequest();
        req.setCantitate(5);

        org.springframework.web.server.ResponseStatusException ex = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> bonService.updateBonProdus(1L, 5L, req));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void getPlata_notFound_throwsNotFound() {
        Mockito.when(plataRepository.findById(999L)).thenReturn(Optional.empty());

        org.springframework.web.server.ResponseStatusException ex = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> bonService.getPlata(1L, 999L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void updatePlata_shouldUpdate_whenNotSuccess() {
        Bon bon = new Bon();
        bon.setId(1L);

        Plata plata = new Plata();
        plata.setId(9L);
        plata.setBon(bon);
        plata.setTip(TipPlata.CASH);
        plata.setSuma(BigDecimal.valueOf(12.0));
        plata.setStatus(StatusPlata.FAILED);

        Mockito.when(plataRepository.findById(9L)).thenReturn(Optional.of(plata));
        Mockito.when(plataRepository.save(Mockito.any(Plata.class))).thenAnswer(inv -> inv.getArgument(0));

        ro.facultate.pos.dto.UpdatePlataRequest req = new ro.facultate.pos.dto.UpdatePlataRequest();
        req.setTip(TipPlata.CARD);
        req.setSuma(BigDecimal.valueOf(20.0));

        Plata result = bonService.updatePlata(1L, 9L, req);

        assertEquals(TipPlata.CARD, result.getTip());
        assertEquals(BigDecimal.valueOf(20.0), result.getSuma());
    }

    @Test
    void updatePlata_success_throwsBadRequest() {
        Bon bon = new Bon();
        bon.setId(1L);

        Plata plata = new Plata();
        plata.setId(9L);
        plata.setBon(bon);
        plata.setStatus(StatusPlata.SUCCESS);

        Mockito.when(plataRepository.findById(9L)).thenReturn(Optional.of(plata));

        ro.facultate.pos.dto.UpdatePlataRequest req = new ro.facultate.pos.dto.UpdatePlataRequest();
        req.setTip(TipPlata.CARD);
        req.setSuma(BigDecimal.valueOf(20.0));

        org.springframework.web.server.ResponseStatusException ex = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> bonService.updatePlata(1L, 9L, req));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void deletePlata_shouldRemove_whenNotSuccess() {
        Bon bon = new Bon();
        bon.setId(1L);

        Plata plata = new Plata();
        plata.setId(9L);
        plata.setBon(bon);
        plata.setStatus(StatusPlata.FAILED);

        Mockito.when(plataRepository.findById(9L)).thenReturn(Optional.of(plata));

        bonService.deletePlata(1L, 9L);

        Mockito.verify(plataRepository).delete(plata);
    }

    @Test
    void deletePlata_success_throwsBadRequest() {
        Bon bon = new Bon();
        bon.setId(1L);

        Plata plata = new Plata();
        plata.setId(9L);
        plata.setBon(bon);
        plata.setStatus(StatusPlata.SUCCESS);

        Mockito.when(plataRepository.findById(9L)).thenReturn(Optional.of(plata));

        org.springframework.web.server.ResponseStatusException ex = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> bonService.deletePlata(1L, 9L));

        assertEquals(400, ex.getStatusCode().value());
    }
}
