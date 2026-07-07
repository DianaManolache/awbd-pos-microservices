package ro.facultate.pos.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Filtru global de request/response: adauga un header de corelare (X-Correlation-Id)
 * pe cerere si pe raspuns, si logheaza metoda/path/status pentru fiecare cerere care
 * trece prin Gateway.
 */
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingGlobalFilter.class);
    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = request.getHeaders().getFirst(CORRELATION_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        String finalCorrelationId = correlationId;

        ServerHttpRequest mutatedRequest = request.mutate()
                .header(CORRELATION_HEADER, finalCorrelationId)
                .build();

        log.info("[{}] {} {} de la {}", finalCorrelationId, request.getMethod(),
                request.getURI().getPath(), request.getRemoteAddress());

        // Headers can only be mutated before the response commits (starts streaming) -
        // adding them after chain.filter() completes throws UnsupportedOperationException
        // because the header map becomes read-only once the response is committed.
        exchange.getResponse().beforeCommit(() -> {
            exchange.getResponse().getHeaders().add(CORRELATION_HEADER, finalCorrelationId);
            log.info("[{}] raspuns {}", finalCorrelationId, exchange.getResponse().getStatusCode());
            return Mono.empty();
        });

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
