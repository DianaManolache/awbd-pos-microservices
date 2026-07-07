package ro.facultate.pos.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.dto.ProdusResponse;

import java.util.List;
import java.util.Map;

/**
 * Wrapper peste {@link CatalogClient} care adauga circuit breaker + retry.
 * Retry e aplicat doar pe apelurile read-only (getAllProduse, getProdus) -
 * ajusteazaStoc e o mutatie pe delta, si reincercarea ei ar putea dubla
 * ajustarea stocului daca primul apel a reusit efectiv pe server dar
 * raspunsul s-a pierdut, asa ca ramane doar cu circuit breaker (fail-fast),
 * fara retry automat.
 */
@Component
public class CatalogGateway {

    private static final String INSTANCE = "catalogService";

    private final CatalogClient catalogClient;

    public CatalogGateway(CatalogClient catalogClient) {
        this.catalogClient = catalogClient;
    }

    @CircuitBreaker(name = INSTANCE, fallbackMethod = "getAllProduseFallback")
    @Retry(name = INSTANCE)
    public List<ProdusResponse> getAllProduse() {
        return catalogClient.getAllProduse();
    }

    @CircuitBreaker(name = INSTANCE, fallbackMethod = "getProdusFallback")
    @Retry(name = INSTANCE)
    public ProdusResponse getProdus(Long id) {
        return catalogClient.getProdus(id);
    }

    @CircuitBreaker(name = INSTANCE, fallbackMethod = "ajusteazaStocFallback")
    public ProdusResponse ajusteazaStoc(Long id, Map<String, Integer> body) {
        return catalogClient.ajusteazaStoc(id, body);
    }

    private List<ProdusResponse> getAllProduseFallback(Throwable t) {
        rethrowIfBusinessException(t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalog indisponibil, reincercati mai tarziu");
    }

    private ProdusResponse getProdusFallback(Long id, Throwable t) {
        rethrowIfBusinessException(t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalog indisponibil, reincercati mai tarziu");
    }

    private ProdusResponse ajusteazaStocFallback(Long id, Map<String, Integer> body, Throwable t) {
        rethrowIfBusinessException(t);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalog indisponibil - stocul nu a putut fi ajustat, reincercati mai tarziu");
    }

    /**
     * Un 400/404 de la Catalog e un raspuns de business normal (ex. stoc
     * insuficient, produs inexistent), nu o defectiune de infrastructura -
     * trebuie sa treaca neschimbat catre apelant, nu mascat ca 503.
     */
    private void rethrowIfBusinessException(Throwable t) {
        if (t instanceof feign.FeignException.BadRequest || t instanceof feign.FeignException.NotFound) {
            throw (RuntimeException) t;
        }
    }
}
