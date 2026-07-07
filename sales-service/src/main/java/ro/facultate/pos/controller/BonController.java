package ro.facultate.pos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.facultate.pos.dto.CreateBonRequest;
import ro.facultate.pos.dto.UpdateBonRequest;
import ro.facultate.pos.entity.Bon;
import ro.facultate.pos.service.BonService;
import ro.facultate.pos.dto.AddBonProdusRequest;
import ro.facultate.pos.entity.BonProdus;
import ro.facultate.pos.dto.BonDetailsResponse;
import ro.facultate.pos.dto.PayBonRequest;
import ro.facultate.pos.entity.Plata;

import java.util.List;

@Tag(
        name = "Bonuri si Plati",
        description = "Operatii pentru gestionarea procesului de cumparare"
)
@RestController
@RequestMapping("/api/bons")
public class BonController {

    private final BonService bonService;

    public BonController(BonService bonService) {
        this.bonService = bonService;
    }

    @Operation(
            summary = "Creeaza bon",
            description = "Creeaza un bon nou cu status OPEN pentru un client si un vanzator"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bon creat cu succes"),
            @ApiResponse(responseCode = "400", description = "Date invalide (validare)"),
            @ApiResponse(responseCode = "404", description = "Client sau vanzator inexistent")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Bon create(@Valid @RequestBody CreateBonRequest req) {
        return bonService.create(req);
    }

    @Operation(
            summary = "Listeaza bonuri",
            description = "Returneaza lista tuturor bonurilor"
    )
    @ApiResponse(responseCode = "200", description = "Lista bonuri returnata")
    @GetMapping
    public List<Bon> getAll() {
        return bonService.getAll();
    }

    @Operation(
            summary = "Verifica daca un produs e pe vreun bon (uz intern, apelat de Catalog Service)",
            description = "Returneaza true daca produsul apare pe cel putin o linie de bon"
    )
    @GetMapping("/produse/{produsId}/pe-bon")
    public boolean produsExistaPeBon(@PathVariable Long produsId) {
        return bonService.existaProdusPeVreunBon(produsId);
    }

    @Operation(
            summary = "Actualizeaza bon",
            description = "Actualizeaza clientul/vanzatorul unui bon OPEN fara produse adaugate"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bon actualizat"),
            @ApiResponse(responseCode = "400", description = "Bonul nu este OPEN sau are deja produse adaugate"),
            @ApiResponse(responseCode = "404", description = "Bon, client sau vanzator inexistent")
    })
    @PutMapping("/{id}")
    public Bon update(@PathVariable Long id, @Valid @RequestBody UpdateBonRequest req) {
        return bonService.update(id, req);
    }

    @Operation(
            summary = "Sterge bon",
            description = "Sterge un bon OPEN care nu are produse sau plati asociate"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Bon sters"),
            @ApiResponse(responseCode = "400", description = "Bonul nu este OPEN sau are produse/plati asociate"),
            @ApiResponse(responseCode = "404", description = "Bon inexistent")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bonService.delete(id);
    }

