package ro.facultate.pos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.dto.CreateClientRequest;
import ro.facultate.pos.dto.UpdateClientRequest;
import ro.facultate.pos.entity.Client;
import ro.facultate.pos.repository.BonRepository;
import ro.facultate.pos.repository.ClientRepository;

import java.util.List;

@Service
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;
    private final BonRepository bonRepository;

    public ClientService(ClientRepository clientRepository, BonRepository bonRepository) {
        this.clientRepository = clientRepository;
        this.bonRepository = bonRepository;
    }

    public Client create(CreateClientRequest req) {
        Client c = new Client();
        c.setNume(req.getNume());
        c.setEmail(req.getEmail());
        c.setTelefon(req.getTelefon());
        Client saved = clientRepository.save(c);
        log.info("Client creat cu id {}", saved.getId());
        return saved;
    }

    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    public Client getById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));
    }

    public Client update(Long id, UpdateClientRequest req) {
        Client c = getById(id);
        c.setNume(req.getNume());
        c.setEmail(req.getEmail());
        c.setTelefon(req.getTelefon());
        Client saved = clientRepository.save(c);
        log.info("Client actualizat cu id {}", saved.getId());
        return saved;
    }

    public void delete(Long id) {
        Client c = getById(id);
        if (bonRepository.existsByClientId(id)) {
            log.info("Stergere client {} respinsa: are bonuri asociate", id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clientul are bonuri asociate");
        }
        clientRepository.delete(c);
        log.info("Client sters cu id {}", id);
    }
}
