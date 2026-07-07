package ro.facultate.pos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.facultate.pos.dto.CreateUtilizatorRequest;
import ro.facultate.pos.dto.UpdateUtilizatorRequest;
import ro.facultate.pos.entity.Utilizator;
import ro.facultate.pos.service.UtilizatorService;

import java.util.List;

@Tag(
        name = "Utilizatori",
        description = "Operatii pentru gestionarea conturilor de utilizator"
)
@RestController
@RequestMapping("/api/utilizatori")
public class UtilizatorController {

    private final UtilizatorService utilizatorService;

    public UtilizatorController(UtilizatorService utilizatorService) {
        this.utilizatorService = utilizatorService;
    }

    @Operation(
            summary = "Creeaza utilizator",
            description = "Creeaza un cont de utilizator nou pentru un vanzator existent"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utilizator creat cu succes"),
            @ApiResponse(responseCode = "400", description = "Date invalide sau username/vanzator deja asociat"),
            @ApiResponse(responseCode = "404", description = "Vanzatorul nu exista")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Utilizator create(@Valid @RequestBody CreateUtilizatorRequest req) {
        return utilizatorService.create(req);
    }

    @Operation(
            summary = "Listeaza utilizatori",
            description = "Returneaza lista tuturor utilizatorilor"
    )
    @ApiResponse(responseCode = "200", description = "Lista utilizatori")
    @GetMapping
    public List<Utilizator> getAll() {
        return utilizatorService.getAll();
    }

    @Operation(
            summary = "Detalii utilizator",
            description = "Returneaza un utilizator specificat"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilizator gasit"),
            @ApiResponse(responseCode = "404", description = "Utilizatorul nu exista")
    })
    @GetMapping("/{id}")
    public Utilizator getById(@PathVariable Long id) {
        return utilizatorService.getById(id);
    }

    @Operation(
            summary = "Verifica utilizator pentru vanzator (uz intern, apelat de Sales Service)",
            description = "Returneaza true daca exista un cont de utilizator asociat vanzatorului dat"
    )
    @GetMapping("/by-vanzator/{vanzatorId}")
    public boolean existaPentruVanzator(@PathVariable Long vanzatorId) {
        return utilizatorService.existaUtilizatorPentruVanzator(vanzatorId);
    }

    @Operation(
            summary = "Actualizeaza utilizator",
            description = "Actualizeaza datele unui utilizator existent"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilizator actualizat"),
            @ApiResponse(responseCode = "400", description = "Date invalide sau username deja folosit"),
            @ApiResponse(responseCode = "404", description = "Utilizatorul nu exista")
    })
    @PutMapping("/{id}")
    public Utilizator update(@PathVariable Long id, @Valid @RequestBody UpdateUtilizatorRequest req) {
        return utilizatorService.update(id, req);
    }

    @Operation(
            summary = "Sterge utilizator",
            description = "Sterge un utilizator existent"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Utilizator sters"),
            @ApiResponse(responseCode = "404", description = "Utilizatorul nu exista")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        utilizatorService.delete(id);
    }
}
