package ro.facultate.pos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.facultate.pos.dto.CreateVanzatorRequest;
import ro.facultate.pos.dto.UpdateVanzatorRequest;
import ro.facultate.pos.entity.Vanzator;
import ro.facultate.pos.service.VanzatorService;

import java.util.List;

@Tag(
        name = "Vanzatori",
        description = "Operatii pentru gestionarea vanzatorilor"
)
@RestController
@RequestMapping("/api/vanzatori")
public class VanzatorController {

    private final VanzatorService vanzatorService;

    public VanzatorController(VanzatorService vanzatorService) {
        this.vanzatorService = vanzatorService;
    }

    @Operation(
            summary = "Adauga vanzator",
            description = "Adauga un vanzator nou"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vanzator adaugat cu succes"),
            @ApiResponse(responseCode = "400", description = "Date invalide (validare)"),
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vanzator create(@Valid @RequestBody CreateVanzatorRequest req) {
        return vanzatorService.create(req);
    }

    @Operation(
            summary = "Listeaza vanzatorii",
            description = "Returneaza lista cu toti vanzatorii"
    )
    @ApiResponse(responseCode = "200", description = "Lista vanzatori")
    @GetMapping
    public List<Vanzator> getAll() {
        return vanzatorService.getAll();
    }

    @Operation(summary = "Detalii vanzator", description = "Returneaza un vanzator specificat")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vanzator gasit"),
            @ApiResponse(responseCode = "404", description = "Vanzatorul nu exista")
    })
    @GetMapping("/{id}")
    public Vanzator getById(@PathVariable Long id) {
        return vanzatorService.getById(id);
    }

    @Operation(summary = "Actualizeaza vanzator", description = "Actualizeaza numele unui vanzator existent")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vanzator actualizat"),
            @ApiResponse(responseCode = "400", description = "Date invalide (validare)"),
            @ApiResponse(responseCode = "404", description = "Vanzatorul nu exista")
    })
    @PutMapping("/{id}")
    public Vanzator update(@PathVariable Long id, @Valid @RequestBody UpdateVanzatorRequest req) {
        return vanzatorService.update(id, req);
    }

    @Operation(summary = "Sterge vanzator", description = "Sterge un vanzator existent")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vanzator sters"),
            @ApiResponse(responseCode = "400", description = "Vanzatorul are bonuri sau cont de utilizator asociat"),
            @ApiResponse(responseCode = "404", description = "Vanzatorul nu exista")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        vanzatorService.delete(id);
    }
}
