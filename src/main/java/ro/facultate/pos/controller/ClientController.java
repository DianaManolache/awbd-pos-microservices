package ro.facultate.pos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.facultate.pos.dto.CreateClientRequest;
import ro.facultate.pos.dto.UpdateClientRequest;
import ro.facultate.pos.entity.Client;
import ro.facultate.pos.service.ClientService;

import java.util.List;

@Tag(
        name = "Clienti",
        description = "Operatii pentru gestionarea clientilor"
)
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @Operation(
            summary = "Creeaza client",
            description = "Creeaza un client nou"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client creat cu succes"),
            @ApiResponse(responseCode = "400", description = "Date invalide (validare)")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Client create(@Valid @RequestBody CreateClientRequest req) {
        return clientService.create(req);
    }

    @Operation(
            summary = "Listeaza clienti",
            description = "Returneaza lista tuturor clientilor"
    )
    @ApiResponse(responseCode = "200", description = "Lista clienti returnata")
    @GetMapping
    public List<Client> getAll() {
        return clientService.getAll();
    }

    @Operation(summary = "Detalii client", description = "Returneaza un client specificat")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client gasit"),
            @ApiResponse(responseCode = "404", description = "Clientul nu exista")
    })
    @GetMapping("/{id}")
    public Client getById(@PathVariable Long id) {
        return clientService.getById(id);
    }

    @Operation(summary = "Actualizeaza client", description = "Actualizeaza datele unui client existent")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client actualizat"),
            @ApiResponse(responseCode = "400", description = "Date invalide (validare)"),
            @ApiResponse(responseCode = "404", description = "Clientul nu exista")
    })
    @PutMapping("/{id}")
    public Client update(@PathVariable Long id, @Valid @RequestBody UpdateClientRequest req) {
        return clientService.update(id, req);
    }

    @Operation(summary = "Sterge client", description = "Sterge un client existent")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Client sters"),
            @ApiResponse(responseCode = "400", description = "Clientul are bonuri asociate"),
            @ApiResponse(responseCode = "404", description = "Clientul nu exista")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        clientService.delete(id);
    }
}
