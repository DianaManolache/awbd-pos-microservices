package ro.facultate.pos.gateway.ratelimit;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRateLimiterTest {

    private final InMemoryRateLimiter rateLimiter = new InMemoryRateLimiter();

    @Test
    void isAllowed_underLimit_allowsRequest() {
        InMemoryRateLimiter.Config config = new InMemoryRateLimiter.Config();
        config.setMaxRequests(5);
        config.setWindowSeconds(10);
        rateLimiter.getConfig().put("test-route", config);

        for (int i = 0; i < 5; i++) {
            StepVerifier.create(rateLimiter.isAllowed("test-route", "client-1"))
                    .assertNext(response -> assertTrue(response.isAllowed()))
                    .verifyComplete();
        }
    }

    @Test
    void isAllowed_overLimit_blocksRequest() {
        InMemoryRateLimiter.Config config = new InMemoryRateLimiter.Config();
        config.setMaxRequests(3);
        config.setWindowSeconds(10);
        rateLimiter.getConfig().put("test-route-2", config);

        for (int i = 0; i < 3; i++) {
            StepVerifier.create(rateLimiter.isAllowed("test-route-2", "client-2"))
                    .assertNext(response -> assertTrue(response.isAllowed()))
                    .verifyComplete();
        }

        StepVerifier.create(rateLimiter.isAllowed("test-route-2", "client-2"))
                .assertNext(response -> assertFalse(response.isAllowed()))
                .verifyComplete();
    }

    @Test
    void isAllowed_differentClients_areTrackedIndependently() {
        InMemoryRateLimiter.Config config = new InMemoryRateLimiter.Config();
        config.setMaxRequests(1);
        config.setWindowSeconds(10);
        rateLimiter.getConfig().put("test-route-3", config);

        StepVerifier.create(rateLimiter.isAllowed("test-route-3", "client-a"))
                .assertNext(response -> assertTrue(response.isAllowed()))
                .verifyComplete();

        StepVerifier.create(rateLimiter.isAllowed("test-route-3", "client-b"))
                .assertNext(response -> assertTrue(response.isAllowed()))
                .verifyComplete();

        StepVerifier.create(rateLimiter.isAllowed("test-route-3", "client-a"))
                .assertNext(response -> assertFalse(response.isAllowed()))
                .verifyComplete();
    }

    @Test
    void isAllowed_windowExpires_allowsRequestAgain() throws InterruptedException {
        InMemoryRateLimiter.Config config = new InMemoryRateLimiter.Config();
        config.setMaxRequests(1);
        config.setWindowSeconds(1);
        rateLimiter.getConfig().put("test-route-4", config);

        StepVerifier.create(rateLimiter.isAllowed("test-route-4", "client-c"))
                .assertNext(response -> assertTrue(response.isAllowed()))
                .verifyComplete();

        StepVerifier.create(rateLimiter.isAllowed("test-route-4", "client-c"))
                .assertNext(response -> assertFalse(response.isAllowed()))
                .verifyComplete();

        Thread.sleep(1100);

        StepVerifier.create(rateLimiter.isAllowed("test-route-4", "client-c"))
                .assertNext(response -> assertTrue(response.isAllowed()))
                .verifyComplete();
    }
}
