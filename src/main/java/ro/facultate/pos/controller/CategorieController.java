package ro.facultate.pos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.facultate.pos.dto.CreateCategorieRequest;
import ro.facultate.pos.dto.UpdateCategorieRequest;
import ro.facultate.pos.entity.Categorie;
import ro.facultate.pos.service.CategorieService;

import java.util.List;

@Tag(
        name = "Categorii",
        description = "Operatii pentru gestionarea categoriilor"
)
@RestController
@RequestMapping("/api/categorii")
public class CategorieController {

    private final CategorieService categorieService;

    public CategorieController(CategorieService categorieService) {
        this.categorieService = categorieService;
    }

    @Operation(
            summary = "Creeaza categorie",
            description = "Creeaza o categorie noua de produse"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categorie creata cu succes"),
            @ApiResponse(responseCode = "400", description = "Date invalide (validare)")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Categorie create(@Valid @RequestBody CreateCategorieRequest req) {
        return categorieService.create(req);
    }

    @Operation(
            summary = "Listeaza categorii",
            description = "Returneaza lista tuturor categoriilor existente"
    )
    @ApiResponse(responseCode = "200", description = "Lista categorii returnata")
    @GetMapping
    public List<Categorie> getAll() {
        return categorieService.getAll();
    }

    @Operation(summary = "Detalii categorie", description = "Returneaza o categorie specificata")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categorie gasita"),
            @ApiResponse(responseCode = "404", description = "Categoria nu exista")
    })
    @GetMapping("/{id}")
    public Categorie getById(@PathVariable Long id) {
        return categorieService.getById(id);
    }

    @Operation(summary = "Actualizeaza categorie", description = "Actualizeaza numele unei categorii existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categorie actualizata"),
            @ApiResponse(responseCode = "400", description = "Date invalide (validare)"),
            @ApiResponse(responseCode = "404", description = "Categoria nu exista")
    })
    @PutMapping("/{id}")
    public Categorie update(@PathVariable Long id, @Valid @RequestBody UpdateCategorieRequest req) {
        return categorieService.update(id, req);
    }

    @Operation(summary = "Sterge categorie", description = "Sterge o categorie existenta")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categorie sterasa"),
            @ApiResponse(responseCode = "400", description = "Categoria are produse asociate"),
            @ApiResponse(responseCode = "404", description = "Categoria nu exista")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categorieService.delete(id);
    }
}
