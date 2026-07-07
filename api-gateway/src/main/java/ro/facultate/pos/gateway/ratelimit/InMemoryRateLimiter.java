package ro.facultate.pos.gateway.ratelimit;

import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Rate limiter cu fereastra fixa, in memorie - alternativa la RedisRateLimiter
 * (nu avem inca Redis in acest sub-proiect, vine odata cu sub-proiectul de caching).
 * Suficient pentru o singura instanta de Gateway; pentru mai multe instante ar fi
 * nevoie de o stare partajata (ex. Redis).
 */
@Component
public class InMemoryRateLimiter implements RateLimiter<InMemoryRateLimiter.Config> {

    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();
    private final Map<String, Config> configByRoute = new ConcurrentHashMap<>();
    private final Config defaultConfig = new Config();

    @Override
    public Mono<Response> isAllowed(String routeId, String id) {
        Config config = configByRoute.getOrDefault(routeId, defaultConfig);
        long now = System.currentTimeMillis();
        long windowStart = now - config.getWindowSeconds() * 1000L;

        Deque<Long> timestamps = requestLog.computeIfAbsent(id, key -> new ConcurrentLinkedDeque<>());
        boolean allowed;
        int remaining;
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
                timestamps.pollFirst();
            }
            allowed = timestamps.size() < config.getMaxRequests();
            if (allowed) {
                timestamps.addLast(now);
            }
            remaining = Math.max(0, config.getMaxRequests() - timestamps.size());
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("X-RateLimit-Remaining", String.valueOf(remaining));
        headers.put("X-RateLimit-Limit", String.valueOf(config.getMaxRequests()));
        return Mono.just(new Response(allowed, headers));
    }

    @Override
    public Map<String, Config> getConfig() {
        return configByRoute;
    }

    @Override
    public Class<Config> getConfigClass() {
        return Config.class;
    }

    @Override
    public Config newConfig() {
        return new Config();
    }

    public static class Config {
        private int maxRequests = 20;
        private int windowSeconds = 10;

        public int getMaxRequests() { return maxRequests; }
        public void setMaxRequests(int maxRequests) { this.maxRequests = maxRequests; }

        public int getWindowSeconds() { return windowSeconds; }
        public void setWindowSeconds(int windowSeconds) { this.windowSeconds = windowSeconds; }
    }
}
