package ro.facultate.pos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "sales-service")
public interface SalesClient {

    @GetMapping("/api/bons/produse/{produsId}/pe-bon")
    boolean produsExistaPeBon(@PathVariable("produsId") Long produsId);
}