    @Operation(
            summary = "Adauga produs pe bon",
            description = "Adauga un produs pe un bon OPEN si scade stocul produsului"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produs adaugat pe bon"),
            @ApiResponse(responseCode = "400", description = "Bon inchis sau stoc insuficient"),
            @ApiResponse(responseCode = "404", description = "Bon sau produs inexistent")
    })
    @PostMapping("/{bonId}/produse")
    @ResponseStatus(HttpStatus.CREATED)
    public BonProdus addProdus(
            @PathVariable Long bonId,
            @Valid @RequestBody AddBonProdusRequest req) {
        return bonService.addProdus(bonId, req);
    }

    @Operation(
            summary = "Actualizeaza cantitate pe linie",
            description = "Actualizeaza cantitatea unei linii de pe un bon OPEN si ajusteaza stocul"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Linie actualizata"),
            @ApiResponse(responseCode = "400", description = "Bon inchis sau stoc insuficient"),
            @ApiResponse(responseCode = "404", description = "Bon sau linie inexistenta")
    })
    @PutMapping("/{bonId}/produse/{bonProdusId}")
    public BonProdus updateBonProdus(
            @PathVariable Long bonId,
            @PathVariable Long bonProdusId,
            @Valid @RequestBody ro.facultate.pos.dto.UpdateBonProdusRequest req) {
        return bonService.updateBonProdus(bonId, bonProdusId, req);
    }

    @Operation(
            summary = "Sterge linie de pe bon",
            description = "Sterge o linie de pe un bon OPEN si restituie stocul"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Linie stearsa"),
            @ApiResponse(responseCode = "400", description = "Bonul nu este OPEN"),
            @ApiResponse(responseCode = "404", description = "Bon sau linie inexistenta")
    })
    @DeleteMapping("/{bonId}/produse/{bonProdusId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBonProdus(@PathVariable Long bonId, @PathVariable Long bonProdusId) {
        bonService.deleteBonProdus(bonId, bonProdusId);
    }

    @Operation(
            summary = "Detalii bon",
            description = "Returneaza detaliile unui bon impreuna cu produsele si totalul"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalii bon returnate"),
            @ApiResponse(responseCode = "404", description = "Bon inexistent")
    })
    @GetMapping("/{bonId}")
    public BonDetailsResponse getDetails(@PathVariable Long bonId) {
        return bonService.getDetails(bonId);
    }

    @Operation(
            summary = "Plateste bon",
            description = "Proceseaza plata unui bon OPEN si il marcheaza ca PAID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Plata efectuata cu succes"),
            @ApiResponse(responseCode = "400", description = "Bon deja inchis"),
            @ApiResponse(responseCode = "404", description = "Bon inexistent")
    })
    @PostMapping("/{bonId}/pay")
    @ResponseStatus(HttpStatus.CREATED)
    public Plata payBon(
            @PathVariable Long bonId,
            @Valid @RequestBody PayBonRequest req) {
        return bonService.payBon(bonId, req.getTipPlata());
    }

    @Operation(
            summary = "Listeaza platile unui bon",
            description = "Returneaza toate platile asociate unui bon"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista plati returnata"),
            @ApiResponse(responseCode = "404", description = "Bon inexistent")
    })
    @GetMapping("/{bonId}/plati")
    public List<Plata> getPlati(@PathVariable Long bonId) {
        return bonService.getPlati(bonId);
    }

    @Operation(
            summary = "Detalii plata",
            description = "Returneaza o plata specifica a unui bon"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plata gasita"),
            @ApiResponse(responseCode = "404", description = "Bon sau plata inexistenta")
    })
    @GetMapping("/{bonId}/plati/{plataId}")
    public Plata getPlata(@PathVariable Long bonId, @PathVariable Long plataId) {
        return bonService.getPlata(bonId, plataId);
    }

    @Operation(
            summary = "Actualizeaza plata",
            description = "Actualizeaza tipul/suma unei plati care nu are status SUCCESS"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plata actualizata"),
            @ApiResponse(responseCode = "400", description = "Plata are status SUCCESS"),
            @ApiResponse(responseCode = "404", description = "Bon sau plata inexistenta")
    })
    @PutMapping("/{bonId}/plati/{plataId}")
    public Plata updatePlata(
            @PathVariable Long bonId,
            @PathVariable Long plataId,
            @Valid @RequestBody ro.facultate.pos.dto.UpdatePlataRequest req) {
        return bonService.updatePlata(bonId, plataId, req);
    }

    @Operation(
            summary = "Sterge plata",
            description = "Sterge o plata care nu are status SUCCESS"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Plata stearsa"),
            @ApiResponse(responseCode = "400", description = "Plata are status SUCCESS"),
            @ApiResponse(responseCode = "404", description = "Bon sau plata inexistenta")
    })
    @DeleteMapping("/{bonId}/plati/{plataId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlata(@PathVariable Long bonId, @PathVariable Long plataId) {
        bonService.deletePlata(bonId, plataId);
    }
}
