package ro.facultate.pos.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.client.SalesClient;
import ro.facultate.pos.dto.CreateUtilizatorRequest;
import ro.facultate.pos.dto.UpdateUtilizatorRequest;
import ro.facultate.pos.dto.VanzatorResponse;
import ro.facultate.pos.entity.Utilizator;
import ro.facultate.pos.entity.enums.RolUtilizator;
import ro.facultate.pos.repository.UtilizatorRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UtilizatorServiceTest {

    private final UtilizatorRepository utilizatorRepository = Mockito.mock(UtilizatorRepository.class);
    private final SalesClient salesClient = Mockito.mock(SalesClient.class);
    private final UtilizatorService utilizatorService =
            new UtilizatorService(utilizatorRepository, salesClient, new BCryptPasswordEncoder());

    private CreateUtilizatorRequest createRequest() {
        CreateUtilizatorRequest req = new CreateUtilizatorRequest();
        req.setUsername("ana.vanzatoare");
        req.setPassword("parola123");
        req.setRol(RolUtilizator.USER);
        req.setVanzatorId(1L);
        return req;
    }

    @Test
    void create_shouldSaveUtilizator() {
        VanzatorResponse vanzator = new VanzatorResponse();
        vanzator.setId(1L);

        Mockito.when(utilizatorRepository.findByUsername("ana.vanzatoare")).thenReturn(Optional.empty());
        Mockito.when(salesClient.getVanzator(1L)).thenReturn(vanzator);
        Mockito.when(utilizatorRepository.findByVanzatorId(1L)).thenReturn(Optional.empty());

        Utilizator saved = new Utilizator();
        saved.setId(1L);
        saved.setUsername("ana.vanzatoare");
        Mockito.when(utilizatorRepository.save(Mockito.any(Utilizator.class))).thenReturn(saved);

        Utilizator result = utilizatorService.create(createRequest());

        assertEquals(1L, result.getId());
        assertEquals("ana.vanzatoare", result.getUsername());

        org.mockito.ArgumentCaptor<Utilizator> captor = org.mockito.ArgumentCaptor.forClass(Utilizator.class);
        Mockito.verify(utilizatorRepository).save(captor.capture());
        assertNotEquals("parola123", captor.getValue().getPasswordHash());
        assertTrue(new BCryptPasswordEncoder().matches("parola123", captor.getValue().getPasswordHash()));
    }

    @Test
    void create_duplicateUsername_throwsBadRequest() {
        Mockito.when(utilizatorRepository.findByUsername("ana.vanzatoare"))
                .thenReturn(Optional.of(new Utilizator()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> utilizatorService.create(createRequest()));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void create_vanzatorNotFound_throwsNotFound() {
        Mockito.when(utilizatorRepository.findByUsername("ana.vanzatoare")).thenReturn(Optional.empty());
        Mockito.when(salesClient.getVanzator(1L)).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> utilizatorService.create(createRequest()));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void create_vanzatorAlreadyHasAccount_throwsBadRequest() {
        VanzatorResponse vanzator = new VanzatorResponse();
        vanzator.setId(1L);

        Mockito.when(utilizatorRepository.findByUsername("ana.vanzatoare")).thenReturn(Optional.empty());
        Mockito.when(salesClient.getVanzator(1L)).thenReturn(vanzator);
        Mockito.when(utilizatorRepository.findByVanzatorId(1L)).thenReturn(Optional.of(new Utilizator()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> utilizatorService.create(createRequest()));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void getAll_shouldReturnList() {
        Mockito.when(utilizatorRepository.findAll()).thenReturn(List.of(new Utilizator()));

        List<Utilizator> result = utilizatorService.getAll();

        assertEquals(1, result.size());
    }

    @Test
    void getById_notFound_throwsNotFound() {
        Mockito.when(utilizatorRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> utilizatorService.getById(99L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void update_shouldUpdateFields() {
        Utilizator existing = new Utilizator();
        existing.setId(1L);
        existing.setUsername("ana.vanzatoare");

        Mockito.when(utilizatorRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(utilizatorRepository.findByUsername("ana.noua")).thenReturn(Optional.empty());
        Mockito.when(utilizatorRepository.save(Mockito.any(Utilizator.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUtilizatorRequest req = new UpdateUtilizatorRequest();
        req.setUsername("ana.noua");
        req.setPassword("parolaNoua123");
        req.setRol(RolUtilizator.ADMIN);
        req.setActiv(false);

        Utilizator result = utilizatorService.update(1L, req);

        assertEquals("ana.noua", result.getUsername());
        assertEquals(RolUtilizator.ADMIN, result.getRol());
        assertFalse(result.getActiv());
        assertNotEquals("parolaNoua123", result.getPasswordHash());
        assertTrue(new BCryptPasswordEncoder().matches("parolaNoua123", result.getPasswordHash()));
    }

    @Test
    void delete_shouldRemoveUtilizator() {
        Utilizator existing = new Utilizator();
        existing.setId(1L);
        Mockito.when(utilizatorRepository.findById(1L)).thenReturn(Optional.of(existing));

        utilizatorService.delete(1L);

        Mockito.verify(utilizatorRepository).delete(existing);
    }

    @Test
    void existaUtilizatorPentruVanzator_true() {
        Mockito.when(utilizatorRepository.findByVanzatorId(1L)).thenReturn(Optional.of(new Utilizator()));

        assertTrue(utilizatorService.existaUtilizatorPentruVanzator(1L));
    }

    @Test
    void existaUtilizatorPentruVanzator_false() {
        Mockito.when(utilizatorRepository.findByVanzatorId(1L)).thenReturn(Optional.empty());

        assertFalse(utilizatorService.existaUtilizatorPentruVanzator(1L));
    }
}
