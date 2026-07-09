package ro.facultate.pos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ro.facultate.pos.client.CatalogGateway;
import ro.facultate.pos.messaging.NotificationEventPublisher;
import ro.facultate.pos.dto.AddBonProdusRequest;
import ro.facultate.pos.dto.ProdusResponse;
import ro.facultate.pos.dto.UpdateBonRequest;
import ro.facultate.pos.entity.*;
import ro.facultate.pos.entity.enums.BonStatus;
import ro.facultate.pos.entity.enums.StatusPlata;
import ro.facultate.pos.entity.enums.TipPlata;
import ro.facultate.pos.repository.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BonServiceTest {

    private BonRepository bonRepository;
    private ClientRepository clientRepository;
    private VanzatorRepository vanzatorRepository;
    private BonProdusRepository bonProdusRepository;
    private PlataRepository plataRepository;
    private CatalogGateway catalogClient;
    private NotificationEventPublisher notificationEventPublisher;

    private BonService bonService;

    @BeforeEach
    void setUp() {
        bonRepository = Mockito.mock(BonRepository.class);
        clientRepository = Mockito.mock(ClientRepository.class);
        vanzatorRepository = Mockito.mock(VanzatorRepository.class);
        bonProdusRepository = Mockito.mock(BonProdusRepository.class);
        plataRepository = Mockito.mock(PlataRepository.class);
        catalogClient = Mockito.mock(CatalogGateway.class);
        notificationEventPublisher = Mockito.mock(NotificationEventPublisher.class);

        bonService = new BonService(
                bonRepository,
                clientRepository,
                vanzatorRepository,
                bonProdusRepository,
                plataRepository,
                catalogClient,
                notificationEventPublisher
        );
    }

    private feign.FeignException.BadRequest badRequestFrom(String methodKey) {
        feign.Request request = feign.Request.create(
                feign.Request.HttpMethod.POST, "/api/produse/ajusteaza-stoc",
                Map.of(), null, StandardCharsets.UTF_8, null);
        feign.Response response = feign.Response.builder()
                .status(400)
                .reason("Bad Request")
                .request(request)
                .headers(Map.of())
                .build();
        return (feign.FeignException.BadRequest) feign.FeignException.errorStatus(methodKey, response);
    }

    @Test
    void addProdus_shouldDecreaseStock_andSaveBonProdus() {
        Bon bon = new Bon();
        bon.setId(1L);
        bon.setStatus(BonStatus.OPEN);

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));

        ProdusResponse produs = new ProdusResponse();
        produs.setId(2L);
        produs.setNume("Paine");
        produs.setPret(BigDecimal.valueOf(3.5));
        produs.setStoc(10);

        Mockito.when(catalogClient.getProdus(2L)).thenReturn(produs);
        Mockito.when(bonProdusRepository.save(Mockito.any(BonProdus.class))).thenAnswer(inv -> inv.getArgument(0));

        AddBonProdusRequest req = new AddBonProdusRequest();
        req.setProdusId(2L);
        req.setCantitate(3);

        BonProdus saved = bonService.addProdus(1L, req);

        assertEquals(3, saved.getCantitate());
        assertEquals(BigDecimal.valueOf(3.5), saved.getPretUnitar());
        assertEquals(1L, saved.getBon().getId());
        assertEquals(2L, saved.getProdusId());
        assertEquals("Paine", saved.getProdusNume());

        Mockito.verify(catalogClient).ajusteazaStoc(2L, Map.of("delta", -3));
        Mockito.verify(bonProdusRepository).save(Mockito.any(BonProdus.class));
    }

    @Test
    void addProdus_cuPromotieActiva_foloseastePretulEfectivRedus() {
        Bon bon = new Bon();
        bon.setId(1L);
        bon.setStatus(BonStatus.OPEN);

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));

        ProdusResponse produs = new ProdusResponse();
        produs.setId(2L);
        produs.setNume("Nasturi");
        produs.setPret(BigDecimal.valueOf(2.00));
        produs.setPretEfectiv(BigDecimal.valueOf(1.60));
        produs.setStoc(10);

        Mockito.when(catalogClient.getProdus(2L)).thenReturn(produs);
        Mockito.when(bonProdusRepository.save(Mockito.any(BonProdus.class))).thenAnswer(inv -> inv.getArgument(0));

        AddBonProdusRequest req = new AddBonProdusRequest();
        req.setProdusId(2L);
        req.setCantitate(1);

        BonProdus saved = bonService.addProdus(1L, req);

        assertEquals(0, BigDecimal.valueOf(1.60).compareTo(saved.getPretUnitar()));
    }

    @Test
    void addProdus_shouldThrow_whenStockInsufficient() {
        Bon bon = new Bon();
        bon.setId(1L);
        bon.setStatus(BonStatus.OPEN);

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));

        ProdusResponse produs = new ProdusResponse();
        produs.setId(2L);
        produs.setPret(BigDecimal.valueOf(3.5));
        produs.setStoc(2);

        Mockito.when(catalogClient.getProdus(2L)).thenReturn(produs);
        Mockito.doThrow(badRequestFrom("CatalogClient#ajusteazaStoc"))
                .when(catalogClient).ajusteazaStoc(2L, Map.of("delta", -5));

        AddBonProdusRequest req = new AddBonProdusRequest();
        req.setProdusId(2L);
        req.setCantitate(5);

        assertThrows(RuntimeException.class, () -> bonService.addProdus(1L, req));

        Mockito.verify(bonProdusRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void payBon_shouldCreatePayment_andMarkBonPaid() {
        Client client = new Client();
        client.setId(5L);

        Vanzator vanzator = new Vanzator();
        vanzator.setId(7L);

        Bon bon = new Bon();
        bon.setId(1L);
        bon.setStatus(BonStatus.OPEN);
        bon.setClient(client);
        bon.setVanzator(vanzator);

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));

        BonProdus l1 = new BonProdus();
        l1.setBon(bon);
        l1.setProdusId(10L);
        l1.setProdusNume("Paine");
        l1.setCantitate(2);
        l1.setPretUnitar(BigDecimal.valueOf(3.5));

        BonProdus l2 = new BonProdus();
        l2.setBon(bon);
        l2.setProdusId(11L);
        l2.setProdusNume("Suc");
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

        ArgumentCaptor<Plata> captor = ArgumentCaptor.forClass(Plata.class);
        Mockito.verify(plataRepository).save(captor.capture());
        assertEquals(BigDecimal.valueOf(12.0), captor.getValue().getSuma());

        ArgumentCaptor<ro.facultate.pos.event.BonPlatitEvent> eventCaptor =
                ArgumentCaptor.forClass(ro.facultate.pos.event.BonPlatitEvent.class);
        Mockito.verify(notificationEventPublisher).publishBonPlatit(eventCaptor.capture());
        assertEquals(1L, eventCaptor.getValue().getBonId());
        assertEquals(5L, eventCaptor.getValue().getClientId());
        assertEquals(7L, eventCaptor.getValue().getVanzatorId());
        assertEquals(BigDecimal.valueOf(12.0), eventCaptor.getValue().getTotal());
        assertEquals("CASH", eventCaptor.getValue().getTipPlata());

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

        BonProdus line = new BonProdus();
        line.setId(5L);
        line.setBon(bon);
        line.setProdusId(2L);
        line.setProdusNume("Paine");
        line.setCantitate(3);
        line.setPretUnitar(BigDecimal.valueOf(3.5));

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));
        Mockito.when(bonProdusRepository.findById(5L)).thenReturn(Optional.of(line));
        Mockito.when(bonProdusRepository.save(Mockito.any(BonProdus.class))).thenAnswer(inv -> inv.getArgument(0));

        ro.facultate.pos.dto.UpdateBonProdusRequest req = new ro.facultate.pos.dto.UpdateBonProdusRequest();
        req.setCantitate(5);

        BonProdus result = bonService.updateBonProdus(1L, 5L, req);

        assertEquals(5, result.getCantitate());
        Mockito.verify(catalogClient).ajusteazaStoc(2L, Map.of("delta", -2));
    }

    @Test
    void updateBonProdus_insufficientStock_throwsBadRequest() {
        Bon bon = new Bon();
        bon.setId(1L);
        bon.setStatus(BonStatus.OPEN);

        BonProdus line = new BonProdus();
        line.setId(5L);
        line.setBon(bon);
        line.setProdusId(2L);
        line.setProdusNume("Paine");
        line.setCantitate(3);
        line.setPretUnitar(BigDecimal.valueOf(3.5));

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));
        Mockito.when(bonProdusRepository.findById(5L)).thenReturn(Optional.of(line));
        Mockito.doThrow(badRequestFrom("CatalogClient#ajusteazaStoc"))
                .when(catalogClient).ajusteazaStoc(2L, Map.of("delta", -7));

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

        BonProdus line = new BonProdus();
        line.setId(5L);
        line.setBon(bon);
        line.setProdusId(2L);
        line.setProdusNume("Paine");
        line.setCantitate(3);
        line.setPretUnitar(BigDecimal.valueOf(3.5));

        Mockito.when(bonRepository.findById(1L)).thenReturn(Optional.of(bon));
        Mockito.when(bonProdusRepository.findById(5L)).thenReturn(Optional.of(line));

        bonService.deleteBonProdus(1L, 5L);

        Mockito.verify(catalogClient).ajusteazaStoc(2L, Map.of("delta", 3));
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
