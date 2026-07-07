package ro.facultate.pos.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.client.UserClient;
import ro.facultate.pos.dto.CreateVanzatorRequest;
import ro.facultate.pos.dto.UpdateVanzatorRequest;
import ro.facultate.pos.entity.Vanzator;
import ro.facultate.pos.repository.BonRepository;
import ro.facultate.pos.repository.VanzatorRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VanzatorServiceTest {

    private final VanzatorRepository vanzatorRepository = Mockito.mock(VanzatorRepository.class);
    private final BonRepository bonRepository = Mockito.mock(BonRepository.class);
    private final UserClient userClient = Mockito.mock(UserClient.class);
    private final VanzatorService vanzatorService = new VanzatorService(vanzatorRepository, bonRepository, userClient);

    @Test
    void create_shouldSaveVanzator() {
        CreateVanzatorRequest req = new CreateVanzatorRequest();
        req.setNume("Vanzator 1");

        Vanzator saved = new Vanzator();
        saved.setId(1L);
        saved.setNume("Vanzator 1");

        Mockito.when(vanzatorRepository.save(Mockito.any(Vanzator.class))).thenReturn(saved);

        Vanzator result = vanzatorService.create(req);

        assertEquals(1L, result.getId());
        assertEquals("Vanzator 1", result.getNume());

        ArgumentCaptor<Vanzator> captor = ArgumentCaptor.forClass(Vanzator.class);
        Mockito.verify(vanzatorRepository).save(captor.capture());
        assertEquals("Vanzator 1", captor.getValue().getNume());
    }

    @Test
    void getAll_shouldReturnList() {
        Mockito.when(vanzatorRepository.findAll()).thenReturn(List.of(new Vanzator()));

        List<Vanzator> result = vanzatorService.getAll();

        assertEquals(1, result.size());
        Mockito.verify(vanzatorRepository).findAll();
    }

    @Test
    void getById_notFound_throwsNotFound() {
        Mockito.when(vanzatorRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> vanzatorService.getById(99L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void update_shouldUpdateNume() {
        Vanzator existing = new Vanzator();
        existing.setId(1L);
        existing.setNume("Vanzator 1");

        Mockito.when(vanzatorRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(vanzatorRepository.save(Mockito.any(Vanzator.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateVanzatorRequest req = new UpdateVanzatorRequest();
        req.setNume("Vanzator Principal");

        Vanzator result = vanzatorService.update(1L, req);

        assertEquals("Vanzator Principal", result.getNume());
    }

    @Test
    void delete_shouldRemoveVanzator() {
        Vanzator existing = new Vanzator();
        existing.setId(1L);

        Mockito.when(vanzatorRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(bonRepository.existsByVanzatorId(1L)).thenReturn(false);
        Mockito.when(userClient.utilizatorExistaPentruVanzator(1L)).thenReturn(false);

        vanzatorService.delete(1L);

        Mockito.verify(vanzatorRepository).delete(existing);
    }

    @Test
    void delete_withBonuri_throwsBadRequest() {
        Vanzator existing = new Vanzator();
        existing.setId(1L);

        Mockito.when(vanzatorRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(bonRepository.existsByVanzatorId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> vanzatorService.delete(1L));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void delete_withUtilizator_throwsBadRequest() {
        Vanzator existing = new Vanzator();
        existing.setId(1L);

        Mockito.when(vanzatorRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(bonRepository.existsByVanzatorId(1L)).thenReturn(false);
        Mockito.when(userClient.utilizatorExistaPentruVanzator(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> vanzatorService.delete(1L));

        assertEquals(400, ex.getStatusCode().value());
    }
}
