package ro.facultate.pos.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ro.facultate.pos.event.BonPlatitEvent;
import ro.facultate.pos.event.StocEpuizatEvent;
import ro.facultate.pos.service.NotificareService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificareListenerTest {

    private NotificareService notificareService;
    private NotificareListener listener;

    @BeforeEach
    void setUp() {
        notificareService = Mockito.mock(NotificareService.class);
        listener = new NotificareListener(notificareService);
    }

    @Test
    void onBonPlatit_savesNotificare() {
        BonPlatitEvent event = new BonPlatitEvent();
        event.setBonId(1L);
        event.setClientId(2L);
        event.setVanzatorId(3L);
        event.setTotal(BigDecimal.valueOf(15.5));
        event.setTipPlata("CARD");
        event.setData(LocalDateTime.now());

        listener.onBonPlatit(event);

        ArgumentCaptor<Map<String, Object>> detaliiCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(notificareService).inregistreaza(
                Mockito.eq("BON_PLATIT"), Mockito.contains("1"), detaliiCaptor.capture());
        assertEquals(1L, detaliiCaptor.getValue().get("bonId"));
        assertEquals(BigDecimal.valueOf(15.5), detaliiCaptor.getValue().get("total"));
    }

    @Test
    void onStocEpuizat_savesNotificare() {
        StocEpuizatEvent event = new StocEpuizatEvent();
        event.setProdusId(5L);
        event.setNume("Paine");
        event.setStoc(0);

        listener.onStocEpuizat(event);

        ArgumentCaptor<Map<String, Object>> detaliiCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(notificareService).inregistreaza(
                Mockito.eq("STOC_EPUIZAT"), Mockito.contains("Paine"), detaliiCaptor.capture());
        assertEquals(5L, detaliiCaptor.getValue().get("produsId"));
        assertTrue(detaliiCaptor.getValue().containsKey("stoc"));
    }
}
