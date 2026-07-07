package ro.facultate.pos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ro.facultate.pos.dto.VanzatorResponse;

import java.util.List;
import java.util.Map;

@FeignClient(name = "sales-service")
public interface SalesClient {

    @GetMapping("/api/vanzatori")
    List<VanzatorResponse> getAllVanzatori();

    @GetMapping("/api/vanzatori/{id}")
    VanzatorResponse getVanzator(@PathVariable("id") Long id);

    @PostMapping("/api/vanzatori")
    VanzatorResponse creeazaVanzator(@RequestBody Map<String, String> body);
}
