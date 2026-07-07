package ro.facultate.pos.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.dto.CreateClientRequest;
import ro.facultate.pos.dto.UpdateClientRequest;
import ro.facultate.pos.entity.Client;
import ro.facultate.pos.repository.BonRepository;
import ro.facultate.pos.repository.ClientRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ClientServiceTest {

    private final ClientRepository clientRepository = Mockito.mock(ClientRepository.class);
    private final BonRepository bonRepository = Mockito.mock(BonRepository.class);
    private final ClientService clientService = new ClientService(clientRepository, bonRepository);

    @Test
    void create_shouldSaveClient() {
        CreateClientRequest req = new CreateClientRequest();
        req.setNume("Maria");
        req.setEmail("maria@example.com");
        req.setTelefon("0722000000");

        Client saved = new Client();
        saved.setId(1L);
        saved.setNume("Maria");
        saved.setEmail("maria@example.com");
        saved.setTelefon("0722000000");

        Mockito.when(clientRepository.save(Mockito.any(Client.class))).thenReturn(saved);

        Client result = clientService.create(req);

        assertEquals(1L, result.getId());
        assertEquals("Maria", result.getNume());

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        Mockito.verify(clientRepository).save(captor.capture());
        assertEquals("Maria", captor.getValue().getNume());
        assertEquals("maria@example.com", captor.getValue().getEmail());
        assertEquals("0722000000", captor.getValue().getTelefon());
    }

    @Test
    void getAll_shouldReturnList() {
        Mockito.when(clientRepository.findAll()).thenReturn(List.of(new Client(), new Client()));

        List<Client> result = clientService.getAll();

        assertEquals(2, result.size());
        Mockito.verify(clientRepository).findAll();
    }

    @Test
    void getById_notFound_throwsNotFound() {
        Mockito.when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> clientService.getById(99L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void update_shouldUpdateFields() {
        Client existing = new Client();
        existing.setId(1L);
        existing.setNume("Maria");

        Mockito.when(clientRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(clientRepository.save(Mockito.any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateClientRequest req = new UpdateClientRequest();
        req.setNume("Maria Popescu");
        req.setEmail("maria.popescu@example.com");
        req.setTelefon("0733000000");

        Client result = clientService.update(1L, req);

        assertEquals("Maria Popescu", result.getNume());
        assertEquals("maria.popescu@example.com", result.getEmail());
        assertEquals("0733000000", result.getTelefon());
    }

    @Test
    void delete_shouldRemoveClient() {
        Client existing = new Client();
        existing.setId(1L);

        Mockito.when(clientRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(bonRepository.existsByClientId(1L)).thenReturn(false);

        clientService.delete(1L);

        Mockito.verify(clientRepository).delete(existing);
    }

    @Test
    void delete_withBonuri_throwsBadRequest() {
        Client existing = new Client();
        existing.setId(1L);

        Mockito.when(clientRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(bonRepository.existsByClientId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> clientService.delete(1L));

        assertEquals(400, ex.getStatusCode().value());
    }
}
