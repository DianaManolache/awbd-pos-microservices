package ro.facultate.pos.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SalesGatewayResilienceTest {

    @Autowired
    private SalesGateway salesGateway;

    @MockBean
    private SalesClient salesClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void resetCircuitBreaker() {
        circuitBreakerRegistry.circuitBreaker("salesService").reset();
    }

    @Test
    void produsExistaPeBon_retriesOnTransientFailure_thenSucceeds() {
        Mockito.when(salesClient.produsExistaPeBon(1L))
                .thenThrow(new RuntimeException("sales picat"))
                .thenReturn(false);

        boolean result = salesGateway.produsExistaPeBon(1L);

        assertFalse(result);
        Mockito.verify(salesClient, Mockito.times(2)).produsExistaPeBon(1L);
    }

    @Test
    void produsExistaPeBon_failsClosed_whenSalesUnavailable() {
        Mockito.when(salesClient.produsExistaPeBon(1L)).thenThrow(new RuntimeException("sales picat"));

        boolean result = salesGateway.produsExistaPeBon(1L);

        assertTrue(result, "fail-closed: cand Sales e indisponibil, presupunem ca produsul e referentiat si blocam stergerea");
    }

    @Test
    void circuitOpens_afterRepeatedFailures_thenShortCircuitsWithoutCallingSales() {
        Mockito.when(salesClient.produsExistaPeBon(2L)).thenThrow(new RuntimeException("sales picat"));

        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("salesService");
        for (int i = 0; i < 20 && breaker.getState() != CircuitBreaker.State.OPEN; i++) {
            assertTrue(salesGateway.produsExistaPeBon(2L));
        }

        assertEquals(CircuitBreaker.State.OPEN, breaker.getState());

        Mockito.clearInvocations(salesClient);
        assertTrue(salesGateway.produsExistaPeBon(2L));
        Mockito.verify(salesClient, Mockito.never()).produsExistaPeBon(2L);
    }
}
