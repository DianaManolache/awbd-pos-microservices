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
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.dto.ProdusResponse;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CatalogGatewayResilienceTest {

    @Autowired
    private CatalogGateway catalogGateway;

    @MockBean
    private CatalogClient catalogClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void resetCircuitBreaker() {
        circuitBreakerRegistry.circuitBreaker("catalogService").reset();
    }

    private feign.FeignException.BadRequest badRequest() {
        feign.Request request = feign.Request.create(feign.Request.HttpMethod.POST, "/api/produse/1/ajusteaza-stoc",
                Map.of(), null, StandardCharsets.UTF_8, null);
        feign.Response response = feign.Response.builder()
                .status(400).reason("Bad Request").request(request).headers(Map.of()).build();
        return (feign.FeignException.BadRequest) feign.FeignException.errorStatus("CatalogClient#ajusteazaStoc", response);
    }

    @Test
    void getProdus_retriesOnTransientFailure_thenSucceeds() {
        ProdusResponse produs = new ProdusResponse();
        produs.setId(1L);

        Mockito.when(catalogClient.getProdus(1L))
                .thenThrow(new RuntimeException("conexiune pierduta"))
                .thenThrow(new RuntimeException("conexiune pierduta"))
                .thenReturn(produs);

        ProdusResponse result = catalogGateway.getProdus(1L);

        assertEquals(1L, result.getId());
        Mockito.verify(catalogClient, Mockito.times(3)).getProdus(1L);
    }

    @Test
    void getProdus_doesNotRetry_onBadRequest() {
        Mockito.when(catalogClient.getProdus(1L)).thenThrow(badRequest());

        assertThrows(feign.FeignException.BadRequest.class, () -> catalogGateway.getProdus(1L));
        Mockito.verify(catalogClient, Mockito.times(1)).getProdus(1L);
    }

    @Test
    void ajusteazaStoc_doesNotRetry_evenOnTransientFailure() {
        Mockito.when(catalogClient.ajusteazaStoc(Mockito.eq(1L), Mockito.anyMap()))
                .thenThrow(new RuntimeException("conexiune pierduta"));

        assertThrows(ResponseStatusException.class,
                () -> catalogGateway.ajusteazaStoc(1L, Map.of("delta", -1)));
        Mockito.verify(catalogClient, Mockito.times(1)).ajusteazaStoc(1L, Map.of("delta", -1));
    }

    @Test
    void ajusteazaStoc_propagatesBadRequest_notMaskedAs503() {
        Mockito.when(catalogClient.ajusteazaStoc(Mockito.eq(1L), Mockito.anyMap())).thenThrow(badRequest());

        assertThrows(feign.FeignException.BadRequest.class,
                () -> catalogGateway.ajusteazaStoc(1L, Map.of("delta", -1)));
    }

    @Test
    void circuitOpens_afterRepeatedFailures_thenShortCircuitsWithoutCallingCatalog() {
        Mockito.when(catalogClient.ajusteazaStoc(Mockito.eq(1L), Mockito.anyMap()))
                .thenThrow(new RuntimeException("catalog picat"));

        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("catalogService");
        for (int i = 0; i < 20 && breaker.getState() != CircuitBreaker.State.OPEN; i++) {
            assertThrows(ResponseStatusException.class,
                    () -> catalogGateway.ajusteazaStoc(1L, Map.of("delta", -1)));
        }

        assertEquals(CircuitBreaker.State.OPEN, breaker.getState());

        Mockito.clearInvocations(catalogClient);
        assertThrows(ResponseStatusException.class, () -> catalogGateway.ajusteazaStoc(1L, Map.of("delta", -1)));
        Mockito.verify(catalogClient, Mockito.never()).ajusteazaStoc(Mockito.any(), Mockito.any());
    }
}
