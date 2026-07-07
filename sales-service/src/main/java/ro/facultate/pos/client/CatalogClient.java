package ro.facultate.pos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ro.facultate.pos.dto.ProdusResponse;

import java.util.List;
import java.util.Map;

@FeignClient(name = "catalog-service")
public interface CatalogClient {

    @GetMapping("/api/produse")
    List<ProdusResponse> getAllProduse();

    @GetMapping("/api/produse/{id}")
    ProdusResponse getProdus(@PathVariable("id") Long id);

    @PostMapping("/api/produse/{id}/ajusteaza-stoc")
    ProdusResponse ajusteazaStoc(@PathVariable("id") Long id, @RequestBody Map<String, Integer> body);
}
