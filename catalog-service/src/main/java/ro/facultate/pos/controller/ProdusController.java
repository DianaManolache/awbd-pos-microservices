package ro.facultate.pos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.facultate.pos.dto.AjusteazaStocRequest;
import ro.facultate.pos.dto.CreateProdusRequest;
import ro.facultate.pos.dto.UpdateProdusRequest;
import ro.facultate.pos.entity.Produs;
import ro.facultate.pos.service.ProdusService;
import ro.facultate.pos.dto.UpdateStocRequest;

import java.util.List;

@Tag(
        name = "Produse",
        description = "Operatii pentru gestionarea produselor"
)
@RestController
@RequestMapping("/api/produse")
public class ProdusController {

    private final ProdusService produsService;

    public ProdusController(ProdusService produsService) {
        this.produsService = produsService;
    }

    @Operation(
            summary = "Creeaza produs",
            description = "Creeaza un produs nou si il asociaza unei categorii existente"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produs creat cu succes"),
            @ApiResponse(responseCode = "400", description = "Date invalide (validare)"),
            @ApiResponse(responseCode = "404", description = "Categoria nu exista")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Produs create(@Valid @RequestBody CreateProdusRequest req) {
        return produsService.create(req);
    }

    @Operation(
            summary = "Listeaza produse",
            description = "Returneaza lista tuturor produselor"
    )
    @ApiResponse(responseCode = "200", description = "Lista produse")
    @GetMapping
    public List<Produs> getAll() {
        List<Produs> produse = produsService.getAll();
        produsService.aplicaPretEfectiv(produse);
        return produse;
    }

    @Operation(
            summary = "Listeaza produse dupa categorie",
            description = "Returneaza lista tuturor produselor dintr-o categorie specificata"
    )
    @ApiResponse(responseCode = "200", description = "Lista produse")
    @GetMapping("/categorie/{categorieId}")
    public List<Produs> getByCategorie(@PathVariable Long categorieId) {
        List<Produs> produse = produsService.getByCategorie(categorieId);
        produsService.aplicaPretEfectiv(produse);
        return produse;
    }

    @Operation(
            summary = "Modificare stoc produs",
            description = "Modifica stocul unui produs specificat"
    )
    @PutMapping("/{produsId}/stoc")
    public Produs updateStoc(
            @PathVariable Long produsId,
            @Valid @RequestBody UpdateStocRequest req) {
        return produsService.updateStoc(produsId, req);
    }

    @Operation(
            summary = "Ajusteaza stoc produs (uz intern, apelat de Sales Service)",
            description = "Aplica un delta (pozitiv sau negativ) asupra stocului unui produs"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stoc ajustat"),
            @ApiResponse(responseCode = "400", description = "Stocul rezultat ar fi negativ"),
            @ApiResponse(responseCode = "404", description = "Produsul nu exista")
    })
    @PostMapping("/{id}/ajusteaza-stoc")
    public Produs ajusteazaStoc(@PathVariable Long id, @Valid @RequestBody AjusteazaStocRequest req) {
        return produsService.ajusteazaStoc(id, req);
    }

    @Operation(summary = "Detalii produs", description = "Returneaza un produs specificat")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produs gasit"),
            @ApiResponse(responseCode = "404", description = "Produsul nu exista")
    })
    @GetMapping("/{id}")
    public Produs getById(@PathVariable Long id) {
        Produs produs = produsService.getById(id);
        produsService.aplicaPretEfectiv(produs);
        return produs;
    }

    @Operation(summary = "Actualizeaza produs", description = "Actualizeaza toate campurile unui produs existent")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produs actualizat"),
            @ApiResponse(responseCode = "400", description = "Date invalide (validare)"),
            @ApiResponse(responseCode = "404", description = "Produsul sau categoria nu exista")
    })
    @PutMapping("/{id}")
    public Produs update(@PathVariable Long id, @Valid @RequestBody UpdateProdusRequest req) {
        return produsService.update(id, req);
    }

    @Operation(summary = "Sterge produs", description = "Sterge un produs existent")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produs sters"),
            @ApiResponse(responseCode = "400", description = "Produsul este referentiat de un bon sau o promotie"),
            @ApiResponse(responseCode = "404", description = "Produsul nu exista")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        produsService.delete(id);
    }
}
