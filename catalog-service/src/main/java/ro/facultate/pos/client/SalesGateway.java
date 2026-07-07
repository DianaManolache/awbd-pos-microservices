package ro.facultate.pos.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;

/**
 * Wrapper peste {@link SalesClient} care adauga circuit breaker + retry.
 * Cand Sales e indisponibil, verificarea "produsul e pe vreun bon?" (folosita
 * inainte de stergerea unui produs) esueaza inspre "true" (fail-closed) -
 * mai sigur sa blochezi o stergere decat sa permiti una nesigura pe baza
 * unei informatii pe care nu am putut sa o confirmam.
 */
@Component
public class SalesGateway {

    private static final String INSTANCE = "salesService";

    private final SalesClient salesClient;

    public SalesGateway(SalesClient salesClient) {
        this.salesClient = salesClient;
    }

    @CircuitBreaker(name = INSTANCE, fallbackMethod = "produsExistaPeBonFallback")
    @Retry(name = INSTANCE)
    public boolean produsExistaPeBon(Long produsId) {
        return salesClient.produsExistaPeBon(produsId);
    }

    private boolean produsExistaPeBonFallback(Long produsId, Throwable t) {
        return true;
    }
}
