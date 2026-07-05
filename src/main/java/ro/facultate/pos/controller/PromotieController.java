package ro.facultate.pos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.facultate.pos.dto.CreatePromotieRequest;
import ro.facultate.pos.dto.UpdatePromotieRequest;
import ro.facultate.pos.entity.Promotie;
import ro.facultate.pos.service.PromotieService;

import java.util.List;

@Tag(
        name = "Promotii",
        description = "Operatii pentru gestionarea promotiilor"
)
@RestController
@RequestMapping("/api/promotii")
public class PromotieController {

    private final PromotieService promotieService;

    public PromotieController(PromotieService promotieService) {
        this.promotieService = promotieService;
    }

    @Operation(summary = "Creeaza promotie", description = "Creeaza o promotie noua")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Promotie creata cu succes"),
            @ApiResponse(responseCode = "400", description = "Date invalide (validare sau interval de date)")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Promotie create(@Valid @RequestBody CreatePromotieRequest req) {
        return promotieService.create(req);
    }

    @Operation(summary = "Listeaza promotii", description = "Returneaza lista tuturor promotiilor")
    @ApiResponse(responseCode = "200", description = "Lista promotii")
    @GetMapping
    public List<Promotie> getAll() {
        return promotieService.getAll();
    }

    @Operation(summary = "Detalii promotie", description = "Returneaza o promotie specificata")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotie gasita"),
            @ApiResponse(responseCode = "404", description = "Promotia nu exista")
    })
    @GetMapping("/{id}")
    public Promotie getById(@PathVariable Long id) {
        return promotieService.getById(id);
    }

    @Operation(summary = "Actualizeaza promotie", description = "Actualizeaza datele unei promotii existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotie actualizata"),
            @ApiResponse(responseCode = "400", description = "Date invalide (validare sau interval de date)"),
            @ApiResponse(responseCode = "404", description = "Promotia nu exista")
    })
    @PutMapping("/{id}")
    public Promotie update(@PathVariable Long id, @Valid @RequestBody UpdatePromotieRequest req) {
        return promotieService.update(id, req);
    }

    @Operation(summary = "Sterge promotie", description = "Sterge o promotie existenta")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Promotie sterasa"),
            @ApiResponse(responseCode = "404", description = "Promotia nu exista")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        promotieService.delete(id);
    }

    @Operation(summary = "Adauga produs la promotie", description = "Asociaza un produs existent unei promotii")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produs adaugat"),
            @ApiResponse(responseCode = "400", description = "Produsul este deja in promotie"),
            @ApiResponse(responseCode = "404", description = "Promotia sau produsul nu exista")
    })
    @PostMapping("/{id}/produse/{produsId}")
    public Promotie addProdus(@PathVariable Long id, @PathVariable Long produsId) {
        return promotieService.addProdus(id, produsId);
    }

    @Operation(summary = "Scoate produs din promotie", description = "Dezasociaza un produs dintr-o promotie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produs scos din promotie"),
            @ApiResponse(responseCode = "404", description = "Promotia, produsul sau asocierea nu exista")
    })
    @DeleteMapping("/{id}/produse/{produsId}")
    public Promotie removeProdus(@PathVariable Long id, @PathVariable Long produsId) {
        return promotieService.removeProdus(id, produsId);
    }
}
