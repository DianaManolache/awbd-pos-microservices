package ro.facultate.pos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ro.facultate.pos.entity.Notificare;
import ro.facultate.pos.repository.NotificareRepository;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotificareServiceTest {

    private NotificareRepository notificareRepository;
    private NotificareService notificareService;

    @BeforeEach
    void setUp() {
        notificareRepository = Mockito.mock(NotificareRepository.class);
        notificareService = new NotificareService(notificareRepository);
    }

    @Test
    void inregistreaza_savesNotificareWithGivenFields() {
        Mockito.when(notificareRepository.save(Mockito.any(Notificare.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Notificare saved = notificareService.inregistreaza("BON_PLATIT", "Bon 1 platit", Map.of("bonId", 1L));

        assertEquals("BON_PLATIT", saved.getTip());
        assertEquals("Bon 1 platit", saved.getMesaj());
        assertEquals(1L, saved.getDetalii().get("bonId"));
        assertNotNull(saved.getPrimitaLa());

        ArgumentCaptor<Notificare> captor = ArgumentCaptor.forClass(Notificare.class);
        Mockito.verify(notificareRepository).save(captor.capture());
        assertEquals("BON_PLATIT", captor.getValue().getTip());
    }

    @Test
    void getPage_delegatesToRepository() {
        Notificare n = new Notificare("STOC_EPUIZAT", "mesaj", Map.of(), null);
        Mockito.when(notificareRepository.findAll(Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(n)));

        var page = notificareService.getPage(PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
    }
}
